package moe.shizuku.fcmformojo.notification;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.os.Build;
import android.support.annotation.Nullable;
import android.support.v4.util.LongSparseArray;

import moe.shizuku.fcmformojo.FFMSettings;
import moe.shizuku.fcmformojo.model.Chat;
import moe.shizuku.fcmformojo.model.Chat.ChatType;
import moe.shizuku.fcmformojo.model.Message;
import moe.shizuku.fcmformojo.model.PushMessage;
import moe.shizuku.fcmformojo.receiver.NotificationReceiver;

/**
 * 用来放消息内容，处理发通知，通知被点击被删除的东西。
 */
public class NotificationBuilder {

    private NotificationManager mNotificationManager;

    private LongSparseArray<Chat> mMessages;

    private int mMessageCount;
    private int mSendersCount;

    private NotificationBuilderImpl mImpl;

    private NotificationBuilderImpl getImpl() {
        return mImpl;
    }

    private NotificationBuilderImpl createImpl(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            return new NotificationBuilderImplO(context);
        } else {
            return new NotificationBuilderImplBase();
        }
    }

    public NotificationBuilder(Context context) {
        mSendersCount = 0;
        mMessages = new LongSparseArray<>();
        mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        mImpl = createImpl(context);
    }

    public int getSendersCount() {
        return mSendersCount;
    }

    int getMessageCount() {
        return mMessageCount;
    }

    public NotificationManager getNotificationManager() {
        return mNotificationManager;
    }

    /**
     * 插入新消息
     */
    public void addMessage(Context context, PushMessage message) {
        Chat chat = mMessages.get(message.getSenderId());
        if (chat == null) {
            chat = new Chat(message);
            mMessages.put(message.getSenderId(), chat);
        }

        chat.getMessages().add(new Message(message));

        mMessageCount ++;
        mSendersCount = mMessages.size();

        if (shouldNotify(chat)) {
            getImpl().notify(context, chat, this);
        }
    }

    public Chat getChat(long id) {
        return mMessages.get(id);
    }

    private boolean shouldNotify(Chat chat) {
        return chat.isSystem()
                || FFMSettings.getNotification(chat.getType() != ChatType.FRIEND);
    }

    /**
     * 清空全部消息
     *
     */
    public void clearMessages() {
        mMessageCount = 0;
        mSendersCount = 0;
        mMessages.clear();

        mNotificationManager.cancelAll();
    }

    /**
     * 清空消息
     *
     * @param senderId 消息发送人 id
     */
    public void clearMessages(long senderId) {
        Chat chat = mMessages.get(senderId);
        if (chat == null) {
            return;
        }
        mMessageCount -= chat.getMessages().size();
        mMessages.remove(senderId);

        mSendersCount = mMessages.size();

        getImpl().clear(chat, this);
    }

    public static PendingIntent createContentIntent(Context context, int requestCode, @Nullable Chat chat) {
        long id = chat == null ? 0 : chat.getId();
        if (id == -1 || id == -3) {
            return null;
        }
        return PendingIntent.getBroadcast(context, requestCode, NotificationReceiver.contentIntent(chat), PendingIntent.FLAG_UPDATE_CURRENT);
    }

    public static PendingIntent createDeleteIntent(Context context, int requestCode, @Nullable Chat chat) {
        long id = chat == null ? 0 : chat.getId();
        if (id == -1 || id == -3) {
            return null;
        }
        return PendingIntent.getBroadcast(context, requestCode, NotificationReceiver.deleteIntent(chat), PendingIntent.FLAG_UPDATE_CURRENT);
    }
}
