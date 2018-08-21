package moe.shizuku.fcmformojo;

import android.os.Bundle;

import io.reactivex.Single;
import moe.shizuku.fcmformojo.adapter.GroupWhitelistAdapter;
import moe.shizuku.fcmformojo.adapter.WhitelistAdapter;
import moe.shizuku.fcmformojo.model.FFMResult;
import moe.shizuku.fcmformojo.model.GroupWhitelistState;
import moe.shizuku.fcmformojo.model.WhitelistState;

import static moe.shizuku.fcmformojo.FFMApplication.FFMService;
import static moe.shizuku.fcmformojo.FFMApplication.OpenQQService;

/**
 * Created by rikka on 2017/9/2.
 */

public class GroupWhitelistActivity extends AbsWhitelistActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mSwitchBar.setSwitchOnText(R.string.whitelist_summary_group_on);
        mSwitchBar.setSwitchOffText(R.string.whitelist_summary_group_off);
    }

    @Override
    public WhitelistAdapter createListAdapter() {
        return new GroupWhitelistAdapter();
    }

    @Override
    public Single<? extends WhitelistState> startFetchWhitelistState() {
        return Single.zip(FFMService.getGroupWhitelist(), OpenQQService.getGroupsBasicInfo(),
                (state, groups) -> {
                    state.generateStates(groups);
                    return state;
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
