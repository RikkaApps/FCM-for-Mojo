package moe.shizuku.fcmformojo.model;

import android.support.annotation.Keep;

/**
 * Created by rikka on 2017/8/25.
 */

@Keep
public class NotificationToggle {

    private boolean friend;
    private boolean group;

    NotificationToggle(boolean friend, boolean group) {
        this.friend = friend;
        this.group = group;
    }

    public boolean isFriendEnable() {
        return friend;
    }

    public boolean isGroupEnable() {
        return group;
    }

    public static NotificationToggle create(boolean friend, boolean group) {
        return new NotificationToggle(friend, group);
    }
}
