package moe.shizuku.fcmformojo;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Pair;
import android.view.Menu;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Toast;

import java.util.List;

import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.BiFunction;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import moe.shizuku.fcmformojo.adapter.BlacklistAdapter;
import moe.shizuku.fcmformojo.model.BlacklistState;
import moe.shizuku.fcmformojo.model.FFMResult;
import moe.shizuku.fcmformojo.model.Group;
import moe.shizuku.fcmformojo.viewholder.BlacklistItemViewHolder;
import moe.shizuku.utils.recyclerview.helper.RecyclerViewHelper;

import static moe.shizuku.fcmformojo.FFMApplication.FFMService;
import static moe.shizuku.fcmformojo.FFMApplication.OpenQQService;

public class BlacklistActivity extends AbsConfigurationsActivity {

    private CompositeDisposable mCompositeDisposable = new CompositeDisposable();

    private View mToggleContainer;
    private CompoundButton mToggle;

    private RecyclerView mRecyclerView;
    private BlacklistAdapter mAdapter;

    private BlacklistState mServerBlacklistState;

    private boolean mRefreshed;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_blacklist);

        if (getActionBar() != null) {
            getActionBar().setDisplayHomeAsUpEnabled(true);
        }

        mRecyclerView = findViewById(android.R.id.list);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        mAdapter = new BlacklistAdapter();
        mAdapter.addRule(Pair.class, BlacklistItemViewHolder.CREATOR);

        mRecyclerView.setAdapter(mAdapter);

        RecyclerViewHelper.fixOverScroll(mRecyclerView);

        mToggle = findViewById(android.R.id.switch_widget);
        mToggle.setEnabled(false);
        mToggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton button, boolean checked) {
                button.setText(button.getContext().getString(checked ? R.string.per_group_on : R.string.per_group_off));

                mAdapter.setEnabled(checked);
                mAdapter.notifyItemRangeChanged(0, mAdapter.getItemCount(), checked);
            }
        });

        mToggleContainer = findViewById(R.id.switch_container);
        mToggleContainer.setEnabled(false);
        mToggleContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mToggle.setChecked(!mToggle.isChecked());
            }
        });

        fetchBlacklistState();
    }

    private void fetchBlacklistState() {
        mCompositeDisposable.add(Single.zip(FFMService.getGroupBlacklist(), OpenQQService.getGroupsBasicInfo(),
                new BiFunction<BlacklistState, List<Group>, BlacklistState>() {
                    @Override
                    public BlacklistState apply(BlacklistState state, List<Group> groups) throws Exception {
                        state.generateStates(groups);
                        return state;
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<BlacklistState>() {
                    @Override
                    public void accept(BlacklistState state) throws Exception {
                        mServerBlacklistState = state;

                        mToggleContainer.setEnabled(true);
                        mToggle.setEnabled(true);
                        mToggle.setChecked(state.isEnabled());
                        mAdapter.updateData(state);
                        mRefreshed = true;

                        invalidateOptionsMenu();

                        FFMSettings.putLocalPerGroupSettingsEnabled(state.isEnabled());
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        Toast.makeText(getApplicationContext(), "Something went wrong:\n" + throwable.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }));
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.findItem(R.id.action_upload).setEnabled(mRefreshed);
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.upload, menu);
        return true;
    }

    @Override
    public void uploadConfigurations() {
        if (!isConfigurationsChanged()) {
            Toast.makeText(getApplicationContext(), "Nothing changed.", Toast.LENGTH_SHORT).show();

            return;
        }

        final BlacklistState blacklistState = mAdapter.collectCurrentData();
        mCompositeDisposable.add(FFMService.updateGroupBlacklist(blacklistState)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<FFMResult>() {
                    @Override
                    public void accept(FFMResult result) throws Exception {
                        mServerBlacklistState = blacklistState;

                        FFMSettings.putLocalPerGroupSettingsEnabled(blacklistState.isEnabled());

                        Toast.makeText(getApplicationContext(), "Succeed.", Toast.LENGTH_SHORT).show();
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        Toast.makeText(getApplicationContext(), "Network error:\n" + throwable.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                })
        );
    }

    @Override
    public boolean isConfigurationsChanged() {
        return mServerBlacklistState != null
                && !mServerBlacklistState.equals(mAdapter.collectCurrentData());
    }
}
