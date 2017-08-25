package moe.shizuku.fcmformojo.settings;

import android.annotation.TargetApi;
import android.app.ActionBar;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import moe.shizuku.fcmformojo.FFMApplication;
import moe.shizuku.fcmformojo.FFMSettings;
import moe.shizuku.fcmformojo.FFMSettings.ForegroundImpl;
import moe.shizuku.fcmformojo.R;
import moe.shizuku.fcmformojo.model.FFMResult;
import moe.shizuku.fcmformojo.model.NotificationToggle;
import moe.shizuku.fcmformojo.profile.Profile;
import moe.shizuku.fcmformojo.profile.ProfileList;
import moe.shizuku.fcmformojo.service.FFMIntentService;
import moe.shizuku.fcmformojo.utils.UsageStatsUtils;
import moe.shizuku.preference.ListPreference;
import moe.shizuku.preference.Preference;
import moe.shizuku.preference.SwitchPreference;
import moe.shizuku.privileged.api.PrivilegedAPIs;

import static moe.shizuku.fcmformojo.FFMApplication.FFMService;

/**
 * Created by rikka on 2017/8/21.
 */

public class NotificationSettingsFragment extends SettingsFragment {

    private SwitchPreference mFriendToggle;
    private SwitchPreference mGroupToggle;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        super.onCreatePreferences(savedInstanceState, rootKey);

        addPreferencesFromResource(R.xml.manage_notification);

        mFriendToggle = (SwitchPreference) findPreference("notification");
        mGroupToggle = (SwitchPreference) findPreference("notification_group");

        List<CharSequence> names = new ArrayList<>();
        List<CharSequence> packages = new ArrayList<>();
        for (Profile profile : ProfileList.getProfile()) {
            names.add(getContext().getString(profile.getDisplayName()));
            packages.add(profile.getPackageName());
        }

        ListPreference qq = (ListPreference) findPreference("qq_package");
        qq.setEntries(names.toArray(new CharSequence[names.size()]));
        qq.setEntryValues(packages.toArray(new CharSequence[packages.size()]));

        findPreference("update_avatar").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Toast.makeText(getContext(), "Progress will be shown via notification", Toast.LENGTH_SHORT).show();
                FFMIntentService.startUpdateIcon(getContext(), null);
                return true;
            }
        });

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            findPreference("edit_channel").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @TargetApi(Build.VERSION_CODES.O)
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    Intent intent = new Intent(Settings.ACTION_CHANNEL_NOTIFICATION_SETTINGS);
                    intent.putExtra(Settings.EXTRA_CHANNEL_ID, "friend_message_channel");
                    intent.putExtra(Settings.EXTRA_APP_PACKAGE, getActivity().getPackageName());
                    startActivity(intent);
                    return true;
                }
            });

            findPreference("edit_channel_group").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @TargetApi(Build.VERSION_CODES.O)
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    Intent intent = new Intent(Settings.ACTION_CHANNEL_NOTIFICATION_SETTINGS);
                    intent.putExtra(Settings.EXTRA_CHANNEL_ID, "group_message_channel");
                    intent.putExtra(Settings.EXTRA_APP_PACKAGE, getActivity().getPackageName());
                    startActivity(intent);
                    return true;
                }
            });
        }

        mCompositeDisposable.add(FFMService
                .getNotificationsToggle()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<NotificationToggle>() {
                    @Override
                    public void accept(NotificationToggle toggle) throws Exception {
                        mFriendToggle.setChecked(toggle.isFriendEnable());
                        mGroupToggle.setChecked(toggle.isGroupEnable());

                        mFriendToggle.setEnabled(true);
                        mGroupToggle.setEnabled(true);
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
        inflater.inflate(R.menu.upload, menu);

        ActionBar actionBar = getActivity().getActionBar();
        if (actionBar != null) {
            actionBar.setTitle(R.string.notification_settings);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_upload:
                mCompositeDisposable.add(FFMService
                        .updateNotificationsToggle(NotificationToggle.create(mFriendToggle.isChecked(), mGroupToggle.isChecked()))
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Consumer<FFMResult>() {
                            @Override
                            public void accept(FFMResult result) throws Exception {
                                Toast.makeText(getContext(), "Succeed.", Toast.LENGTH_SHORT).show();

                                Log.d("Sync", "updateNotificationsToggle success");
                            }
                        }, new Consumer<Throwable>() {
                            @Override
                            public void accept(Throwable throwable) throws Exception {
                                Toast.makeText(getContext(), "Network error:\n" + throwable.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }));
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onResume() {
        super.onResume();
        getPreferenceScreen().getSharedPreferences()
                .registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        getPreferenceScreen().getSharedPreferences()
                .unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        switch (key) {
            case FFMSettings.GET_FOREGROUND:
                switch (sharedPreferences.getString(key, ForegroundImpl.NONE)) {
                    case ForegroundImpl.USAGE_STATS:
                        if (!UsageStatsUtils.granted(getContext())) {
                            getContext().startActivity(new Intent(android.provider.Settings.ACTION_USAGE_ACCESS_SETTINGS));
                        }
                        break;
                    case ForegroundImpl.SHIZUKU:
                        PrivilegedAPIs.setPermitNetworkThreadPolicy();
                        if (!FFMApplication.sPrivilegedAPIs.authorized()) {
                            FFMApplication.sPrivilegedAPIs.requstAuthorization(getActivity());
                        }
                        break;
                }
                break;
        }
    }
}
