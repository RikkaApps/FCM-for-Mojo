package moe.shizuku.fcmformojo.profile;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;

import moe.shizuku.fcmformojo.R;
import moe.shizuku.fcmformojo.compat.ShizukuCompat;
import moe.shizuku.fcmformojo.model.Chat;

/**
 * Created by rikka on 2017/7/29.
 */

public class QQLProfile implements Profile {

    @Override
    public String getPackageName() {
        return "com.tencent.qqlite";
    }

    @Override
    public int getDisplayName() {
        return R.string.display_name_qql;
    }

    @Override
    public int getNotificationIcon() {
        return R.drawable.ic_noti_qq_24dp;
    }

    @Override
    public int getNotificationColor() {
        return R.color.colorNotification;
    }

    @SuppressLint("WrongConstant")
    @Override
    public void onStartChatActivity(Context context, Chat chat) {
        Intent intent = new Intent("com.tencent.qqlite.action.CHAT")
                .setComponent(ComponentName.unflattenFromString("com.tencent.qqlite/com.tencent.mobileqq.activity.ChatActivity"))
                .setFlags(0x14000000)
                .putExtra("entrance", 6)
                .putExtra("key_notification_click_action", true)
                .putExtra("uinname", chat.getName())
                .putExtra("uintype", chat.getType())
                .putExtra("uin", Long.toString(chat.getUid()))
                .putExtra("incoming_senderuin", Long.toString(chat.getUid()));

        if (!ShizukuCompat.startActivity(context, intent, getPackageName())) {
            ProfileHelper.startLauncherActivity(context, this);
        }
    }

    @Override
    public void onStartQrCodeScanActivity(Context context) {
        Intent intent = new Intent()
                .setClassName(getPackageName(), "com.tencent.biz.qrcode.activity.ScannerActivity")
                .setComponent(ComponentName.unflattenFromString("com.tencent.qqlite/com.tencent.biz.qrcode.activity.ScannerActivity"));
        if (!ShizukuCompat.startActivity(context, intent, getPackageName())) {
            ProfileHelper.startLauncherActivity(context, this);
        }
    }
}
