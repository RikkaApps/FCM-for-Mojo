package moe.shizuku.fcmformojo.settings;

import android.os.Bundle;
import android.widget.Toast;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import moe.shizuku.fcmformojo.R;
import moe.shizuku.fcmformojo.model.Account;
import moe.shizuku.preference.EditTextPreference;

import static moe.shizuku.fcmformojo.FFMApplication.FFMService;

public class AccountSettingsFragment extends SettingsFragment {

    private EditTextPreference mAccount;
    private EditTextPreference mPassword;

    private Account mAccountInfo;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        super.onCreatePreferences(savedInstanceState, rootKey);

        addPreferencesFromResource(R.xml.manage_account);

        mAccount = (EditTextPreference) findPreference("qq_account");
        mPassword = (EditTextPreference) findPreference("qq_password");

        mAccount.setOnPreferenceChangeListener((preference, newValue) -> {
            if (newValue instanceof String) {
                updateRemoteConfiguration(new Account((String) newValue, mAccountInfo.getPassword()));
            }
            return true;
        });

        mPassword.setOnPreferenceChangeListener((preference, newValue) -> {
            if (newValue instanceof String) {
                updateRemoteConfiguration(new Account(mAccountInfo.getAccount(), new Account.Password((String) newValue, null)));
            }
            return true;
        });

        fetchRemoteConfiguration();
    }

    private void updateData(Account accountInfo) {
        mAccount.setText(accountInfo.getAccount());
        mPassword.setText(accountInfo.getPassword().getRaw());
        mAccount.setEnabled(true);
        mPassword.setEnabled(true);
    }

    private void updateRemoteConfiguration(Account accountInfo) {
        mAccount.setEnabled(false);
        mPassword.setEnabled(false);

        mCompositeDisposable.add(FFMService.updateAccountInfo(accountInfo)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(result -> {
                    mAccountInfo = accountInfo;
                    updateData(mAccountInfo);
                    Toast.makeText(getContext(), R.string.toast_succeeded, Toast.LENGTH_SHORT).show();
                }, throwable -> {
                    updateData(mAccountInfo);
                    Toast.makeText(getContext(), "Network error:\n" + throwable.getMessage(), Toast.LENGTH_SHORT).show();
                }));
    }

    private void fetchRemoteConfiguration() {
        mCompositeDisposable.add(FFMService
                .getAccountInfo()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(account -> {
                    mAccountInfo = account;
                    updateData(mAccountInfo);
                }, throwable -> Toast.makeText(getContext(), "Network error:\n" + throwable.getMessage(), Toast.LENGTH_SHORT).show()));
    }
}
