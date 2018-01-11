package moe.shizuku.fcmformojo.adapter;

import android.util.Pair;

import java.util.HashSet;
import java.util.Set;

import moe.shizuku.fcmformojo.model.Group;
import moe.shizuku.fcmformojo.model.GroupWhitelistState;
import moe.shizuku.fcmformojo.viewholder.GroupWhitelistItemViewHolder;
import moe.shizuku.support.recyclerview.ClassCreatorPool;

/**
 * Created by rikka on 2017/9/2.
 */

public class GroupWhitelistAdapter extends WhitelistAdapter {

    public GroupWhitelistAdapter() {
        getCreatorPool().putRule(Pair.class, GroupWhitelistItemViewHolder.CREATOR);
    }

    @Override
    public ClassCreatorPool getCreatorPool() {
        return (ClassCreatorPool) super.getCreatorPool();
    }

    @Override
    public GroupWhitelistState collectCurrentData() {
        Set<Long> list = new HashSet<>();
        for (Pair<Group, Boolean> state : this.<Pair<Group, Boolean>>getItems()) {
            if (state.second) {
                list.add(state.first.getUid());
            }
        }
        return new GroupWhitelistState(isEnabled(), list);
    }
}
