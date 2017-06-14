package moe.shizuku.fcmformojo.notification;

import android.annotation.TargetApi;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.media.AudioAttributes;
import android.media.RingtoneManager;
import android.os.Build;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;

import java.util.ArrayList;
import java.util.List;

import moe.shizuku.fcmformojo.R;
import moe.shizuku.fcmformojo.model.Chat;

/**
 * Created by rikka on 2017/6/13.
 */

@TargetApi(Build.VERSION_CODES.O)
public class NotificationBuilderImplO extends NotificationBuilderImplN {

    public NotificationBuilderImplO(Context context) {
        super();

        // TODO let user select value first
        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        List<NotificationChannel> channels = new ArrayList<>();
        NotificationChannel channel;
        channel = new NotificationChannel("friend_message_channel",
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

        channel = new NotificationChannel("group_message_channel",
                context.getString(R.string.notification_channel_group_message),
                NotificationManager.IMPORTANCE_LOW);
        channel.enableLights(true);
        channel.setLightColor(context.getColor(R.color.colorNotification));
        channel.enableVibration(false);
        channel.setShowBadge(false);
        channels.add(channel);

        notificationManager.createNotificationChannels(channels);
    }

    /*@Override
    public NotificationCompat.Builder getBuilder(Context context, @Nullable Chat chat) {
        NotificationCompat.Builder builder = super.getBuilder(context, chat);
        bu
    }*/
}
