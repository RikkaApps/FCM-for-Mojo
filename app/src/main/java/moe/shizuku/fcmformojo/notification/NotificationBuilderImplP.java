package moe.shizuku.fcmformojo.notification;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.Person;
import android.support.v4.graphics.drawable.IconCompat;

import moe.shizuku.fcmformojo.R;
import moe.shizuku.fcmformojo.app.MessagingStyle;
import moe.shizuku.fcmformojo.model.Chat;
import moe.shizuku.fcmformojo.model.Message;
import moe.shizuku.fcmformojo.model.openqq.User;

import static moe.shizuku.fcmformojo.FFMStatic.NOTIFICATION_MAX_MESSAGES;

public class NotificationBuilderImplP extends NotificationBuilderImplO {

    NotificationBuilderImplP(Context context) {
        super(context);
    }

    @Override
    public Bitmap loadLargeIcon(Context context, Chat chat) {
        if (chat.isFriend()) {
            return null;
        }
        return super.loadLargeIcon(context, chat);
    }

    @Override
    public NotificationCompat.Style createStyle(Context context, Chat chat) {
        MessagingStyle style = new MessagingStyle(new Person.Builder()
                .setName(context.getString(R.string.you))
                .setIcon(IconCompat.createWithBitmap(UserIcon.requestIcon(context, User.getSelf().getUid(), Chat.ChatType.FRIEND)))
                .build());
        style.setConversationTitle(chat.getName());
        style.setGroupConversation(chat.isGroup());

        for (int i = chat.getMessages().size() - NOTIFICATION_MAX_MESSAGES, count = 0; i < chat.getMessages().size() && count <= 8; i++, count++) {
            if (i < 0) {
                continue;
            }

            Message message = chat.getMessages().get(i);
            User sender = message.getSenderUser();

            IconCompat icon = null;
            Bitmap bitmap = UserIcon.getIcon(context, sender.getUid(), Chat.ChatType.FRIEND);
            if (bitmap != null) {
                icon = IconCompat.createWithBitmap(bitmap);
            }

            Person person = null;
            if (message.getSenderUser() != User.getSelf()) {
                person = new Person.Builder()
                        .setKey(sender.getName())
                        .setName(sender.getName())
                        .setIcon(icon)
                        .build();
            }

            style.addMessage(message.getContent(context), message.getTimestamp(), person);
        }

        style.setSummaryText(context.getString(R.string.notification_messages, chat.getMessages().getSize()));

        return style;
    }
}
