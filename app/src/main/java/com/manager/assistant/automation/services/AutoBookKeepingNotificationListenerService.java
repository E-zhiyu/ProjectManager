package com.manager.assistant.automation.services;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.sqlite.SQLiteException;
import android.os.Build;
import android.os.Bundle;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.RemoteInput;

import com.manager.assistant.R;
import com.manager.assistant.automation.broadcast.bookkeeping.AutoBookkeepingActionsReceiver;
import com.manager.assistant.data.controllers.RuleDataController;
import com.manager.assistant.data.controllers.TagDataController;
import com.manager.assistant.generic_enums.ChannelInfo;
import com.manager.assistant.generic_enums.LogTags;
import com.manager.assistant.automation.broadcast.RuleUpdateReceiver;
import com.manager.assistant.automation.broadcast.BroadcastActions;
import com.manager.assistant.data.save.preference.AutoBookKeepingPreference;
import com.manager.assistant.generic_enums.KeyStrings;
import com.manager.assistant.data.classes.AnalysisRule;
import com.manager.assistant.generic_enums.NotificationID;
import com.manager.assistant.generic_enums.PendingRequestCode;
import com.manager.assistant.helpers.NotificationHelper;
import com.manager.assistant.auxiliary.enums.AccountType;
import com.manager.assistant.ui.pages.rule.AnalysisRuleManageActivity;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class AutoBookKeepingNotificationListenerService extends NotificationListenerService
        implements RuleUpdateReceiver.BroadcastListener {
    private final HashMap<RuleKey, List<RuleValue>> ruleHashMap = new HashMap<>();    //解析规则哈希表
    private RuleUpdateReceiver ruleUpdateReceiver;   //规则更新的广播接收器
    private boolean isFunctionOpened;                                   //通知解析功能是否开启
    private String lastPackageName = "";                                //上一次接收通知的包名
    private String lastTitle = "";                                      //上一次通知的标题
    private long lastReceiveEpochMilli = 0;                             //上一次接收消息的时间（毫秒）

    private static class RuleKey {
        private final String title;                                     //通知标题
        private final String packageName;                               //通知发送者包名

        public RuleKey(String packageName, String title) {
            this.packageName = packageName;
            this.title = title;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;

            if (o == null || getClass() != o.getClass()) return false;

            RuleKey key = (RuleKey) o;
            return Objects.equals(title, key.title)
                    && Objects.equals(packageName, key.packageName);
        }

        @Override
        public int hashCode() {
            return Objects.hash(title, packageName);
        }
    }

    private static class RuleValue {
        private final String ruleName;          //规则名称
        private final String content;           //通知内容正则表达式
        private final AccountType type;  //通知种类
        private final long rule_no;             //规则编号

        public RuleValue(String ruleName, String content, AccountType type, long rule_no) {
            this.ruleName = ruleName;
            this.content = content;
            this.type = type;
            this.rule_no = rule_no;
        }

        public String getRuleName() {
            return ruleName;
        }

        public String getContent() {
            return content;
        }

        public AccountType getType() {
            return type;
        }

        public long getRule_no() {
            return rule_no;
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(LogTags.NOTIFICATION_SERVICE.n(), "服务已启动");
        return START_STICKY;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        //启动时则加载规则
        try {
            ruleHashMap.putAll(loadRulesInHashMap());
        } catch (SQLiteException e) {
            Toast.makeText(getApplicationContext(), "通知监听服务无法加载解析规则", Toast.LENGTH_SHORT).show();
        }
        isFunctionOpened = AutoBookKeepingPreference.getSwitchStat(getApplicationContext());   //启动时加载功能开关状态

        //注册规则更新和开关状态更新的广播接收器
        ruleUpdateReceiver = new RuleUpdateReceiver(this);
        IntentFilter filter = new IntentFilter();
        filter.addAction(BroadcastActions.ACTION_RULES_UPDATED.toString());       //过滤规则更新动作
        filter.addAction(BroadcastActions.ACTION_NOTIFICATION_ANALYSIS_FUNCTION_SWITCHED.toString()); //过滤通知解析功能开关状态变化
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(ruleUpdateReceiver, filter, Context.RECEIVER_EXPORTED);
        } else {
            registerReceiver(ruleUpdateReceiver, filter);
        }

        //发送通知监听服务已运行的通知
        sendBroadcast(new Intent(BroadcastActions.ACTION_NOTIFICATION_LISTENER_ENABLED.toString()));
        Log.d(LogTags.NOTIFICATION_SERVICE.n(), "服务已创建");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        Log.d(LogTags.NOTIFICATION_SERVICE.n(), "服务已关闭");
        //注销广播接收器防止重复刷新UI
        if (ruleUpdateReceiver != null) {
            unregisterReceiver(ruleUpdateReceiver);
        }
    }

    @Override
    public void onNotificationPosted(@NonNull StatusBarNotification sbn) {
        //未开启通知监听功能则不运行
        if (!isFunctionOpened) {
            Log.d(LogTags.NOTIFICATION_SERVICE.n(), "自动记账功能未启用");
            return;
        }

        //获取通知数据
        String packageName = sbn.getPackageName();
        String title = sbn.getNotification().extras.getString("android.title");
        String text = sbn.getNotification().extras.getString("android.text");
        if (text == null || title == null) return;

        //同一应用发送太频繁直接不运行
        long currentEpochMilli = System.currentTimeMillis();
        long difference = currentEpochMilli - lastReceiveEpochMilli;        //求时间差
        lastReceiveEpochMilli = currentEpochMilli;
        boolean isSamePackageName = packageName.equals(lastPackageName);    //判断是否包名相同
        lastPackageName = packageName;
        boolean isSameTitle = title.equals(lastTitle);                      //判断标题是否相同
        lastTitle = title;
        if (difference <= 1500 && isSameTitle && isSamePackageName) {
            Log.d(LogTags.NOTIFICATION_SERVICE.n(), "同一应用发送通知过于频繁，不执行任何操作");
            return;
        }

        Log.d(LogTags.NOTIFICATION_SERVICE.n(), String.format("通知发送者包名：%s", packageName));
        Log.d(LogTags.NOTIFICATION_SERVICE.n(), String.format("通知标题：%s", title));
        Log.d(LogTags.NOTIFICATION_SERVICE.n(), String.format("通知内容：%s", text));

        //处理通知内容
        RuleKey key = new RuleKey(packageName, title);
        List<RuleValue> valueList = ruleHashMap.get(key);
        if (valueList != null) {
            for (RuleValue value : valueList) {
                String content = value.getContent();
                String ruleName = value.getRuleName();
                AccountType type = value.getType();
                long ruleNo = value.getRule_no();

                Matcher matcher;                                        //通知内容匹配器
                try {
                    Pattern pattern = Pattern.compile(content);         //编译为正则表达式
                    matcher = pattern.matcher(text);
                } catch (PatternSyntaxException e) {                    //处理无法编译为Matcher的情况
                    Log.e(LogTags.NOTIFICATION_SERVICE.n(), "正则表达式编译出错");
                    String err = String.format(
                            Locale.getDefault(),
                            "规则“%s”的正则表达式编译出错",
                            ruleName
                    );
                    sendErrorNotification(err, ruleNo);
                    continue;
                }

                if (matcher.find()) {
                    Log.d(LogTags.NOTIFICATION_SERVICE.n(), "成功匹配正则表达式");

                    //生成流水数据包
                    long tagNo = TagDataController.getTagByRuleNo(ruleNo, getApplicationContext()).getTno();
                    Bundle dataBundle = getNewAccountData(matcher, type, tagNo, ruleName, ruleNo);
                    if (dataBundle == null) {
                        return;
                    }
                    Log.i(LogTags.NOTIFICATION_SERVICE.n(), "流水数据生成成功");

                    //发送确认自动记账的通知，在通知中决定保留还是删除
                    sendNotificationToConfirm(dataBundle, ruleName, getApplicationContext());
                } else {
                    Log.d(LogTags.NOTIFICATION_SERVICE.n(), "正则表达式不匹配");
                }
            }
        }
    }

    /**
     * 处理规则更新的接口回调
     */
    @Override
    public void onRuleUpdated() {
        try {
            Log.d(LogTags.NOTIFICATION_SERVICE.n(), "收到规则更新广播，正在更新规则……");
            ruleHashMap.putAll(loadRulesInHashMap());
            Log.d(LogTags.NOTIFICATION_SERVICE.n(), "规则更新成功");
        } catch (SQLiteException e) {
            Log.w(LogTags.NOTIFICATION_SERVICE.n(), "规则更新失败");
            Toast.makeText(getApplicationContext(), "自动记账出错：无法获取更新的规则", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 处理功能开关状态变更的接口回调
     */
    @Override
    public void onFunctionSwitched() {
        Log.d(LogTags.NOTIFICATION_SERVICE.n(), "收到功能开关状态变更广播");
        isFunctionOpened = AutoBookKeepingPreference.getSwitchStat(getApplicationContext());
        Log.d(LogTags.NOTIFICATION_SERVICE.n(), "通知解析功能：" + isFunctionOpened);
    }

    /**
     * 获得流水记录数据
     *
     * @param matcher  正则表达式的匹配对象
     * @param type     解析规则中的流水种类
     * @param tagNo    解析规则对应的标签编号
     * @param ruleName 解析规则的名称
     * @param ruleNo   规则编号
     * @return 解析通知内容后生成的流水数据包(正则表达式解析失败返回null)
     * @throws SQLiteException 流水数据保存失败引发的异常
     */
    @Nullable
    private Bundle getNewAccountData(
            @NonNull Matcher matcher,
            @NonNull AccountType type,
            long tagNo,
            String ruleName,
            long ruleNo
    ) throws SQLiteException {
        //获取匹配到的金额数据
        double amount;
        try {
            amount = Double.parseDouble(Objects.requireNonNull(matcher.group(1)));
        } catch (IndexOutOfBoundsException e) { //处理没有捕获组的情况
            String err = String.format(Locale.getDefault(), "规则“%s”没有设置金额捕获组", ruleName);
            sendErrorNotification(err, ruleNo);
            return null;
        } catch (NumberFormatException e) {
            String error = String.format(Locale.getDefault(), "规则“%s”无法获取金额数据", ruleName);
            sendErrorNotification(error, ruleNo);
            return null;
        }

        //获取当前的时间
        DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        LocalDateTime now = LocalDateTime.now();
        String timeStr = dateFormat.format(now);

        //生成备注
        String remark = "自动记账：" + ruleName;

        //生成流水记录数据包
        Bundle dataBundle = new Bundle();
        dataBundle.putLong(KeyStrings.TAG_NO.v(), tagNo);
        dataBundle.putString(KeyStrings.ACCOUNT_DATETIME.v(), timeStr);
        dataBundle.putString(KeyStrings.ACCOUNT_TYPE.v(), type.toString());
        dataBundle.putDouble(KeyStrings.ACCOUNT_AMOUNT.v(), amount);
        dataBundle.putString(KeyStrings.ACCOUNT_REMARK.v(), remark);
        if (type == AccountType.TRANSFER) {
            List<String> transferAccountInfo = RuleDataController.getTransferAccounts(ruleNo, getApplicationContext());
            if (!transferAccountInfo.isEmpty()) {
                String exportAccount = transferAccountInfo.get(0);
                String importAccount = transferAccountInfo.get(1);
                dataBundle.putString(KeyStrings.ACCOUNT_EXPORT.v(), exportAccount);
                dataBundle.putString(KeyStrings.ACCOUNT_IMPORT.v(), importAccount);
            }
        }

        return dataBundle;
    }

    /**
     * 加载通知解析规则
     *
     * @return 规则哈希表
     * @throws SQLiteException 规则读取失败引发的异常
     */
    @NonNull
    private HashMap<RuleKey, List<RuleValue>> loadRulesInHashMap() throws SQLiteException {
        Log.d(LogTags.NOTIFICATION_SERVICE.n(), "开始加载通知解析规则");

        HashMap<RuleKey, List<RuleValue>> ruleHashMap = new HashMap<>();
        List<AnalysisRule> ruleList = RuleDataController.loadAnalysisRule(getApplicationContext());
        for (AnalysisRule rule : ruleList) {
            String ruleName = rule.getRuleName();
            long ruleNo = rule.getRuleNo();
            AccountType type = rule.getType();
            String packageName = rule.getPackageName();
            String title = rule.getNotificationTitle();
            String content = rule.getNotificationContent();

            RuleKey key = new RuleKey(packageName, title);
            RuleValue value = new RuleValue(ruleName, content, type, ruleNo);
            List<RuleValue> valueList = ruleHashMap.get(key);
            if (valueList == null) {
                valueList = new ArrayList<>();
                valueList.add(value);
                ruleHashMap.put(key, valueList);
            } else {
                valueList.add(value);
            }
        }

        return ruleHashMap;
    }

    /**
     * 发送错误警告通知
     *
     * @param content 通知内容
     * @param ruleNo  出错的规则编号
     */
    private void sendErrorNotification(String content, long ruleNo) {
        //发送错误提示通知
        int notificationID = NotificationID.AUTO_BOOKKEEPING_ERROR.ordinal() + Math.toIntExact(ruleNo);
        Intent skip2RuleManage = new Intent(getApplicationContext(), AnalysisRuleManageActivity.class);
        PendingIntent pi = PendingIntent.getActivity(
                getApplicationContext(),
                PendingRequestCode.AUTO_BOOKKEEPING_ERROR.ordinal() + Math.toIntExact(ruleNo),
                skip2RuleManage,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        //实例化构建器
        String channelID = ChannelInfo.AUTO_BOOKKEEPING.getId();
        NotificationCompat.Builder builder = new NotificationCompat.Builder(
                getApplicationContext(),
                channelID
        )
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("自动记账出错")
                .setContentText(content)
                .setContentIntent(pi)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .setAutoCancel(true);

        //发送通知
        NotificationHelper.sendNotification(
                notificationID,
                builder,
                getApplicationContext()
        );
    }

    /**
     * 发送通知以提醒用户确认
     *
     * @param dataBundle 自动生成的账单的数据包
     * @param ruleName   触发自动记账的规则名称
     * @param context    上下文
     */
    private void sendNotificationToConfirm(@NonNull Bundle dataBundle, String ruleName, Context context) {
        //生成通知唯一标识符
        int notificationID = dataBundle.hashCode() * 10 + NotificationID.AUTO_BOOKKEEPING_CONFIRM.ordinal();

        //创建保留Action
        NotificationCompat.Action keepAction = createAction(
                context,
                dataBundle,
                ruleName,
                BroadcastActions.ACTION_KEEP.toString(),
                "保留",
                PendingRequestCode.ACCOUNT_KEEP.ordinal(),
                null
        );

        //创建备注输入Action
        RemoteInput remarkRemoteInput = new RemoteInput.Builder(KeyStrings.ACCOUNT_REMARK.v())
                .setLabel("输入备注")
                .build();
        NotificationCompat.Action remarkInputAction = createAction(
                context,
                dataBundle,
                ruleName,
                BroadcastActions.ACTION_INPUT_REMARK.toString(),
                "备注并保留",
                PendingRequestCode.ACCOUNT_INPUT_REMARK.ordinal(),
                remarkRemoteInput
        );

        //创建删除Action
        NotificationCompat.Action deleteAction = createAction(
                context,
                dataBundle,
                ruleName,
                BroadcastActions.ACTION_DELETE.toString(),
                "删除",
                PendingRequestCode.ACCOUNT_DELETE.ordinal(),
                null
        );

        //创建通知被删除的PendingIntent
        Intent notificationDeletedIntent = new Intent(context, AutoBookkeepingActionsReceiver.class);
        notificationDeletedIntent.setAction(BroadcastActions.ACTION_AUTO_BOOKKEEPING_NOTIFICATION_DELETED.toString());
        notificationDeletedIntent.putExtra(
                KeyStrings.NOTIFICATION_ID.v(),
                notificationID
        );
        notificationDeletedIntent.putExtras(dataBundle);                                               //发送流水记录数据包
        notificationDeletedIntent.putExtra(KeyStrings.ANALYSIS_RULE_NAME.v(), ruleName);   //发送规则名称
        int pendingDeletedID = dataBundle.hashCode() * 10 + PendingRequestCode.AUTO_BOOKKEEPING_NOTIFICATION_DELETE.ordinal();
        PendingIntent deletePendingIntent = PendingIntent.getBroadcast(
                context,
                pendingDeletedID,
                notificationDeletedIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        //创建通知构建器
        String channelID = ChannelInfo.AUTO_BOOKKEEPING.getId();
        String content = String.format(Locale.getDefault(), "“%s”产生了一条流水记录", ruleName);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channelID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("自动记账确认")
                .setContentText(content)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .setAutoCancel(true)
                .addAction(keepAction)                  //点击保留按钮
                .addAction(remarkInputAction)           //点击更改备注按钮
                .addAction(deleteAction)                //点击删除按钮
                .setDeleteIntent(deletePendingIntent);  //通知被划走

        //创建通知点击PendingIntent
        int clickBehaviourCode = AutoBookKeepingPreference.getNotificationClickBehaviour(context);
        if (clickBehaviourCode != 0) {
            Intent notificationClickIntent = new Intent(context, AutoBookkeepingActionsReceiver.class);
            notificationClickIntent.setAction(BroadcastActions.ACTION_AUTO_BOOKKEEPING_NOTIFICATION_CLICKED.toString());
            notificationClickIntent.putExtra(
                    KeyStrings.NOTIFICATION_ID.v(),
                    notificationID
            );
            notificationClickIntent.putExtras(dataBundle);                                                  //发送流水记录数据
            notificationClickIntent.putExtra(KeyStrings.ANALYSIS_RULE_NAME.v(), ruleName);      //发送规则名称
            int pendingClickedID = dataBundle.hashCode() * 10 + PendingRequestCode.AUTO_BOOKKEEPING_NOTIFICATION_CLICK.ordinal();
            PendingIntent clickPendingIntent = PendingIntent.getBroadcast(
                    context,
                    pendingClickedID,
                    notificationClickIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );

            //为通知构建器加上点击逻辑
            builder.setContentIntent(clickPendingIntent);
            builder.setAutoCancel(false);
        }

        //发送通知
        NotificationHelper.sendNotification(
                notificationID, //确保多个记录的通知ID不同
                builder,
                context
        );
    }

    /**
     * 创建用于处理自动记账流水记录的通知Action（即通知按钮）
     *
     * @param context     上下文
     * @param dataBundle  自动生成的账单的数据包
     * @param ruleName    触发自动记账的规则名称
     * @param actionID    {@link Intent}的action标识符，用于区别不同的操作，可使用{@link BroadcastActions}中的枚举对象的.toString()方法
     * @param title       按钮的文本
     * @param requestCode PendingIntent的唯一请求代码
     * @param remoteInput 通知输入框（不需要输入框可以为null）
     * @return 通知Action实例，可直接使用.addAction()添加至NotificationCompat.Builder中
     */
    @NonNull
    private NotificationCompat.Action createAction(
            Context context,
            @NonNull Bundle dataBundle,
            String ruleName,
            String actionID,
            String title,
            int requestCode,
            RemoteInput remoteInput
    ) {
        //创建Intent
        Intent intent = new Intent(context, AutoBookkeepingActionsReceiver.class);
        intent.setAction(actionID);
        int notificationID = NotificationID.AUTO_BOOKKEEPING_CONFIRM.ordinal() + dataBundle.hashCode() * 10;
        intent.putExtra(KeyStrings.NOTIFICATION_ID.v(), notificationID);
        intent.putExtras(dataBundle);                                               //发送流水记录数据包
        intent.putExtra(KeyStrings.ANALYSIS_RULE_NAME.v(), ruleName);   //发送规则名称

        //创建PendingIntent
        int dataHash = dataBundle.hashCode();
        PendingIntent pi = PendingIntent.getBroadcast(
                context,
                dataHash * 10 + requestCode,    //为了区分不同流水记录和不同的Action，必须将两个变量整合到一起作为唯一标识符
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_MUTABLE
        );

        //创建构建器实例
        NotificationCompat.Action.Builder builder = new NotificationCompat.Action.Builder(
                R.mipmap.ic_launcher,
                title,
                pi
        );
        if (remoteInput != null) {
            builder.addRemoteInput(remoteInput);
        }

        return builder.build();
    }
}
