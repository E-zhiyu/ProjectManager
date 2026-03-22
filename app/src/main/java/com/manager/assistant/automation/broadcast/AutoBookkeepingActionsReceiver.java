package com.manager.assistant.automation.broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.RemoteInput;

import com.manager.assistant.R;
import com.manager.assistant.data.classes.running_account.RunningAccountBase;
import com.manager.assistant.generic_enums.ChannelInfo;
import com.manager.assistant.generic_enums.KeyValueStrings;
import com.manager.assistant.generic_enums.NotificationID;
import com.manager.assistant.helpers.NotificationHelper;

import java.util.Locale;

public class AutoBookkeepingActionsReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, @NonNull Intent intent) {
        long rno = intent.getLongExtra(KeyValueStrings.RNO.getValue(), 0);          //流水编号
        String ruleName = intent.getStringExtra(KeyValueStrings.ANALYSIS_RULE_NAME.getValue()); //规则名称
        String action = intent.getAction();
        if (action == null || rno == 0) {
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

            onRemarkInput(context, remark, rno, ruleName);
        } else if (action.equals(BroadcastActions.ACTION_KEEP.toString())) {
            onAccountKept(context, rno, ruleName);
        } else if (action.equals(BroadcastActions.ACTION_DELETE.toString())) {
            onAccountDeleted(context, rno, ruleName);
        }
    }

    /**
     * 保留按钮点击回调
     *
     * @param context  上下文
     * @param rno      流水编号
     * @param ruleName 触发自动记账的规则名称
     */
    private void onAccountKept(Context context, long rno, String ruleName) {
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
        int notificationID = NotificationID.AUTO_BOOKKEEPING_CONFIRM.ordinal() + (int) rno * 10;
        NotificationHelper.sendNotification(
                notificationID,
                builder,
                context
        );
    }

    /**
     * 备注输入回调
     *
     * @param context  上下文
     * @param remark   用户输入的备注
     * @param rno      流水编号
     * @param ruleName 触发自动记账的规则名称
     */
    private void onRemarkInput(Context context, String remark, long rno, String ruleName) {
        //修改数据
        RunningAccountBase.updateRemark(remark, rno, context);

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
        int notificationID = NotificationID.AUTO_BOOKKEEPING_CONFIRM.ordinal() + (int) rno * 10;
        NotificationHelper.sendNotification(
                notificationID,
                builder,
                context
        );
    }

    /**
     * 删除按钮点击回调
     *
     * @param context  上下文
     * @param rno      流水编号
     * @param ruleName 触发自动记账的规则名称
     */
    private void onAccountDeleted(Context context, long rno, String ruleName) {
        //修改数据
        RunningAccountBase.deleteAccount(rno, context);

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
        int notificationID = NotificationID.AUTO_BOOKKEEPING_CONFIRM.ordinal() + (int) rno * 10;
        NotificationHelper.sendNotification(
                notificationID,
                builder,
                context
        );
    }
}
