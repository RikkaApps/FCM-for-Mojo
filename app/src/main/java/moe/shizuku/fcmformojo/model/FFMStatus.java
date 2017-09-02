package moe.shizuku.fcmformojo.model;

import android.support.annotation.Keep;

/**
 * Created by rikka on 2017/8/26.
 */

@Keep
public class FFMStatus {

    private int devices;
    private WhitelistStatus group_whitelist;
    private WhitelistStatus discuss_whitelist;
    private String version;
    private boolean running;

    public int getDevices() {
        return devices;
    }

    public WhitelistStatus getGroupBlacklist() {
        return group_whitelist;
    }

    public WhitelistStatus getDiscussWhitelist() {
        return discuss_whitelist;
    }

    public String getVersion() {
        return version;
    }

    public boolean isRunning() {
        return running;
    }

    @Keep
    public static class WhitelistStatus {

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
