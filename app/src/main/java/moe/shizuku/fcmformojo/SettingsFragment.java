package moe.shizuku.fcmformojo;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Html;

import com.google.firebase.iid.FirebaseInstanceId;

import moe.shizuku.fcmformojo.utils.ClipboardUtils;
import moe.shizuku.fcmformojo.utils.UsageStatsUtils;
import moe.shizuku.support.preference.Preference;
import moe.shizuku.support.preference.PreferenceFragment;

/**
 * Created by Rikka on 2017/4/22.
 */

public class SettingsFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        getPreferenceManager().setSharedPreferencesName("settings");
        getPreferenceManager().setSharedPreferencesMode(Context.MODE_PRIVATE);

        addPreferencesFromResource(R.xml.preference);

        findPreference("view_token").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                new AlertDialog.Builder(getContext())
                        .setMessage(Html.fromHtml("<font face=\"monospace\">" + FirebaseInstanceId.getInstance().getToken() + "</font>", Html.TO_HTML_PARAGRAPH_LINES_CONSECUTIVE))
                        .setPositiveButton(android.R.string.copy, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                ClipboardUtils.put(getContext(), FirebaseInstanceId.getInstance().getToken());
                            }
                        })
                        .setNeutralButton(R.string.share, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                startActivity(Intent.createChooser(new Intent(Intent.ACTION_SEND)
                                                .putExtra(Intent.EXTRA_TEXT, FirebaseInstanceId.getInstance().getToken())
                                                .setType("text/plain")
                                        , getContext().getString(R.string.share)));
                            }
                        })
                        .setNegativeButton(android.R.string.cancel, null)
                        .show();
                return true;
            }
        });

        findPreference("update_friend_face").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                ((MainActivity) getActivity()).refreshHeads();
                return true;
            }
        });

        findPreference(FFMSettings.QQ_PACKAGE).setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                if (!UsageStatsUtils.granted(getContext())) {
                    getContext().startActivity(new Intent(android.provider.Settings.ACTION_USAGE_ACCESS_SETTINGS));
                    return true;
                }
                return false;
            }
        });
    }

    @Override
    public DividerDecoration onCreateItemDecoration() {
        return new CategoryDivideDividerDecoration();
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
            case FFMSettings.BASE_URL:
                FFMApplication.get(getContext()).updateBaseUrl(FFMSettings.getBaseUrl());
                break;
        }
    }
}
