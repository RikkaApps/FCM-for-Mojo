package moe.shizuku.fcmformojo.adapter;

import android.util.Pair;

import java.util.ArrayList;
import java.util.List;

import moe.shizuku.fcmformojo.model.BlacklistState;
import moe.shizuku.fcmformojo.model.Group;
import moe.shizuku.utils.recyclerview.BaseRecyclerViewAdapter;

/**
 * Created by rikka on 2017/8/28.
 */

public class BlacklistAdapter extends BaseRecyclerViewAdapter {

    private boolean mEnabled;

    public boolean isEnabled() {
        return mEnabled;
    }

    public void setEnabled(boolean enabled) {
        mEnabled = enabled;
    }

    public void updateData(BlacklistState state) {
        mEnabled = state.isEnabled();

        getItems().clear();
        getItems().addAll(state.getStates());

        notifyDataSetChanged();
    }

    public BlacklistState collectCurrentData() {
        List<Long> list = new ArrayList<>();
        for (Pair<Group, Boolean> state : this.<Pair<Group, Boolean>>getItems()) {
            if (!state.second) {
                list.add(state.first.getUid());
            }
        }
        return new BlacklistState(mEnabled, list);
    }
}
