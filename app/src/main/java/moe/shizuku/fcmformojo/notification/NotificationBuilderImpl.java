package moe.shizuku.fcmformojo.notification;

import android.content.Context;

import moe.shizuku.fcmformojo.model.Chat;

/**
 * Created by Rikka on 2016/9/18.
 */
abstract class NotificationBuilderImpl {

    /**
     * 发送通知
     */
    abstract void notify(Context context, Chat chat, NotificationBuilder nb);
}
