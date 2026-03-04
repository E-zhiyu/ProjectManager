package com.manager.assistant.helpers;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.manager.assistant.generic_enums.ChannelInfo;
import com.manager.assistant.generic_enums.NotificationID;

import java.util.ArrayList;
import java.util.List;

public class NotificationHelper {
    /**
     * 创建通知渠道
     *
     * @param context 上下文
     */
    public static void createNotificationChannels(Context context) {
        //获取渠道实例
        List<NotificationChannel> channelList = new ArrayList<>();
        for (ChannelInfo info : ChannelInfo.values()) {
            int importance = info.getImportance();

            if (importance == NotificationManager.IMPORTANCE_DEFAULT) {
                channelList.add(getDefaultChannel(info));
            }
        }

        //创建通知渠道
        NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
        notificationManager.createNotificationChannels(channelList);
    }

    /**
     * 生成一般重要性通知渠道
     *
     * @param info 通知渠道的相关数据
     * @return 重要性为一般的通知渠道实例
     */
    @NonNull
    private static NotificationChannel getDefaultChannel(@NonNull ChannelInfo info) {
        NotificationChannel channel = new NotificationChannel(info.getId(),
                info.getName(),
                NotificationManager.IMPORTANCE_DEFAULT
        );

        channel.setShowBadge(info.isShowBadge());
        channel.setDescription(info.getDescription());

        return channel;
    }

    /**
     * 发送通知
     *
     * @param builder 已经设置好的通知构建器
     * @param context 上下文
     */
    public static void sendNotification(@NonNull NotificationCompat.Builder builder, @NonNull Context context) {
        NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
        notificationManager.notify(NotificationID.BUDGET_AMOUNT.ordinal(), builder.build());
    }
}
