package moe.shizuku.fcmformojo.settings;

import android.app.ActionBar;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.widget.Toast;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import moe.shizuku.fcmformojo.FFMApplication;
import moe.shizuku.fcmformojo.FFMSettings;
import moe.shizuku.fcmformojo.R;
import moe.shizuku.fcmformojo.model.FFMResult;
import moe.shizuku.fcmformojo.utils.LocalBroadcast;
import moe.shizuku.preference.Preference;

import static moe.shizuku.fcmformojo.FFMApplication.FFMService;

/**
 * Created by rikka on 2017/8/21.
 */

public class ServerSettingsFragment extends SettingsFragment {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        super.onCreatePreferences(savedInstanceState, rootKey);

        addPreferencesFromResource(R.xml.manage_server);

        findPreference("restart_webqq").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(final Preference preference) {
                preference.setEnabled(false);
                restart();
                return true;
            }
        });

        findPreference("stop_webqq").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(final Preference preference) {
                preference.setEnabled(false);
                stop();
                return true;
            }
        });
    }

    private void restart() {
        mCompositeDisposable.add(FFMService.restart()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doFinally(new Action() {
                    @Override
                    public void run() throws Exception {
                        findPreference("restart_webqq").setEnabled(true);
                    }
                })
                .subscribe(new Consumer<FFMResult>() {
                    @Override
                    public void accept(FFMResult ffmResult) throws Exception {
                        Toast.makeText(getContext(), "Succeed.", Toast.LENGTH_SHORT).show();
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        Toast.makeText(getContext(), "Network error:\n" + throwable.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }));
    }

    private void stop() {
        mCompositeDisposable.add(FFMService.stop()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doFinally(new Action() {
                    @Override
                    public void run() throws Exception {
                        findPreference("stop_webqq").setEnabled(true);
                    }
                })
                .subscribe(new Consumer<FFMResult>() {
                    @Override
                    public void accept(FFMResult ffmResult) throws Exception {
                        Toast.makeText(getContext(), "Succeed.", Toast.LENGTH_SHORT).show();
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        Toast.makeText(getContext(), "Network error:\n" + throwable.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }));
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
