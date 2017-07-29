package moe.shizuku.fcmformojo.profile;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by rikka on 2017/7/29.
 */

public class ProfileList {

    private static final List<Profile> list = new ArrayList<>();

    static {
        list.add(new QQProfile());
        list.add(new TIMProfile());
        list.add(new QQLProfile());
        list.add(new QQHDProfile());
        list.add(new QQiProfile());
        list.add(new QQJPProfile());
    }

    public static Profile getProfile(String packageName) {
        for (Profile profile : list) {
            if (packageName.equals(profile.getPackageName())) {
                return profile;
            }
        }
        return list.get(0);
    }

    public static List<Profile> getProfile() {
        return list;
    }
}
