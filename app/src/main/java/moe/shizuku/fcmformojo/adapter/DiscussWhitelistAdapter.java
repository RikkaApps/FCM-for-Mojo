package moe.shizuku.fcmformojo.adapter;

import android.util.Pair;

import java.util.HashSet;
import java.util.Set;

import moe.shizuku.fcmformojo.model.Discuss;
import moe.shizuku.fcmformojo.model.DiscussWhitelistState;
import moe.shizuku.fcmformojo.viewholder.DiscussWhitelistViewHolder;

/**
 * Created by rikka on 2017/9/2.
 */

public class DiscussWhitelistAdapter extends WhitelistAdapter {

    @SuppressWarnings("unchecked")
    public DiscussWhitelistAdapter() {
        getCreatorPool().putRule(Pair.class, DiscussWhitelistViewHolder.CREATOR);
    }

    @Override
    public DiscussWhitelistState collectCurrentData() {
        Set<String> list = new HashSet<>();
        for (Pair<Discuss, Boolean> state : this.<Pair<Discuss, Boolean>>getItems()) {
            if (state.second) {
                list.add(state.first.getName());
            }
        }
        return new DiscussWhitelistState(isEnabled(), list);
    }
}
