package moe.shizuku.fcmformojo.notification;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;

import moe.shizuku.fcmformojo.model.Chat;

/**
 * Created by Rikka on 2016/9/18.
 */
abstract class NotificationBuilderImpl {

    /**
     * create NotificationCompat.Builder
     */
    abstract public NotificationCompat.Builder createBuilder(Context context, @Nullable Chat chat);

    /**
     * 发送通知
     */
    abstract void notify(Context context, Chat chat, NotificationBuilder nb);

    /**
     * 发送系统通知
     */
    abstract void notifySystem(Context context, Chat chat, NotificationBuilder nb);

    /**
     * 清除通知
     */
    abstract void clear(Chat chat, NotificationBuilder nb);
}
