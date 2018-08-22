package moe.shizuku.fcmformojo.notification;

import android.annotation.TargetApi;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.media.AudioAttributes;
import android.media.RingtoneManager;
import android.os.Build;

import java.util.ArrayList;
import java.util.List;

import moe.shizuku.fcmformojo.R;

import static moe.shizuku.fcmformojo.FFMStatic.NOTIFICATION_CHANNEL_FRIENDS;
import static moe.shizuku.fcmformojo.FFMStatic.NOTIFICATION_CHANNEL_GROUPS;
import static moe.shizuku.fcmformojo.FFMStatic.NOTIFICATION_CHANNEL_PROGRESS;
import static moe.shizuku.fcmformojo.FFMStatic.NOTIFICATION_CHANNEL_SERVER;

/**
 * Created by rikka on 2017/6/13.
 */

@TargetApi(Build.VERSION_CODES.O)
class NotificationBuilderImplO extends NotificationBuilderImplBase {

    NotificationBuilderImplO(Context context) {
        super();

        // TODO let user select value first
        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        List<NotificationChannel> channels = new ArrayList<>();
        NotificationChannel channel;
        channel = new NotificationChannel(NOTIFICATION_CHANNEL_FRIENDS,
                context.getString(R.string.notification_channel_friend_message),
                NotificationManager.IMPORTANCE_HIGH);
        channel.enableLights(true);
        channel.setLightColor(context.getColor(R.color.colorNotification));
        channel.enableVibration(true);
        channel.setSound(
                RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION),
                new AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                        .build()
        );
        channel.setVibrationPattern(new long[]{0, 100, 0, 100});
        channel.setShowBadge(false);
        channels.add(channel);

        channel = new NotificationChannel(NOTIFICATION_CHANNEL_GROUPS,
                context.getString(R.string.notification_channel_group_message),
                NotificationManager.IMPORTANCE_LOW);
        channel.enableLights(true);
        channel.setLightColor(context.getColor(R.color.colorNotification));
        channel.enableVibration(false);
        channel.setShowBadge(false);
        channels.add(channel);

        channel = new NotificationChannel(NOTIFICATION_CHANNEL_PROGRESS,
                context.getString(R.string.notification_channel_progress_message),
                NotificationManager.IMPORTANCE_LOW);
        channel.enableLights(false);
        channel.enableVibration(false);
        channel.setShowBadge(false);
        channels.add(channel);

        channel = new NotificationChannel(NOTIFICATION_CHANNEL_SERVER,
                context.getString(R.string.notification_channel_server_message),
                NotificationManager.IMPORTANCE_HIGH);
        channel.enableLights(true);
        channel.setLightColor(context.getColor(R.color.colorServerNotification));
        channel.setVibrationPattern(new long[]{0, 100, 0, 100});
        channel.setShowBadge(false);
        channels.add(channel);

        if (notificationManager != null) {
            notificationManager.deleteNotificationChannel("group_summary_channel");
            notificationManager.createNotificationChannels(channels);
        }
    }
}
