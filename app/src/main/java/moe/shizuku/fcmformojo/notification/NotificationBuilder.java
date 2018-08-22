package moe.shizuku.fcmformojo.notification;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.os.Build;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.util.LongSparseArray;

import moe.shizuku.fcmformojo.FFMApplication;
import moe.shizuku.fcmformojo.model.Chat;
import moe.shizuku.fcmformojo.model.Message;
import moe.shizuku.fcmformojo.model.PushChat;
import moe.shizuku.fcmformojo.model.openqq.User;
import moe.shizuku.fcmformojo.receiver.FFMBroadcastReceiver;
import moe.shizuku.fcmformojo.utils.ChatMessagesList;

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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            return new NotificationBuilderImplP(context);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
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

    public NotificationCompat.Builder createBuilder(Context context, @Nullable Chat chat) {
        return getImpl().createBuilder(context, chat);
    }

    /**
     * 插入新消息
     */
    public void addMessage(Context context, PushChat pushChat) {
        if (pushChat.isSystem()) {
            getImpl().notifySystem(context, pushChat, this);
            return;
        }

        long id = pushChat.getUniqueId();

        Chat chat = mMessages.get(id);
        if (chat == null) {
            pushChat.setMessages(new ChatMessagesList());
        } else {
            pushChat.setMessages(chat.getMessages());
            pushChat.setIcon(chat.getIcon());
        }
        chat = pushChat;

        boolean clear = false;
        for (Message message : chat.getMessages()) {
            if (message.getSenderUser().getUid() == User.getSelf().getUid()) {
                clear = true;
                break;
            }
        }
        if (clear) {
            mMessageCount -= chat.getMessages().size();
            mMessageCount = mMessageCount > 0 ? mMessageCount : 0;
            chat.getMessages().clear();
        }

        chat.getMessages().add(chat.getLatestMessage());

        mMessages.put(id, chat);

        mMessageCount ++;
        mSendersCount = mMessages.size();

        notify(context, chat);
    }

    public void addMessage(Context context, long id, Message message) {
        Chat chat = mMessages.get(id);
        if (chat == null || chat.isSystem()) {
            return;
        }

        chat.getMessages().add(message);

        mMessageCount ++;
        mSendersCount = mMessages.size();

        notify(context, chat);
    }

    private void notify(Context context, Chat chat) {
        if (shouldNotify(context)) {
            getImpl().notify(context, chat, this);
        }
    }

    private boolean shouldNotify(Context context) {
        if (FFMApplication.get(context).isTop()) {
            clearMessages();
            return false;
        }

        return true;
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

    public void clearMessages(long id, boolean removeNotification) {
        Chat chat = mMessages.get(id);
        if (chat == null) {
            return;
        }
        mMessageCount -= chat.getMessages().size();
        mSendersCount = mMessages.size();

        chat.getMessages().clear();

        if (removeNotification)
            getImpl().clear(chat, this);
    }

    /**
     * 清空消息
     *
     * @param id 唯一的 id
     */
    public void clearMessages(long id) {
        clearMessages(id, true);
    }

    public static PendingIntent createContentIntent(Context context, int requestCode, @Nullable Chat chat) {
        return PendingIntent.getBroadcast(context, requestCode, FFMBroadcastReceiver.contentIntent(chat), PendingIntent.FLAG_UPDATE_CURRENT);
    }

    public static PendingIntent createDeleteIntent(Context context, int requestCode, @Nullable Chat chat) {
        return PendingIntent.getBroadcast(context, requestCode, FFMBroadcastReceiver.deleteIntent(chat), PendingIntent.FLAG_UPDATE_CURRENT);
    }
}
