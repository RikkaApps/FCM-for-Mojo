package moe.shizuku.fcmformojo.profile;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;

import moe.shizuku.fcmformojo.R;
import moe.shizuku.fcmformojo.model.Chat;

/**
 * Created by rikka on 2017/7/29.
 */

public class TIMProfile implements Profile {

    @Override
    public String getPackageName() {
        return "com.tencent.tim";
    }

    @Override
    public int getDisplayName() {
        return R.string.display_name_tim;
    }

    @Override
    public int getNotificationIcon() {
        return R.drawable.ic_noti_tim_24dp;
    }

    @Override
    public int getNotificationColor() {
        return R.color.colorNotification;
    }

    @Override
    public void onStartChatActivity(Context context, Chat chat) {
        if (chat == null) {
            ProfileHelper.startActivity(context, this);
            return;
        }

        @SuppressLint("WrongConstant")
        Intent intent = new Intent("com.tencent.tim.action.MAINACTIVITY")
                .setComponent(ComponentName.unflattenFromString("com.tencent.tim/com.tencent.mobileqq.activity.SplashActivity"))
                .setFlags(335544320)
                .putExtra("open_chatfragment", true)
                .putExtra("entrance", 6)
                .putExtra("key_notification_click_action", (String) null)
                .putExtra("uinname", chat.getName())
                .putExtra("uintype", chat.getType() - 1)
                .putExtra("uin", Long.toString(chat.getUid()));

        context.startActivity(intent);
    }
}
