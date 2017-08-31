package moe.shizuku.fcmformojo.model;

import android.support.annotation.Keep;

/**
 * Created by rikka on 2017/8/26.
 */

@Keep
public class FFMStatus {

    private int devices;
    private GroupBlacklistStatus group_blacklist;
    private String version;
    private boolean running;

    public int getDevices() {
        return devices;
    }

    public GroupBlacklistStatus getGroupBlacklist() {
        return group_blacklist;
    }

    public String getVersion() {
        return version;
    }

    public boolean isRunning() {
        return running;
    }

    public static class GroupBlacklistStatus {

        private boolean enabled;
        private int count;

        public boolean isEnabled() {
            return enabled;
        }

        public int getCount() {
            return count;
        }
    }
}
