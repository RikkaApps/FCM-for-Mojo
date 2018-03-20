package android.app;

import android.content.ComponentName;

public class ActivityManager {

    public static class RunningAppProcessInfo {

        public static int IMPORTANCE_FOREGROUND;

        public static int IMPORTANCE_TOP_SLEEPING;

        public static int procStateToImportance(int state) {
            throw new UnsupportedOperationException("STUB");
        }
    }

    public static class TaskDescription {

    }

    public static class RunningTaskInfo {

        public ComponentName topActivity;
    }
}