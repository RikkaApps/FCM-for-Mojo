package moe.shizuku.fcmformojo;

import android.widget.CompoundButton;

import java.util.List;

import io.reactivex.Single;
import io.reactivex.functions.BiFunction;
import moe.shizuku.fcmformojo.adapter.DiscussWhitelistAdapter;
import moe.shizuku.fcmformojo.adapter.WhitelistAdapter;
import moe.shizuku.fcmformojo.model.Discuss;
import moe.shizuku.fcmformojo.model.DiscussWhitelistState;
import moe.shizuku.fcmformojo.model.FFMResult;
import moe.shizuku.fcmformojo.model.WhitelistState;

import static moe.shizuku.fcmformojo.FFMApplication.FFMService;
import static moe.shizuku.fcmformojo.FFMApplication.OpenQQService;

/**
 * Created by rikka on 2017/9/2.
 */

public class DiscussWhitelistActivity extends AbsWhitelistActivity {

    @Override
    public void setToggleText(CompoundButton button, boolean checked) {
        button.setText(button.getContext().getString(checked ? R.string.whitelist_summary_discuss_on : R.string.whitelist_summary_discuss_off));
    }

    @Override
    public WhitelistAdapter createListAdapter() {
        return new DiscussWhitelistAdapter();
    }

    @Override
    public Single<? extends WhitelistState> startFetchWhitelistState() {
        return Single.zip(FFMService.getDiscussWhitelist(), OpenQQService.getDiscussesInfo(),
                new BiFunction<DiscussWhitelistState, List<Discuss>, DiscussWhitelistState>() {
                    @Override
                    public DiscussWhitelistState apply(DiscussWhitelistState state, List<Discuss> groups) throws Exception {
                        state.generateStates(groups);
                        return state;
                    }
                });
    }

    @Override
    public Single<FFMResult> startUpdateWhitelistState(WhitelistState whitelistState) {
        return FFMService.updateDiscussWhitelist((DiscussWhitelistState) whitelistState);
    }

    @Override
    public void onFetchSucceed(WhitelistState state) {
        FFMSettings.putLocalDiscussWhitelistValue(state.isEnabled() ? state.getList().size() : -1);
    }

    @Override
    public void onUploadSucceed(WhitelistState state) {
        FFMSettings.putLocalDiscussWhitelistValue(state.isEnabled() ? state.getList().size() : -1);
    }
}
