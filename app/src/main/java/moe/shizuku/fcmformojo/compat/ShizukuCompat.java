package moe.shizuku.fcmformojo.compat;

import android.content.Context;
import android.content.Intent;
import android.os.UserHandle;
import android.os.UserManager;
import android.support.annotation.WorkerThread;

import moe.shizuku.api.ShizukuActivityManagerV26;
import moe.shizuku.api.ShizukuClient;
import moe.shizuku.api.ShizukuPackageManagerV26;

/**
 * Created by rikka on 2017/8/22.
 */

@WorkerThread
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
        if (userManager == null) {
            return false;
        }

        if (!ShizukuClient.getState().isAuthorized()) {
            return false;
        }

        for (UserHandle userHandle : userManager.getUserProfiles()) {
            int userId = userHandle.hashCode(); // 就是（
            try {
                if (ShizukuPackageManagerV26.getApplicationInfo(packageName, 0, userId) != null) {
                    ShizukuActivityManagerV26.startActivityAsUser(
                            null, null, intent, null, null, null, 0, 0, null, null, userId);
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

    public static String getForegroundPackage() {
        try {
            return ShizukuActivityManagerV26.getTasks(1, 0).get(0).baseActivity.getPackageName();
        } catch (Exception ignored) {
            return null;
        }
    }
}
