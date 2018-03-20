package moe.shizuku.fcmformojo.adapter;

import moe.shizuku.fcmformojo.model.WhitelistState;
import moe.shizuku.support.recyclerview.BaseRecyclerViewAdapter;
import moe.shizuku.support.recyclerview.ClassCreatorPool;

/**
 * Created by rikka on 2017/8/28.
 */

public abstract class WhitelistAdapter extends BaseRecyclerViewAdapter<ClassCreatorPool> {

    private boolean mEnabled;

    @Override
    public ClassCreatorPool onCreateCreatorPool() {
        return new ClassCreatorPool();
    }

    public boolean isEnabled() {
        return mEnabled;
    }

    public void setEnabled(boolean enabled) {
        mEnabled = enabled;
    }

    public void updateData(WhitelistState state) {
        mEnabled = state.isEnabled();

        getItems().clear();
        getItems().addAll(state.getStates());

        notifyDataSetChanged();
    }

    public abstract WhitelistState collectCurrentData();
}
