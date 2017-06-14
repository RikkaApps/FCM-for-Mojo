package moe.shizuku.fcmformojo.notification;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.IntRange;
import android.support.annotation.Nullable;
import android.support.v4.util.LongSparseArray;

import java.lang.ref.WeakReference;

import moe.shizuku.fcmformojo.FFMSettings;
import moe.shizuku.fcmformojo.R;
import moe.shizuku.fcmformojo.model.Chat;
import moe.shizuku.fcmformojo.model.PushMessage;
import moe.shizuku.fcmformojo.receiver.NotificationReceiver;
import moe.shizuku.fcmformojo.utils.DrawableUtils;

/**
 * Created by Rikka on 2016/4/8.
 *
 * 用来放消息内容，处理发通知，通知被点击被删除的东西
 */
public class NotificationBuilder {

    private NotificationManager mNotificationManager;

    private LongSparseArray<Chat> mMessages;

    private WeakReference[] mPersonIcons;
    private WeakReference[] mGroupIcons;

    private int mMessageCount;
    private int mSendersCount;

    private NotificationBuilderImpl mImpl;

    private NotificationBuilderImpl getImpl() {
        return mImpl;
    }

    private NotificationBuilderImpl createImpl(Context context) {
        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.N) {
            return new NotificationBuilderImplN();
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            return new NotificationBuilderImplO(context);
        }
        return null;
    }

    public NotificationBuilder(Context context) {
        mSendersCount = 0;
        mMessages = new LongSparseArray<>();
        mPersonIcons = new WeakReference[7];
        mGroupIcons = new WeakReference[7];
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

        chat.getMessages().add(new Chat.Message(message));

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
        return chat.isSystemMessage() || FFMSettings.getNotification(chat.getType() != 1);
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

    public static PendingIntent getContentIntent(Context context, int requestCode, @Nullable Chat chat, boolean all) {
        long id = chat == null ? 0 : chat.getId();
        if (id == -1 || id == -3) {
            return null;
        }
        return PendingIntent.getBroadcast(context, requestCode, NotificationReceiver.contentIntent(id, all), 0);
    }

    public static PendingIntent getDeleteIntent(Context context, int requestCode, @Nullable Chat chat, boolean all) {
        long id = chat == null ? 0 : chat.getId();
        if (id == -1 || id == -3) {
            return null;
        }
        return PendingIntent.getBroadcast(context, requestCode, NotificationReceiver.deleteIntent(id, all), 0);
    }

    Bitmap getLargeIcon(Context context, int i, boolean group) {
        i = Math.abs(i % 7);
        if (group) {
            if (mPersonIcons[i] == null || mPersonIcons[i].get() == null) {
                mPersonIcons[i] = new WeakReference<>(makeBitmap(context, i, true));
            }
            return (Bitmap) mPersonIcons[i].get();
        } else {
            if (mGroupIcons[i] == null || mGroupIcons[i].get() == null) {
                mGroupIcons[i] = new WeakReference<>(makeBitmap(context,  i, false));
            }
            return (Bitmap) mGroupIcons[i].get();
        }
    }

    private static final int[] COLORS = {
            R.color.colorNotificationRed,
            R.color.colorNotificationOrange,
            R.color.colorNotificationYellow,
            R.color.colorNotificationGreen,
            R.color.colorNotificationIndigo,
            R.color.colorNotificationBlue,
            R.color.colorNotificationPurple,
    };

    private static Bitmap makeBitmap(Context context, @IntRange(from = 0, to = 6) int i, boolean group) {
        Drawable drawable = context.getDrawable(group ? R.drawable.ic_noti_group_48dp : R.drawable.ic_noti_person_48dp);
        if (drawable != null) {
            drawable.setTint(context.getColor(COLORS[i]));

            return DrawableUtils.toBitmap(drawable);
        }
        return null;
    }
}
