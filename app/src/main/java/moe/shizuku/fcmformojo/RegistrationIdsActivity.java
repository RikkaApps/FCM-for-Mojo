package moe.shizuku.fcmformojo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import moe.shizuku.fcmformojo.adapter.RegistrationIdsAdapter;
import moe.shizuku.fcmformojo.api.FFMService;
import moe.shizuku.fcmformojo.model.FFMResult;
import moe.shizuku.fcmformojo.model.RegistrationId;
import moe.shizuku.fcmformojo.viewholder.RegistrationIdViewHolder;
import moe.shizuku.fcmformojo.viewholder.TitleViewHolder;
import moe.shizuku.utils.recyclerview.helper.RecyclerViewHelper;

import static moe.shizuku.fcmformojo.FFMStatic.ACTION_UPDATE_URL;

public class RegistrationIdsActivity extends BaseActivity {

    private CompositeDisposable mCompositeDisposable = new CompositeDisposable();

    private RecyclerView mRecyclerView;
    private RegistrationIdsAdapter mAdapter;

    private boolean mRefreshed;

    private FFMService mFFMService;

    private BroadcastReceiver mUrlChangedBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            mFFMService = FFMApplication.getRxRetrofit(context).create(FFMService.class);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registeration_ids);

        if (getActionBar() != null) {
            getActionBar().setDisplayHomeAsUpEnabled(true);
        }

        mRecyclerView = findViewById(android.R.id.list);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        mAdapter = new RegistrationIdsAdapter();
        mAdapter.addRule(RegistrationId.class, RegistrationIdViewHolder.CREATOR);
        mAdapter.addRule(CharSequence.class, TitleViewHolder.CREATOR);

        mRecyclerView.setAdapter(mAdapter);

        RecyclerViewHelper.fixOverScroll(mRecyclerView);

        mFFMService = FFMApplication.getRxRetrofit(this).create(FFMService.class);

        updateItems();
        requestRegistrationIds();

        LocalBroadcastManager.getInstance(this)
                .registerReceiver(mUrlChangedBroadcastReceiver, new IntentFilter(ACTION_UPDATE_URL));
    }

    @Override
    protected void onDestroy() {
        LocalBroadcastManager.getInstance(this)
                .unregisterReceiver(mUrlChangedBroadcastReceiver);
        mCompositeDisposable.clear();
        super.onDestroy();
    }

    public void updateItems(List<RegistrationId> items) {
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

    private void requestRegistrationIds() {
        mCompositeDisposable.add(mFFMService.getRegistrationIds()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<List<RegistrationId>>() {
                    @Override
                    public void accept(List<RegistrationId> registrationIds) throws Exception {
                        updateItems(registrationIds);
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        Toast.makeText(getApplicationContext(), "Network error:\n" + throwable.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                })
        );
    }

    private void updateRegistrationIds() {
        mCompositeDisposable.add(mFFMService.updateRegistrationIds(mAdapter.getRegistrationIds())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<FFMResult>() {
                    @Override
                    public void accept(FFMResult registrationIds) throws Exception {
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

    private void addDevice() {
        RegistrationId registrationId = RegistrationId.create();
        if (registrationId == null) {
            Toast.makeText(this, "Can't add because token is null.", Toast.LENGTH_SHORT).show();
            return;
        }

        for (RegistrationId id : mAdapter.getRegistrationIds()) {
            if (id.getId().equals(registrationId.getId())) {
                Toast.makeText(this, "Can't add because token already exists.", Toast.LENGTH_SHORT).show();
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
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_upload:
                updateRegistrationIds();
                return true;
            case R.id.action_add:
                addDevice();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
