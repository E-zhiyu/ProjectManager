package com.manager.assistant.helpers;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.media.AudioAttributes;
import android.media.RingtoneManager;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.manager.assistant.auxiliary.enums.ChannelInfo;

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

            if (importance == NotificationManager.IMPORTANCE_HIGH) {
                channelList.add(getImportanceHighChannel(info));
            }
        }

        //创建通知渠道
        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.createNotificationChannels(channelList);
    }

    /**
     * 生成高重要性通知渠道
     *
     * @param info 通知渠道的相关数据
     * @return 重要性为高的通知渠道实例
     */
    @NonNull
    private static NotificationChannel getImportanceHighChannel(@NonNull ChannelInfo info) {
        NotificationChannel channel = new NotificationChannel(
                info.getId(),
                info.getName(),
                NotificationManager.IMPORTANCE_HIGH
        );

        //配置角标、描述
        channel.setShowBadge(true);
        channel.setDescription(info.getDescription());

        //声音
        channel.setSound(
                RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION),
                new AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                        .build()
        );

        //震动
        channel.enableVibration(true);
        channel.setVibrationPattern(new long[]{0, 200, 100, 200});

        //灯光提示
        channel.enableLights(true);

        return channel;
    }

    /**
     * 发送通知
     *
     * @param notificationID 该通知的唯一标识符，如果有相同标识符的通知，则会直接覆盖更新。可以使用{@link NotificationHelper}中的枚举常数作为标识符
     * @param builder        已经设置好的通知构建器
     * @param context        上下文
     */
    public static void sendNotification(
            int notificationID,
            @NonNull NotificationCompat.Builder builder,
            @NonNull Context context
    ) {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
            //兼容性
            builder.setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setDefaults(NotificationCompat.DEFAULT_ALL);

            //发送通知
            NotificationManagerCompat.from(context).notify(notificationID, builder.build());
        }
    }
}
