package moe.shizuku.fcmformojo.compat;

import android.content.Context;
import android.content.Intent;
import android.os.UserHandle;
import android.os.UserManager;

import moe.shizuku.fcmformojo.FFMApplication;
import moe.shizuku.privileged.api.PrivilegedAPIs;

/**
 * Created by rikka on 2017/8/22.
 */

public class ShizukuCompat {

    /**
     * 在所有用户中寻找并尝试打开 Activity。
     *
     * @param context Context
     * @param intent Intent
     * @param packageName 包名
     */
    public static boolean startActivity(Context context, Intent intent, String packageName) {
        // 如果当前用户有就直接打开
        if (intent.resolveActivity(context.getPackageManager()) != null) {
            try {
                context.startActivity(intent);
                return true;
            } catch (SecurityException e) {
                // 给 Shizuku 处理
                //Toast.makeText(context, "Can't start activity because of permission.", Toast.LENGTH_SHORT).show();
            } catch (Exception ignored) {
                return false;
            }
        }

        // 就可能是在其他的用户了
        UserManager userManager = context.getSystemService(UserManager.class);

        PrivilegedAPIs.setPermitNetworkThreadPolicy();
        PrivilegedAPIs privilegedAPIs = FFMApplication.sPrivilegedAPIs;
        if (!privilegedAPIs.authorized()) {
            return false;
        }

        for (UserHandle userHandle : userManager.getUserProfiles()) {
            int userId = userHandle.hashCode(); // 就是（
            try {
                if (privilegedAPIs.getApplicationInfo(packageName, 0, userId) != null) {
                    privilegedAPIs.startActivity(intent, userId);
                    return true;
                }
            } catch (SecurityException e) {
                //Toast.makeText(context, "Can't start activity because of permission.", Toast.LENGTH_SHORT).show();
                return false;
            } catch (Exception ignored) {
            }
        }

        return false;
    }
}
