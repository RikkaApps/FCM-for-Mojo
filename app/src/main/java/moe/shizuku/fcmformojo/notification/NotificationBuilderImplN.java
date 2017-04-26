package moe.shizuku.fcmformojo.notification;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.service.notification.StatusBarNotification;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.RemoteInput;

import java.io.File;

import moe.shizuku.fcmformojo.FFMApplication;
import moe.shizuku.fcmformojo.FFMSettings;
import moe.shizuku.fcmformojo.R;
import moe.shizuku.fcmformojo.app.MessagingStyle;
import moe.shizuku.fcmformojo.model.Chat;
import moe.shizuku.fcmformojo.receiver.NotificationReceiver;
import moe.shizuku.fcmformojo.utils.FileUtils;

/**
 * Created by Rikka on 2016/9/18.
 */
final class NotificationBuilderImplN extends NotificationBuilderImpl {

    private static final String TAG = "NotificationBuilderImplN";

    private static final String KEY_TEXT_REPLY = "reply";
    private static final String GROUP_KEY = "messages";
    private static final int GROUP_ID = -10000;
    private static final int SYSTEM_MESSAGE_ID = -10001;

    private static final int MAX_MESSAGES = 8;

    public NotificationBuilderImplN() {
    }

    @Override
    void notify(Context context, Chat chat, NotificationBuilder nb) {
        int id = (int) chat.getUid();

        if (chat.isSystemMessage()) {
            id = (int) chat.getId();
        }

        notifyGroupSummary(context, chat, nb);

        NotificationCompat.Builder builder = getBuilder(context, chat)
                .setLargeIcon(getLargeIcon(context, chat, nb))
                .setContentTitle(chat.getName())
                .setContentText(chat.getLastMessage().getContent())
                .setGroup(chat.isSystemMessage() ? null : GROUP_KEY)
                .setGroupSummary(false)
                .setShowWhen(true)
                .setWhen(chat.getLastMessage().getTimestamp())
                .setStyle(getStyle(context, chat))
                .setContentIntent(NotificationBuilder.getContentIntent(context, id, chat, false))
                .setDeleteIntent(NotificationBuilder.getDeleteIntent(context, id, chat, false))
                .setCategory(NotificationCompat.CATEGORY_MESSAGE);

        if (!chat.isSystemMessage()) {
            builder.addAction(getReplyAction(context, id, chat));
        } else {
            id = SYSTEM_MESSAGE_ID;
        }

        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(id, builder.build());
    }

    private static Bitmap getLargeIcon(Context context, Chat chat, NotificationBuilder nb) {
        if (chat.isSystemMessage()) {
            return null;
        }

        if (chat.getType() == 1) {
            File file = FileUtils.getCacheFile(context, "/head/" + chat.getLastMessage().getSenderUid());
            if (file.exists()) {
                return BitmapFactory.decodeFile(file.getAbsolutePath());
            } else {
                return nb.getLargeIcon(context, chat.getName().hashCode(), false);
            }
        } else if (chat.getType() != 1) {
            return nb.getLargeIcon(context, chat.getName().hashCode(), true);
        }
        return null;
    }

    private static NotificationCompat.Style getStyle(Context context, Chat chat) {
        if (chat.getType() == 1 || chat.isSystemMessage()) {
            NotificationCompat.BigTextStyle style = new NotificationCompat.BigTextStyle();
            style.setBigContentTitle(chat.getName());

            StringBuilder sb = new StringBuilder();
            for (int i = chat.getMessages().size() - MAX_MESSAGES, count = 0; i < chat.getMessages().size() && count <= 8; i++, count ++) {
                if (i < 0) {
                    continue;
                }

                Chat.Message message = chat.getMessages().get(i);
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

                Chat.Message message = chat.getMessages().get(i);
                style.addMessage(message.getContent(), message.getTimestamp(), message.getSender());
            }

            style.setSummaryText(context.getString(R.string.message_format, chat.getMessages().size()));

            return style;
        }
    }

    private static NotificationCompat.Action getReplyAction(Context context, int id, Chat chat) {
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, id, NotificationReceiver.replyIntent(chat.getId(), chat.getType()), PendingIntent.FLAG_UPDATE_CURRENT);
        String replyLabel = context.getString(R.string.reply, chat.getName());
        RemoteInput remoteInput = new RemoteInput.Builder(KEY_TEXT_REPLY)
                .setLabel(replyLabel)
                .build();

        return new NotificationCompat.Action.Builder(R.drawable.ic_reply_24dp,
                        replyLabel, pendingIntent)
                        .addRemoteInput(remoteInput)
                        .build();
    }

    /**
     * 分组消息的头部
     **/
    private void notifyGroupSummary(Context context, Chat chat, NotificationBuilder nb) {
        if (chat.isSystemMessage()) {
            return;
        }

        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        NotificationCompat.Builder builder = getBuilder(context, null)
                .setSubText(String.format(context.getString(R.string.messages_format), nb.getMessageCount(), nb.getSendersCount()))
                .setShowWhen(true)
                .setWhen(System.currentTimeMillis())
                .setGroup(GROUP_KEY)
                .setGroupSummary(true)
                .setContentIntent(NotificationBuilder.getContentIntent(context, 0, null, true))
                .setDeleteIntent(NotificationBuilder.getDeleteIntent(context, 0, null, true));

        notificationManager.notify(GROUP_ID, builder.build());
    }

    /**
     * 返回一个设置了 SmallIcon 等任何通知都相同的属性的 NotificationCompat.Builder
     * 同时设置浮动通知 / LED / 震动等
     *
     * @param chat 聊天内容
     *
     * @return NotificationCompat.Builder
     **/
    public static NotificationCompat.Builder getBuilder(Context context, @Nullable Chat chat) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context)
                .setColor(context.getColor(R.color.colorNotification))
                .setSmallIcon(R.drawable.ic_noti_24dp)
                .setVisibility(Notification.VISIBILITY_PRIVATE);

        if (FFMApplication.get(context).isSystem()) {
            Bundle extras = new Bundle();
            extras.putString("android.substName", "QQ");
            builder.addExtras(extras);
        }

        if (chat == null) {
            return builder;
        }

        // @消息当作好友消息处理
        boolean group = chat.getType() != 1 && !chat.getLastMessage().isAt();

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
            if (sbn.getId() != SYSTEM_MESSAGE_ID
                    && sbn.getId() != GROUP_ID) {
                clearGroup = false;
            }
        }
        if (clearGroup) {
            nb.getNotificationManager().cancel(GROUP_ID);
        }
    }
}
