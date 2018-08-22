package moe.shizuku.fcmformojo;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.google.firebase.iid.FirebaseInstanceId;

import java.util.Set;
import java.util.concurrent.Callable;

import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import moe.shizuku.fcmformojo.adapter.RegistrationIdsAdapter;
import moe.shizuku.fcmformojo.model.FFMResult;
import moe.shizuku.fcmformojo.model.RegistrationId;
import moe.shizuku.fcmformojo.utils.LocalBroadcast;
import moe.shizuku.fcmformojo.viewholder.RegistrationIdViewHolder;
import moe.shizuku.fcmformojo.viewholder.TitleViewHolder;
import moe.shizuku.support.recyclerview.RecyclerViewHelper;

import static moe.shizuku.fcmformojo.FFMApplication.FFMService;

public class RegistrationIdsActivity extends AbsConfigurationsActivity {

    private CompositeDisposable mCompositeDisposable = new CompositeDisposable();

    private RegistrationIdsAdapter mAdapter;

    private boolean mRefreshed;

    private Set<RegistrationId> mServerRegistrationIds;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registeration_ids);

        if (getActionBar() != null) {
            getActionBar().setDisplayHomeAsUpEnabled(true);
        }

        RecyclerView recyclerView = findViewById(android.R.id.list);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        mAdapter = new RegistrationIdsAdapter();
        mAdapter.getCreatorPool()
                .putRule(RegistrationId.class, RegistrationIdViewHolder.CREATOR)
                .putRule(CharSequence.class, TitleViewHolder.CREATOR);

        recyclerView.setAdapter(mAdapter);

        RecyclerViewHelper.fixOverScroll(recyclerView);

        updateItems();
        fetchRegistrationIds();

        // 设为 null 主界面就不会再提示需要注意 token
        FFMSettings.putNewToken(null);
    }

    @Override
    protected void onDestroy() {
        mCompositeDisposable.clear();
        super.onDestroy();
    }

    public void updateItems(Set<RegistrationId> items) {
        mRefreshed = true;

        mAdapter.getItems().clear();
        mAdapter.getItems().add(getString(R.string.manage_devices_header));
        mAdapter.getItems().addAll(items);
        mAdapter.notifyDataSetChanged();

        invalidateOptionsMenu();
    }

    public void updateItems() {
        mAdapter.getItems().clear();
        mAdapter.getItems().add(getString(R.string.manage_devices_header));
        mAdapter.getItems().addAll(mAdapter.getRegistrationIds());
        mAdapter.notifyDataSetChanged();
    }

    private void fetchRegistrationIds() {
        mCompositeDisposable.add(FFMService.getRegistrationIds()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(registrationIds -> {
                    mServerRegistrationIds = registrationIds;

                    updateItems(registrationIds);
                }, throwable -> Toast.makeText(getApplicationContext(), getString(R.string.toast_something_wroing, throwable.getMessage()), Toast.LENGTH_SHORT).show())
        );
    }

    private void addDevice() {
        RegistrationId registrationId = RegistrationId.create();
        if (registrationId != null) {
            addDevice(registrationId);
        } else {
            Toast.makeText(this, R.string.toast_token_requesting, Toast.LENGTH_SHORT).show();

            mCompositeDisposable.add(Single
                    .fromCallable(() -> FirebaseInstanceId.getInstance().getToken(getString(R.string.project_id), "FCM"))
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(token -> addDevice(RegistrationId.create(token)), throwable -> {
                        throwable.printStackTrace();

                        Toast.makeText(RegistrationIdsActivity.this, getString(R.string.toast_something_wroing, throwable.getMessage()), Toast.LENGTH_SHORT).show();

                        Crashlytics.log("requesting token");
                        Crashlytics.logException(throwable);
                    }));
        }
    }

    private void addDevice(RegistrationId registrationId) {
        for (RegistrationId id : mAdapter.getRegistrationIds()) {
            if (id.getId().equals(registrationId.getId())) {
                Toast.makeText(this, R.string.toast_token_exists, Toast.LENGTH_SHORT).show();
                return;
            }
        }

        mAdapter.getItems().add(registrationId);
        mAdapter.notifyItemInserted(mAdapter.getItemCount() - 1);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.findItem(R.id.action_upload).setEnabled(mRefreshed);
        menu.findItem(R.id.action_add).setEnabled(mRefreshed);
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.ids, menu);
        return true;
    }

    @Override
    public void uploadConfigurations() {
        if (!isConfigurationsChanged()) {
            Toast.makeText(getApplicationContext(), R.string.toast_nothing_changed, Toast.LENGTH_SHORT).show();

            return;
        }

        final Set<RegistrationId> registrationIds = mAdapter.getRegistrationIds();
        mCompositeDisposable.add(FFMService.updateRegistrationIds(registrationIds)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(result -> {
                    mServerRegistrationIds = registrationIds;

                    Toast.makeText(getApplicationContext(), R.string.toast_succeeded, Toast.LENGTH_SHORT).show();

                    LocalBroadcast.refreshStatus(getApplicationContext());
                }, throwable -> Toast.makeText(getApplicationContext(), getString(R.string.toast_something_wroing, throwable.getMessage()), Toast.LENGTH_SHORT).show())
        );
    }

    @Override
    public boolean isConfigurationsChanged() {
        return mServerRegistrationIds != null
                && !mAdapter.getRegistrationIds().equals(mServerRegistrationIds);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_add:
                addDevice();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
