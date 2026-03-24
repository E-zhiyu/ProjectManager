package com.manager.assistant.automation.broadcast.bookkeeping;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.RemoteInput;

import com.manager.assistant.R;
import com.manager.assistant.automation.broadcast.BroadcastActions;
import com.manager.assistant.data.controllers.AccountDataController;
import com.manager.assistant.generic_enums.ChannelInfo;
import com.manager.assistant.generic_enums.KeyValueStrings;
import com.manager.assistant.helpers.NotificationHelper;

import java.util.Locale;

public class AutoBookkeepingActionsReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, @NonNull Intent intent) {
        Bundle dataBundle = intent.getExtras();
        String ruleName = intent.getStringExtra(KeyValueStrings.ANALYSIS_RULE_NAME.getValue()); //规则名称
        int notificationID = intent.getIntExtra(KeyValueStrings.NOTIFICATION_ID.getValue(), -1);
        String action = intent.getAction();
        if (action == null || dataBundle == null || notificationID == -1) {
            return;
        }

        //根据action执行接下来的逻辑
        if (action.equals(BroadcastActions.ACTION_INPUT_REMARK.toString())) {
            Bundle inputResults = RemoteInput.getResultsFromIntent(intent);
            if (inputResults == null) {
                return;
            }

            String remark = inputResults.getString(KeyValueStrings.ACCOUNT_REMARK.getValue());
            if (remark == null) {
                return;
            }

            onRemarkInput(context, notificationID, remark, dataBundle, ruleName);
        } else if (action.equals(BroadcastActions.ACTION_KEEP.toString())) {
            onAccountKept(context, notificationID, dataBundle, ruleName);
        } else if (action.equals(BroadcastActions.ACTION_DELETE.toString())) {
            onAccountDeleted(context, notificationID, ruleName);
        } else if (action.equals(BroadcastActions.ACTION_NOTIFICATION_DELETED.toString())) {
            onNotificationDeleted(context, dataBundle);
        }
    }

    /**
     * 保留按钮点击回调
     *
     * @param context        上下文
     * @param notificationID 触发该接收器的通知的ID
     * @param dataBundle     流水数据包
     * @param ruleName       触发自动记账的规则名称
     */
    private void onAccountKept(Context context, int notificationID, @NonNull Bundle dataBundle, String ruleName) {
        //创建通知构建器
        String content = String.format(Locale.getDefault(), "已保留由“%s”触发的记录", ruleName);
        String channelID = ChannelInfo.AUTO_BOOKKEEPING.getId();
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channelID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("保留成功")
                .setContentText(content)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_CALL)
                .setTimeoutAfter(1500)
                .setAutoCancel(true);

        //发送通知
        NotificationHelper.sendNotification(
                notificationID,
                builder,
                context
        );

        writeDataAndBroadcast(dataBundle, context);
    }

    /**
     * 备注输入回调
     *
     * @param context        上下文
     * @param notificationID 触发该接收器的通知的ID
     * @param remark         用户输入的备注
     * @param dataBundle     流水数据包
     * @param ruleName       触发自动记账的规则名称
     */
    private void onRemarkInput(Context context, int notificationID, String remark, @NonNull Bundle dataBundle, String ruleName) {
        //创建通知构建器
        String content = String.format(Locale.getDefault(), "备注成功并保留由“%s”触发的记录", ruleName);
        String channelID = ChannelInfo.AUTO_BOOKKEEPING.getId();
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channelID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("备注输入成功")
                .setContentText(content)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_CALL)
                .setTimeoutAfter(1500)
                .setAutoCancel(true);

        //发送通知
        NotificationHelper.sendNotification(
                notificationID,
                builder,
                context
        );

        //修改数据包中的备注
        dataBundle.putString(KeyValueStrings.ACCOUNT_REMARK.getValue(), remark);

        writeDataAndBroadcast(dataBundle, context);
    }

    /**
     * 删除按钮点击回调
     *
     * @param context        上下文
     * @param notificationID 触发该接收器的通知的ID
     * @param ruleName       触发自动记账的规则名称
     */
    private void onAccountDeleted(Context context, int notificationID, String ruleName) {
        //创建通知构建器
        String content = String.format(Locale.getDefault(), "已删除由“%s”触发的记录", ruleName);
        String channelID = ChannelInfo.AUTO_BOOKKEEPING.getId();
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channelID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("删除成功")
                .setContentText(content)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_CALL)
                .setTimeoutAfter(1500)
                .setAutoCancel(true);

        //发送通知
        NotificationHelper.sendNotification(
                notificationID,
                builder,
                context
        );
    }

    /**
     * 通知被划走回调
     *
     * @param context    上下文
     * @param dataBundle 流水记录数据包
     */
    private void onNotificationDeleted(@NonNull Context context, Bundle dataBundle) {
        //发送本地广播以保存数据
        Intent accountAdded = new Intent(BroadcastActions.ACTION_RUNNING_ACCOUNT_UPDATED.toString());
        accountAdded.putExtras(dataBundle);
        context.sendBroadcast(accountAdded);
    }

    /**
     * 将流水数据写入数据库并发送本地广播更新UI
     *
     * @param dataBundle 流水记录数据包
     * @param context    上下文
     */
    private void writeDataAndBroadcast(Bundle dataBundle, Context context) {
        //将数据写入数据库
        long rno = AccountDataController.saveNewAccount(dataBundle, context);
        dataBundle.putLong(KeyValueStrings.ACCOUNT_NO.getValue(), rno);

        //发送本地广播以保存数据
        Intent accountAdded = new Intent(BroadcastActions.ACTION_RUNNING_ACCOUNT_UPDATED.toString());
        accountAdded.putExtras(dataBundle);
        context.sendBroadcast(accountAdded);
    }
}
