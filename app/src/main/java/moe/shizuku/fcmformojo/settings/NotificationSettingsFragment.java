package moe.shizuku.fcmformojo.settings;

import android.app.ActionBar;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import moe.shizuku.api.ShizukuClient;
import moe.shizuku.fcmformojo.FFMApplication;
import moe.shizuku.fcmformojo.FFMSettings;
import moe.shizuku.fcmformojo.FFMSettings.ForegroundImpl;
import moe.shizuku.fcmformojo.R;
import moe.shizuku.fcmformojo.model.NotificationToggle;
import moe.shizuku.fcmformojo.profile.Profile;
import moe.shizuku.fcmformojo.profile.ProfileList;
import moe.shizuku.fcmformojo.service.FFMIntentService;
import moe.shizuku.fcmformojo.utils.UsageStatsUtils;
import moe.shizuku.preference.ListPreference;
import moe.shizuku.preference.Preference;
import moe.shizuku.preference.SwitchPreference;

import static moe.shizuku.fcmformojo.FFMApplication.FFMService;

/**
 * Created by rikka on 2017/8/21.
 */

public class NotificationSettingsFragment extends SettingsFragment {

    private SwitchPreference mFriendToggle;
    private SwitchPreference mGroupToggle;

    private ListPreference mForegroundList;

    private NotificationToggle mServerNotificationToggle;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        super.onCreatePreferences(savedInstanceState, rootKey);

        addPreferencesFromResource(R.xml.manage_notification);

        mFriendToggle = (SwitchPreference) findPreference("notification");
        mGroupToggle = (SwitchPreference) findPreference("notification_group");
        mForegroundList = (ListPreference) findPreference("get_foreground");

        List<CharSequence> names = new ArrayList<>();
        List<CharSequence> packages = new ArrayList<>();
        for (Profile profile : ProfileList.getProfile()) {
            names.add(requireContext().getString(profile.getDisplayName()));
            packages.add(profile.getPackageName());
        }

        ListPreference qq = (ListPreference) findPreference("qq_package");
        qq.setEntries(names.toArray(new CharSequence[names.size()]));
        qq.setEntryValues(packages.toArray(new CharSequence[packages.size()]));

        findPreference("update_avatar").setOnPreferenceClickListener(preference -> {
            Toast.makeText(getContext(), "Progress will be shown via notification", Toast.LENGTH_SHORT).show();
            FFMIntentService.startUpdateIcon(requireContext(), null);
            return true;
        });

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            findPreference("edit_channel").setOnPreferenceClickListener(preference -> {
                Intent intent = new Intent(Settings.ACTION_CHANNEL_NOTIFICATION_SETTINGS);
                intent.putExtra(Settings.EXTRA_CHANNEL_ID, "friend_message_channel");
                intent.putExtra(Settings.EXTRA_APP_PACKAGE, requireContext().getPackageName());
                startActivity(intent);
                return true;
            });

            findPreference("edit_channel_group").setOnPreferenceClickListener(preference -> {
                Intent intent = new Intent(Settings.ACTION_CHANNEL_NOTIFICATION_SETTINGS);
                intent.putExtra(Settings.EXTRA_CHANNEL_ID, "group_message_channel");
                intent.putExtra(Settings.EXTRA_APP_PACKAGE, requireContext().getPackageName());
                startActivity(intent);
                return true;
            });
        }

        Preference.OnPreferenceChangeListener pushListener = (preference, newValue) -> {
            getListView().post(() -> uploadNotificationsToggle(preference));
            return true;
        };

        mFriendToggle.setOnPreferenceChangeListener(pushListener);
        mGroupToggle.setOnPreferenceChangeListener(pushListener);

        fetchRemoteConfiguration();
    }

    private void fetchRemoteConfiguration() {
        mCompositeDisposable.add(FFMService
                .getNotificationsToggle()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(toggle -> {
                    mServerNotificationToggle = toggle;

                    mFriendToggle.setChecked(toggle.isFriendEnable());
                    mGroupToggle.setChecked(toggle.isGroupEnable());

                    mFriendToggle.setEnabled(true);
                    mGroupToggle.setEnabled(true);
                }, throwable -> Toast.makeText(getContext(), "Network error:\n" + throwable.getMessage(), Toast.LENGTH_SHORT).show()));
    }

    private void uploadNotificationsToggle(final Preference preference) {
        final NotificationToggle newNotificationToggle = NotificationToggle.create(mFriendToggle.isChecked(), mGroupToggle.isChecked());
        if (newNotificationToggle.equals(mServerNotificationToggle)) {
            return;
        }

        preference.setEnabled(false);

        mCompositeDisposable.add(FFMService
                .updateNotificationsToggle(NotificationToggle.create(mFriendToggle.isChecked(), mGroupToggle.isChecked()))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doFinally(() -> preference.setEnabled(true))
                .subscribe(result -> {
                    mServerNotificationToggle = newNotificationToggle;

                    //Toast.makeText(getContext(), "Succeed.", Toast.LENGTH_SHORT).show();

                    Log.d("Sync", "updateNotificationsToggle success, new state: " + newNotificationToggle);
                }, throwable -> {
                    Toast.makeText(getContext(), "Network error:\n" + throwable.getMessage(), Toast.LENGTH_SHORT).show();

                    Log.w("Sync", "updateNotificationsToggle failed", throwable);
                }));
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        ActionBar actionBar = requireActivity().getActionBar();
        if (actionBar != null) {
            actionBar.setTitle(R.string.notification_settings);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        getPreferenceScreen().getSharedPreferences()
                .registerOnSharedPreferenceChangeListener(this);

        int group = FFMSettings.getLocalGroupWhitelistValue();
        if (group == -1) {
            findPreference(FFMSettings.LOCAL_GROUP_WHITELIST)
                    .setSummary(R.string.settings_per_group_summary_off);
        } else {
            findPreference(FFMSettings.LOCAL_GROUP_WHITELIST)
                    .setSummary(requireContext().getResources().getQuantityString(R.plurals.settings_per_group_summary_on, group, group));
        }

        int discuss = FFMSettings.getLocalDiscussWhitelistValue();
        if (discuss == -1) {
            findPreference(FFMSettings.LOCAL_DISCUSS_WHITELIST)
                    .setSummary(R.string.settings_per_discuss_summary_off);
        } else {
            findPreference(FFMSettings.LOCAL_DISCUSS_WHITELIST)
                    .setSummary(requireContext().getResources().getQuantityString(R.plurals.settings_per_discuss_summary_on, discuss, discuss));
        }
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
                        if (!UsageStatsUtils.granted(requireContext())) {
                            requireContext().startActivity(new Intent(android.provider.Settings.ACTION_USAGE_ACCESS_SETTINGS));
                        }
                    case ForegroundImpl.NONE:
                        AsyncTask.execute(() -> FFMApplication.get(requireContext()).unregisterTaskStackListener());
                        break;
                    case ForegroundImpl.SHIZUKU:
                        if (ShizukuClient.getManagerVersion(requireContext()) < 106) {
                            mForegroundList.setValue(ForegroundImpl.NONE);

                            Toast.makeText(requireContext(), "Shizuku version too low", Toast.LENGTH_SHORT).show();
                            break;
                        }

                        Single
                                .fromCallable(ShizukuClient::getState)
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(state -> {
                                    if (isDetached()) {
                                        return;
                                    }

                                    if (!ShizukuClient.getState().isAuthorized()) {
                                        if (!ShizukuClient.checkSelfPermission(getContext())) {
                                            ShizukuClient.requestPermission(NotificationSettingsFragment.this);
                                        } else {
                                            ShizukuClient.requestAuthorization(NotificationSettingsFragment.this);
                                        }
                                    }
                                });

                        break;
                }
                break;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case ShizukuClient.REQUEST_CODE_PERMISSION:
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    ShizukuClient.requestAuthorization(this);
                } else {
                    // denied
                    mForegroundList.setValue(ForegroundImpl.NONE);
                }
                break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case ShizukuClient.REQUEST_CODE_AUTHORIZATION:
                if (resultCode == ShizukuClient.AUTH_RESULT_OK) {
                    ShizukuClient.setToken(data);
                    FFMSettings.putToken(ShizukuClient.getToken());

                    AsyncTask.execute(() -> FFMApplication.get(requireContext()).registerTaskStackListener());
                } else {
                    // error
                    mForegroundList.setValue(ForegroundImpl.NONE);
                }
                return;
            default:
                super.onActivityResult(requestCode, resultCode, data);
        }
    }
}
