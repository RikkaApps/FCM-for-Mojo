package moe.shizuku.fcmformojo;

import android.widget.CompoundButton;

import java.util.List;

import io.reactivex.Single;
import io.reactivex.functions.BiFunction;
import moe.shizuku.fcmformojo.adapter.GroupWhitelistAdapter;
import moe.shizuku.fcmformojo.adapter.WhitelistAdapter;
import moe.shizuku.fcmformojo.model.FFMResult;
import moe.shizuku.fcmformojo.model.Group;
import moe.shizuku.fcmformojo.model.GroupWhitelistState;
import moe.shizuku.fcmformojo.model.WhitelistState;

import static moe.shizuku.fcmformojo.FFMApplication.FFMService;
import static moe.shizuku.fcmformojo.FFMApplication.OpenQQService;

/**
 * Created by rikka on 2017/9/2.
 */

public class GroupWhitelistActivity extends AbsWhitelistActivity {

    @Override
    public void setToggleText(CompoundButton button, boolean checked) {
        button.setText(button.getContext().getString(checked ? R.string.whitelist_summary_group_on : R.string.whitelist_summary_group_off));
    }

    @Override
    public WhitelistAdapter createListAdapter() {
        return new GroupWhitelistAdapter();
    }

    @Override
    public Single<? extends WhitelistState> startFetchWhitelistState() {
        return Single.zip(FFMService.getGroupWhitelist(), OpenQQService.getGroupsBasicInfo(),
                new BiFunction<GroupWhitelistState, List<Group>, GroupWhitelistState>() {
                    @Override
                    public GroupWhitelistState apply(GroupWhitelistState state, List<Group> groups) throws Exception {
                        state.generateStates(groups);
                        return state;
                    }
                });
    }

    @Override
    public Single<FFMResult> startUpdateWhitelistState(WhitelistState whitelistState) {
        return FFMService.updateGroupWhitelist((GroupWhitelistState) whitelistState);
    }

    @Override
    public void onFetchSucceed(WhitelistState state) {
        FFMSettings.putLocalGroupWhitelistValue(state.isEnabled() ? state.getList().size() : -1);
    }

    @Override
    public void onUploadSucceed(WhitelistState state) {
        FFMSettings.putLocalGroupWhitelistValue(state.isEnabled() ? state.getList().size() : -1);
    }
}
