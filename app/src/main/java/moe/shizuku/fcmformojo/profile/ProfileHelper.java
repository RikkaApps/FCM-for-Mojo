package moe.shizuku.fcmformojo.profile;

import android.content.Context;
import android.content.Intent;

/**
 * Created by rikka on 2017/7/29.
 */

public class ProfileHelper {

    public static void startActivity(Context context, Profile profile) {
        Intent intent = context.getPackageManager().getLaunchIntentForPackage(profile.getPackageName());
        if (intent == null) {
            return;
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        context.startActivity(intent);
    }
}
