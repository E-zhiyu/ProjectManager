package com.sly.coffer.auxiliary.enums;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.media.AudioAttributes;
import android.media.RingtoneManager;
import android.net.Uri;

import java.util.function.Function;

public enum ChannelInfo {
    BUDGET_BALANCE(
            "budget_balance_channel",
            info -> {
                NotificationChannel channel = new NotificationChannel(
                        info.getId(),
                        "预算提醒",
                        NotificationManager.IMPORTANCE_HIGH
                );

                channel.enableLights(true);                             //灯光
                channel.setDescription("当预算余额低于阈值时发送提醒通知"); //描述

                //声音
                Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                AudioAttributes audioAttributes = new AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                        .build();
                channel.setSound(defaultSoundUri, audioAttributes);

                return channel;
            }
    ),
    AUTO_BOOKKEEPING(
            "auto_bookkeeping_channel",
            info -> {
                NotificationChannel channel = new NotificationChannel(
                        info.getId(),
                        "自动记账",
                        NotificationManager.IMPORTANCE_HIGH
                );

                channel.enableLights(true);                             //灯光
                channel.setDescription("触发自动记账后发送确认通知");        //描述

                //声音
                Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                AudioAttributes audioAttributes = new AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                        .build();
                channel.setSound(defaultSoundUri, audioAttributes);

                return channel;
            }
    );
    private final String id;
    private final Function<ChannelInfo, NotificationChannel> channelBuilder;

    ChannelInfo(String id, Function<ChannelInfo, NotificationChannel> channelBuilder) {
        this.id = id;
        this.channelBuilder = channelBuilder;
    }

    /**
     * 获取通知渠道实例
     *
     * @return 通知渠道实例
     */
    public NotificationChannel getNotificationChannel() {
        return channelBuilder.apply(this);
    }

    public String getId() {
        return id;
    }
}
