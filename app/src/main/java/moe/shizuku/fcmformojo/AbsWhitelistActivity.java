package moe.shizuku.fcmformojo;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Toast;

import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import moe.shizuku.fcmformojo.adapter.WhitelistAdapter;
import moe.shizuku.fcmformojo.model.FFMResult;
import moe.shizuku.fcmformojo.model.WhitelistState;
import moe.shizuku.support.recyclerview.RecyclerViewHelper;

public abstract class AbsWhitelistActivity extends AbsConfigurationsActivity {

    private CompositeDisposable mCompositeDisposable = new CompositeDisposable();

    private View mToggleContainer;
    private CompoundButton mToggle;

    private WhitelistAdapter mAdapter;
    private WhitelistState mServerWhitelistState;

    private boolean mRefreshed;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_blacklist);

        if (getActionBar() != null) {
            getActionBar().setDisplayHomeAsUpEnabled(true);
        }

        RecyclerView recyclerView = findViewById(android.R.id.list);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        mAdapter = createListAdapter();

        recyclerView.setAdapter(mAdapter);

        RecyclerViewHelper.fixOverScroll(recyclerView);

        mToggle = findViewById(android.R.id.switch_widget);
        mToggle.setEnabled(false);
        mToggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton button, boolean checked) {
                setToggleText(button, checked);

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

        fetchWhitelistState();
    }

    public abstract void setToggleText(CompoundButton button, boolean checked);

    public abstract WhitelistAdapter createListAdapter();

    public abstract Single<? extends WhitelistState> startFetchWhitelistState();

    public abstract Single<FFMResult> startUpdateWhitelistState(WhitelistState whitelistState);

    public void onFetchSucceed(WhitelistState state) {

    }

    public void onUploadSucceed(WhitelistState state) {

    }

    private void fetchWhitelistState() {
        mCompositeDisposable.add(startFetchWhitelistState()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<WhitelistState>() {
                    @Override
                    public void accept(WhitelistState state) throws Exception {
                        mServerWhitelistState = state;

                        mToggleContainer.setEnabled(true);
                        mToggle.setEnabled(true);
                        mToggle.setChecked(state.isEnabled());
                        mAdapter.updateData(state);
                        mRefreshed = true;

                        invalidateOptionsMenu();

                        onFetchSucceed(state);
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        Toast.makeText(getApplicationContext(), getString(R.string.toast_something_wroing, throwable.getMessage()), Toast.LENGTH_SHORT).show();
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
            Toast.makeText(getApplicationContext(), R.string.toast_nothing_changed, Toast.LENGTH_SHORT).show();

            return;
        }

        final WhitelistState whitelistState = mAdapter.collectCurrentData();
        mCompositeDisposable.add(startUpdateWhitelistState(whitelistState)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<FFMResult>() {
                    @Override
                    public void accept(FFMResult result) throws Exception {
                        mServerWhitelistState = whitelistState;

                        Toast.makeText(getApplicationContext(), R.string.toast_succeeded, Toast.LENGTH_SHORT).show();

                        onUploadSucceed(whitelistState);
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        Toast.makeText(getApplicationContext(), getString(R.string.toast_something_wroing, throwable.getMessage()), Toast.LENGTH_SHORT).show();
                    }
                })
        );
    }

    @Override
    public boolean isConfigurationsChanged() {
        return mServerWhitelistState != null
                && !mServerWhitelistState.equals(mAdapter.collectCurrentData());
    }
}
