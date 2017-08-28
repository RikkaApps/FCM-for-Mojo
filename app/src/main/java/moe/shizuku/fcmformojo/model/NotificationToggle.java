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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        NotificationToggle that = (NotificationToggle) o;

        if (friend != that.friend) return false;
        return group == that.group;
    }

    @Override
    public int hashCode() {
        int result = (friend ? 1 : 0);
        result = 31 * result + (group ? 1 : 0);
        return result;
    }

    @Override
    public String toString() {
        return "NotificationToggle{" +
                "friend=" + friend +
                ", group=" + group +
                '}';
    }
}
