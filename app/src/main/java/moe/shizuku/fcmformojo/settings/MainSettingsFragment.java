package moe.shizuku.fcmformojo.settings;

import android.app.ActionBar;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;

import moe.shizuku.fcmformojo.BaseActivity;
import moe.shizuku.fcmformojo.R;
import moe.shizuku.preference.Preference;

/**
 * Created by Rikka on 2017/4/22.
 */

public class MainSettingsFragment extends SettingsFragment {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        super.onCreatePreferences(savedInstanceState, rootKey);

        addPreferencesFromResource(R.xml.main);

        findPreference("server_settings").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                getFragmentManager().beginTransaction()
                        .setCustomAnimations(R.animator.dir_enter, R.animator.dir_leave, R.animator.dir_enter, R.animator.dir_leave)
                        .add(android.R.id.content, new ServerSettingsFragment())
                        .addToBackStack(null)
                        .commit();
                return true;
            }
        });

        findPreference("notification_settings").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                getFragmentManager().beginTransaction()
                        .setCustomAnimations(R.animator.dir_enter, R.animator.dir_leave, R.animator.dir_enter, R.animator.dir_leave)
                        .add(android.R.id.content, new NotificationSettingsFragment())
                        .addToBackStack(null)
                        .commit();
                return true;
            }
        });

        findPreference("donate").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                ((BaseActivity) getActivity()).onOptionsItemSelected(R.id.action_donate);
                return true;
            }
        });

        findPreference("about").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                ((BaseActivity) getActivity()).onOptionsItemSelected(R.id.action_about);
                return true;
            }
        });
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
            actionBar.setTitle(R.string.activity_name);
            actionBar.setDisplayHomeAsUpEnabled(false);
        }
    }
}
