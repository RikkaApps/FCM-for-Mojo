package moe.shizuku.fcmformojo.model;

import android.support.annotation.Keep;

/**
 * Created by rikka on 2017/8/26.
 */

@Keep
public class FFMStatus {

    private int devices;
    private String version;
    private boolean running;

    public int getDevices() {
        return devices;
    }

    public String getVersion() {
        return version;
    }

    public boolean isRunning() {
        return running;
    }
}
