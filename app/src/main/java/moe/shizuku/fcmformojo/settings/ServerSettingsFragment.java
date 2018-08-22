package moe.shizuku.fcmformojo.settings;

import android.app.ActionBar;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.ViewGroup;
import android.widget.Toast;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import moe.shizuku.fcmformojo.FFMApplication;
import moe.shizuku.fcmformojo.FFMSettings;
import moe.shizuku.fcmformojo.R;
import moe.shizuku.fcmformojo.utils.LocalBroadcast;
import moe.shizuku.fcmformojo.utils.ViewUtils;

import static moe.shizuku.fcmformojo.FFMApplication.FFMService;

/**
 * Created by rikka on 2017/8/21.
 */

public class ServerSettingsFragment extends SettingsFragment {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        super.onCreatePreferences(savedInstanceState, rootKey);

        addPreferencesFromResource(R.xml.manage_server);

        findPreference("restart_webqq").setOnPreferenceClickListener(preference -> {
            preference.setEnabled(false);
            restart();
            return true;
        });

        findPreference("stop_webqq").setOnPreferenceClickListener(preference -> {
            preference.setEnabled(false);
            stop();
            return true;
        });
    }

    @Override
    public RecyclerView onCreateRecyclerView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        RecyclerView recyclerView = super.onCreateRecyclerView(inflater, parent, savedInstanceState);
        ViewUtils.setPaddingVertical(recyclerView, getResources().getDimensionPixelSize(R.dimen.dp_8));
        return recyclerView;
    }

    private void restart() {
        mCompositeDisposable.add(FFMService.restart()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doFinally(() -> findPreference("restart_webqq").setEnabled(true))
                .subscribe(ffmResult -> Toast.makeText(getContext(), "Succeed.", Toast.LENGTH_SHORT).show(), throwable -> Toast.makeText(getContext(), "Network error:\n" + throwable.getMessage(), Toast.LENGTH_SHORT).show()));
    }

    private void stop() {
        mCompositeDisposable.add(FFMService.stop()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doFinally(() -> findPreference("stop_webqq").setEnabled(true))
                .subscribe(ffmResult -> Toast.makeText(getContext(), "Succeed.", Toast.LENGTH_SHORT).show(), throwable -> Toast.makeText(getContext(), "Network error:\n" + throwable.getMessage(), Toast.LENGTH_SHORT).show()));
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        ActionBar actionBar = getActivity().getActionBar();
        if (actionBar != null) {
            actionBar.setTitle(R.string.server_settings);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        switch (key) {
            case FFMSettings.BASE_URL:
                FFMApplication.updateBaseUrl(FFMSettings.getBaseUrl());
                LocalBroadcast.refreshStatus(getContext());
                break;
        }
    }
}
