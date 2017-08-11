package moe.shizuku.fcmformojo.notification;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.os.Build;
import android.support.annotation.Nullable;
import android.support.v4.util.LongSparseArray;

import java.util.ArrayList;

import moe.shizuku.fcmformojo.FFMApplication;
import moe.shizuku.fcmformojo.FFMSettings;
import moe.shizuku.fcmformojo.model.Chat;
import moe.shizuku.fcmformojo.model.Chat.ChatType;
import moe.shizuku.fcmformojo.model.Message;
import moe.shizuku.fcmformojo.model.PushChat;
import moe.shizuku.fcmformojo.receiver.NotificationReceiver;
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
    public void addMessage(Context context, PushChat pushChat) {
        if (pushChat.isSystem()) {
            handleSystemMessage(context, pushChat);
            return;
        }

        long uid = pushChat.getUid();
        // 会出现没有 uid 的情况
        if (uid == 0) {
            uid = pushChat.getId();
        }

        Chat chat = mMessages.get(uid);
        if (chat == null || pushChat.isSystem()) {
            pushChat.setMessages(new ChatMessagesList());
        } else {
            pushChat.setMessages(chat.getMessages());
            pushChat.setIcon(chat.getIcon());
        }
        chat = pushChat;
        chat.getMessages().add(chat.getLatestMessage());

        mMessages.put(uid, chat);

        mMessageCount ++;
        mSendersCount = mMessages.size();

        if (shouldNotify(context, chat)) {
            getImpl().notify(context, chat, this);
        }
    }

    private boolean shouldNotify(Context context, Chat chat) {
        String foreground = FFMApplication.get(context).getForegroundPackage();
        if (FFMSettings.getProfile().getPackageName().equals(foreground)) {
            clearMessages();
            return false;
        }

        return chat.getLatestMessage().isAt()
                || FFMSettings.getNotificationEnabled(chat.getType() != ChatType.FRIEND);
    }

    private void handleSystemMessage(Context context, PushChat chat) {
        switch (chat.getLatestMessage().getSender()) {

        }
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
        return PendingIntent.getBroadcast(context, requestCode, NotificationReceiver.contentIntent(chat), PendingIntent.FLAG_UPDATE_CURRENT);
    }

    public static PendingIntent createDeleteIntent(Context context, int requestCode, @Nullable Chat chat) {
        return PendingIntent.getBroadcast(context, requestCode, NotificationReceiver.deleteIntent(chat), PendingIntent.FLAG_UPDATE_CURRENT);
    }
}
