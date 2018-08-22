package moe.shizuku.fcmformojo.settings;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.ViewGroup;
import android.widget.Toast;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import moe.shizuku.fcmformojo.R;
import moe.shizuku.fcmformojo.model.Account;
import moe.shizuku.fcmformojo.model.openqq.User;
import moe.shizuku.fcmformojo.service.FFMIntentService;
import moe.shizuku.fcmformojo.utils.ViewUtils;
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

        findPreference("update_avatar").setOnPreferenceClickListener(preference -> {
            Dialog.OnClickListener listener = (dialog, which) -> {
                if (which == AlertDialog.BUTTON_POSITIVE) {
                    FFMIntentService.startUpdateIcon(requireContext(), false);
                } else {
                    FFMIntentService.startUpdateIcon(requireContext(), true);
                }

                Toast.makeText(getContext(), "Progress will be shown via notification", Toast.LENGTH_SHORT).show();
            };

            new AlertDialog.Builder(requireContext())
                    .setMessage(R.string.dialog_skip_exists_avatar)
                    .setPositiveButton(R.string.yes, listener)
                    .setNegativeButton(R.string.no, listener)
                    .setNeutralButton(android.R.string.cancel, null)
                    .setCancelable(false)
                    .show();
            return true;
        });

        fetchRemoteConfiguration();
    }

    @Override
    public RecyclerView onCreateRecyclerView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        RecyclerView recyclerView = super.onCreateRecyclerView(inflater, parent, savedInstanceState);
        ViewUtils.setPaddingVertical(recyclerView, getResources().getDimensionPixelSize(R.dimen.dp_8));
        return recyclerView;
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
                    User.setSelf(new User(0, Long.parseLong(accountInfo.getAccount()), null));
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

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        ActionBar actionBar = requireActivity().getActionBar();
        if (actionBar != null) {
            actionBar.setTitle(R.string.account_settings);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }
}
