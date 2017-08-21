package moe.shizuku.fcmformojo.utils;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

import static moe.shizuku.fcmformojo.FFMStatic.ACTION_REFRESH_STATUS;
import static moe.shizuku.fcmformojo.FFMStatic.ACTION_UPDATE_URL;

/**
 * Created by rikka on 2017/8/22.
 */

public class LocalBroadcast {

    public static void refreshStatus(Context context) {
        LocalBroadcastManager.getInstance(context)
                .sendBroadcast(new Intent(ACTION_REFRESH_STATUS));
    }

    public static void updateUrl(Context context) {
        LocalBroadcastManager.getInstance(context)
                .sendBroadcast(new Intent(ACTION_UPDATE_URL));
    }
}
