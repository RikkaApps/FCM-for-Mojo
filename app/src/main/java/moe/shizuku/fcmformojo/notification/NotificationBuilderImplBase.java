package moe.shizuku.fcmformojo.notification;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.service.notification.StatusBarNotification;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.RemoteInput;

import moe.shizuku.fcmformojo.FFMApplication;
import moe.shizuku.fcmformojo.FFMSettings;
import moe.shizuku.fcmformojo.R;
import moe.shizuku.fcmformojo.app.MessagingStyle;
import moe.shizuku.fcmformojo.model.Chat;
import moe.shizuku.fcmformojo.model.Message;
import moe.shizuku.fcmformojo.profile.Profile;
import moe.shizuku.fcmformojo.receiver.NotificationReceiver;

import static moe.shizuku.fcmformojo.FFMStatic.NOTIFICATION_CHANNEL_FRIENDS;
import static moe.shizuku.fcmformojo.FFMStatic.NOTIFICATION_CHANNEL_GROUPS;
import static moe.shizuku.fcmformojo.FFMStatic.NOTIFICATION_ID_GROUP_SUMMARY;
import static moe.shizuku.fcmformojo.FFMStatic.NOTIFICATION_ID_SYSTEM;
import static moe.shizuku.fcmformojo.FFMStatic.NOTIFICATION_INPUT_KEY;

/**
 * Created by Rikka on 2016/9/18.
 */
class NotificationBuilderImplBase extends NotificationBuilderImpl {

    private static final String TAG = "NotificationBuilderImplBase";

    private static final String GROUP_KEY = "messages";

    private static final int MAX_MESSAGES = 8;

    @Override
    void notify(Context context, Chat chat, NotificationBuilder nb) {
        int id = (int) chat.getUid();

        if (chat.isSystem()) {
            id = (int) chat.getId();
        }

        notifyGroupSummary(context, chat, nb);

        NotificationCompat.Builder builder = createBuilder(context, chat)
                .setLargeIcon(chat.getIcon(context))
                .setContentTitle(chat.getName())
                .setContentText(chat.getLastMessage().getContent())
                .setGroup(chat.isSystem() ? null : GROUP_KEY)
                .setGroupSummary(false)
                .setShowWhen(true)
                .setWhen(chat.getLastMessage().getTimestamp())
                .setStyle(getStyle(context, chat))
                .setContentIntent(NotificationBuilder.createContentIntent(context, id, chat))
                .setDeleteIntent(NotificationBuilder.createDeleteIntent(context, id, chat))
                .setCategory(NotificationCompat.CATEGORY_MESSAGE);

        if (!chat.isSystem()) {
            builder.addAction(createReplyAction(context, id, chat));
        } else {
            id = NOTIFICATION_ID_SYSTEM;
        }

        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(id, builder.build());
    }

    private static NotificationCompat.Style getStyle(Context context, Chat chat) {
        if (chat.isFriend() || chat.isSystem()) {
            NotificationCompat.BigTextStyle style = new NotificationCompat.BigTextStyle();
            style.setBigContentTitle(chat.getName());

            StringBuilder sb = new StringBuilder();
            for (int i = chat.getMessages().size() - MAX_MESSAGES, count = 0; i < chat.getMessages().size() && count <= 8; i++, count ++) {
                if (i < 0) {
                    continue;
                }

                Message message = chat.getMessages().get(i);
                sb.append(message.getContent()).append('\n');
            }
            style.bigText(sb.toString().trim());
            style.setSummaryText(context.getString(R.string.message_format, chat.getMessages().size()));

            return style;
        } else {
            MessagingStyle style = new MessagingStyle(chat.getName());
            style.setConversationTitle(chat.getName());

            for (int i = chat.getMessages().size() - MAX_MESSAGES, count = 0; i < chat.getMessages().size() && count <= 8; i++, count ++) {
                if (i < 0) {
                    continue;
                }

                Message message = chat.getMessages().get(i);
                style.addMessage(message.getContent(), message.getTimestamp(), message.getSender());
            }

            style.setSummaryText(context.getString(R.string.message_format, chat.getMessages().size()));

            return style;
        }
    }

    /**
     * 创建通知的回复动作。
     *
     * @param context Context
     * @param id 唯一 id，也会被作为 PendingIntent 的 requestId
     * @param chat 对应的 Chat
     * @return NotificationCompat.Action
     */
    private static NotificationCompat.Action createReplyAction(Context context, int id, Chat chat) {
        Intent intent = NotificationReceiver.replyIntent(chat);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, id, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        String replyLabel = context.getString(R.string.reply, chat.getName());
        RemoteInput remoteInput = new RemoteInput.Builder(NOTIFICATION_INPUT_KEY)
                .setLabel(replyLabel)
                .build();

        return new NotificationCompat.Action.Builder(R.drawable.ic_reply_24dp, replyLabel, pendingIntent)
                .addRemoteInput(remoteInput)
                .build();
    }

    /**
     * 分组消息的头部
     **/
    private void notifyGroupSummary(Context context, Chat chat, NotificationBuilder nb) {
        if (chat.isSystem()) {
            return;
        }

        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        NotificationCompat.Builder builder = createBuilder(context, null)
                .setSubText(String.format(context.getString(R.string.messages_format), nb.getMessageCount(), nb.getSendersCount()))
                .setShowWhen(true)
                .setWhen(System.currentTimeMillis())
                .setGroup(GROUP_KEY)
                .setGroupSummary(true)
                .setContentIntent(NotificationBuilder.createContentIntent(context, 0, null))
                .setDeleteIntent(NotificationBuilder.createDeleteIntent(context, 0, null));

        notificationManager.notify(NOTIFICATION_ID_GROUP_SUMMARY, builder.build());
    }

    /**
     * 返回一个设置了 SmallIcon 等任何通知都相同的属性的 NotificationCompat.Builder
     * 同时设置浮动通知 / LED / 震动等
     *
     * @param chat 聊天内容
     *
     * @return NotificationCompat.Builder
     **/
    public NotificationCompat.Builder createBuilder(Context context, @Nullable Chat chat) {
        Profile profile = FFMSettings.getProfile();

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_GROUPS)
                .setColor(context.getColor(profile.getNotificationColor()))
                .setSmallIcon(profile.getNotificationIcon())
                .setVisibility(Notification.VISIBILITY_PRIVATE);

        if (FFMApplication.get(context).isSystem()) {
            Bundle extras = new Bundle();
            extras.putString("android.substName", context.getString(profile.getDisplayName()));
            builder.addExtras(extras);
        }

        if (chat == null) {
            return builder;
        }

        // @ 消息当作好友消息处理
        boolean group = chat.isGroup() && !chat.getLastMessage().isAt();
        if (!group) {
            builder.setChannelId(NOTIFICATION_CHANNEL_FRIENDS);
        }

        // sound
        builder.setSound(FFMSettings.getSound(group));

        // heads-up
        int priority = FFMSettings.getPriority(group);
        builder.setPriority(priority);
        if (priority >= NotificationCompat.PRIORITY_HIGH || chat.getLastMessage().isAt()) {
            builder.setVibrate(new long[0]);
        }

        // vibrate
        int vibrate = FFMSettings.getVibrate(group);
        if (vibrate != 0) {
            switch (vibrate) {
                case 1:
                    builder.setVibrate(null);
                    builder.setDefaults(NotificationCompat.DEFAULT_VIBRATE);
                    break;
                case 2:
                    builder.setVibrate(new long[]{0, 100, 0, 100});
                    break;
                case 3:
                    builder.setVibrate(new long[]{0, 1000});
                    break;
            }
        }

        // lights
        if (FFMSettings.getLight(group)
                && priority >= NotificationCompat.PRIORITY_DEFAULT) {
            builder.setLights(context.getColor(R.color.colorNotification), 1000, 1000);
        }

        return builder;
    }

    @Override
    void clear(Chat chat, NotificationBuilder nb) {
        nb.getNotificationManager().cancel((int) chat.getUid());

        boolean clearGroup = true;
        for (StatusBarNotification sbn : nb.getNotificationManager().getActiveNotifications()) {
            if (sbn.getId() != NOTIFICATION_ID_SYSTEM
                    && sbn.getId() != NOTIFICATION_ID_GROUP_SUMMARY) {
                clearGroup = false;
            }
        }
        if (clearGroup) {
            nb.getNotificationManager().cancel(NOTIFICATION_ID_GROUP_SUMMARY);
        }
    }
}
