package moe.shizuku.fcmformojo.profile;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;

import moe.shizuku.fcmformojo.compat.ShizukuCompat;

/**
 * Created by rikka on 2017/7/29.
 */

public class ProfileHelper {

    public static void startLauncherActivity(Context context, Profile profile) {
        Intent intent = context.getPackageManager().getLaunchIntentForPackage(profile.getPackageName());
        if (intent == null) {
            return;
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);

        ShizukuCompat.startActivity(context, intent, profile.getPackageName());
    }

    public static boolean installed(Context context, Profile profile) {
        try {
            context.getPackageManager().getApplicationInfo(profile.getPackageName(), 0);
            return true;
        } catch (PackageManager.NameNotFoundException ignored) {
            return false;
        }
    }
}
