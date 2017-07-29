package moe.shizuku.fcmformojo.profile;

import android.content.Context;
import android.support.annotation.ColorRes;
import android.support.annotation.DrawableRes;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;

import moe.shizuku.fcmformojo.model.Chat;

/**
 * Created by rikka on 2017/7/29.
 */

public interface Profile {

    /**
     * 返回 packageName
     *
     * @return packageName
     */
    String getPackageName();

    /**
     * 返回显示的名称资源
     *
     * @return stringRes
     */
    @StringRes int getDisplayName();

    /**
     * 返回通知小图标资源
     *
     * @return drawableRes
     */
    @DrawableRes int getNotificationIcon();

    /**
     * 返回通知颜色资源
     *
     * @return colorRes
     */
    @ColorRes
    int getNotificationColor();

    /**
     * 当通知被点击时被调用，需要在这里打开对应的 activity
     *
     * @param context Context
     * @param chat 对应的 Chat
     */
    void onStartChatActivity(Context context, @Nullable Chat chat);
}
