package moe.shizuku.fcmformojo.preference;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.webkit.URLUtil;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;

import moe.shizuku.fcmformojo.R;
import moe.shizuku.fcmformojo.utils.URLFormatUtils;
import moe.shizuku.preference.PreferenceDialogFragment;

/**
 * DialogFragment for {@link HttpUriPreference}
 * @author Haruue Icymoon haruue@caoyue.com.cn
 */

public class HttpUriPreferenceDialogFragment extends PreferenceDialogFragment {

    public static final String ARG_KEY_USERNAME = "key_username";
    public static final String ARG_KEY_PASSWORD = "key_password";

    private CheckBox advancedOptionsCheckBox;
    private LinearLayout advancedOptionsContainer;

    private EditText uriEditText;
    private EditText usernameEditText;
    private EditText passwordEditText;

    private AlertDialog dialog;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final Context context = getActivity();

        final AlertDialog.Builder builder = new AlertDialog.Builder(context)
                .setTitle(getPreference().getDialogTitle())
                .setIcon(getPreference().getDialogIcon())
                .setPositiveButton(getPreference().getPositiveButtonText(), this)
                .setNegativeButton(getPreference().getNegativeButtonText(), this);

        View contentView = onCreateDialogView(context);
        if (contentView != null) {
            onBindDialogView(contentView);
            builder.setView(contentView);
        } else {
            builder.setMessage(getPreference().getDialogMessage());
        }

        this.dialog = builder.create();

        return dialog;
    }

    @Override
    public AlertDialog getDialog() {
        return dialog;
    }

    @Override
    protected void onBindDialogView(View view) {
        super.onBindDialogView(view);
        // bind view
        advancedOptionsCheckBox = view.findViewById(R.id.cb_advanced_options);
        advancedOptionsContainer = view.findViewById(R.id.ll_advanced_options_container);
        uriEditText = view.findViewById(R.id.et_uri);
        usernameEditText = view.findViewById(R.id.et_http_username);
        passwordEditText = view.findViewById(R.id.et_http_password);
        // focus & ime
        uriEditText.requestFocus();
        uriEditText.post(new Runnable() {
            @Override
            public void run() {
                InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.showSoftInput(uriEditText, InputMethodManager.SHOW_IMPLICIT);
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        // After this line, dialog and views are all initialized
        // check valid uri
        uriEditText.addTextChangedListener(new UriFormattingTextWatcher() {
            @Override
            public void onTextChanged(boolean isValidUri) {
                AlertDialog d = getDialog();
                d.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(isValidUri);
            }
        });
        // default value
        uriEditText.setText(getHttpUriPreference().getUri());
        usernameEditText.setText(getHttpUriPreference().getUsername());
        passwordEditText.setText(getHttpUriPreference().getPassword());
        // advanced options expand
        advancedOptionsCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    advancedOptionsCheckBox.setVisibility(View.GONE);
                    advancedOptionsContainer.setVisibility(View.VISIBLE);
                }/* else {
                    advancedOptionsContainer.setVisibility(View.GONE);
                }*/
            }
        });
        if (usernameEditText.length() != 0) {
            advancedOptionsCheckBox.setChecked(true);
        }
    }

    private HttpUriPreference getHttpUriPreference() {
        return (HttpUriPreference) getPreference();
    }

    @Override
    public void onDialogClosed(boolean positiveResult) {
        if (positiveResult) {
            String uri = uriEditText.getText().toString();
            if (getHttpUriPreference().callChangeListener(uri)) {
                getHttpUriPreference().setUri(uri);
            }
            String username = usernameEditText.getText().toString();
            if (getHttpUriPreference().callOnUsernameChangeListener(username)) {
                getHttpUriPreference().setUsername(username);
            }
            String password = passwordEditText.getText().toString();
            if (getHttpUriPreference().callOnPasswordChangeListener(password)) {
                getHttpUriPreference().setPassword(password);
            }
        }
    }

    private abstract static class UriFormattingTextWatcher implements TextWatcher {

        public abstract void onTextChanged(boolean isValidUri);

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {}

        @Override
        public void afterTextChanged(Editable s) {
            String stringToCheck = s.toString();
            onTextChanged(URLFormatUtils.isValidURL(stringToCheck));
        }
    }
}
