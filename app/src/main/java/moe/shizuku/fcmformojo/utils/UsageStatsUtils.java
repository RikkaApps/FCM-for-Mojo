package moe.shizuku.fcmformojo.utils;

import android.app.AppOpsManager;
import android.app.usage.UsageEvents;
import android.app.usage.UsageStatsManager;
import android.content.Context;

/**
 * Created by Rikka on 2017/4/23.
 */

public class UsageStatsUtils {

    public static boolean granted(Context context) {
        AppOpsManager appOpsManager = (AppOpsManager) context.getSystemService(Context.APP_OPS_SERVICE);
        int mode = appOpsManager.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS,
                android.os.Process.myUid(), context.getPackageName());
        return mode == AppOpsManager.MODE_ALLOWED;
    }

    public static String getForegroundPackage(Context context) {
        return getForegroundPackage((UsageStatsManager) context.getSystemService(Context.USAGE_STATS_SERVICE));
    }

    public static String getForegroundPackage(UsageStatsManager usageStatsManager) {
        String packageName = null;

        final long INTERVAL = 1000 * 60;
        final long end = System.currentTimeMillis();
        final long begin = end - INTERVAL;
        final UsageEvents usageEvents = usageStatsManager.queryEvents(begin, end);
        while (usageEvents.hasNextEvent()) {
            UsageEvents.Event event = new UsageEvents.Event();
            usageEvents.getNextEvent(event);
            switch (event.getEventType()) {
                case UsageEvents.Event.MOVE_TO_FOREGROUND:
                    packageName = event.getPackageName();
                    break;
                case UsageEvents.Event.MOVE_TO_BACKGROUND:
                    if (event.getPackageName().equals(packageName)) {
                        packageName = null;
                    }
            }
        }

        return packageName;
    }
}
