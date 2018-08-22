package moe.shizuku.fcmformojo.model;

import android.support.annotation.Keep;

import java.util.Set;

import moe.shizuku.fcmformojo.model.openqq.Group;

/**
 * Created by rikka on 2017/8/28.
 */

@Keep
public class GroupWhitelistState extends WhitelistState<Long, Group> {

    public GroupWhitelistState(boolean enabled, Set<Long> list) {
        super(enabled, list);
    }

    @Override
    public boolean equals(Long o1, Group o2) {
        return o1 == o2.getUid();
    }
}
