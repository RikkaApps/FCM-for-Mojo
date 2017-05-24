package moe.shizuku.fcmformojo.preference;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.res.TypedArrayUtils;
import android.text.TextUtils;
import android.util.AttributeSet;

import moe.shizuku.fcmformojo.R;
import moe.shizuku.support.preference.DialogPreference;
import moe.shizuku.support.preference.PreferenceDialogFragment;
import moe.shizuku.support.utils.Settings;

/**
 * Preference to set http uri, http username and http password
 * @author Haruue Icymoon haruue@caoyue.com.cn
 */

@SuppressWarnings({"RestrictedApi", "WeakerAccess", "unused"})
public class HttpUriPreference extends DialogPreference {

    private String uri;
    private String username;
    private String password;

    private String mSummary;
    private String mSummaryWhenAuthorizationSet;
    private String keyUsername;
    private String keyPassword;

    private OnPreferenceChangeListener onUsernameChangeListener;
    private OnPreferenceChangeListener onPasswordChangeListener;

    public HttpUriPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        TypedArray a;

        a = context.obtainStyledAttributes(attrs, R.styleable.HttpUriPreference);
        mSummaryWhenAuthorizationSet = a.getString(R.styleable.HttpUriPreference_summaryWhenAuthorizationSet);
        keyUsername = a.getString(R.styleable.HttpUriPreference_keyHttpUsername);
        keyPassword = a.getString(R.styleable.HttpUriPreference_keyHttpPassword);
        a.recycle();

        a = context.obtainStyledAttributes(attrs,
                R.styleable.Preference, defStyleAttr, defStyleRes);
        mSummary = TypedArrayUtils.getString(a, R.styleable.Preference_summary,
                R.styleable.Preference_android_summary);
        a.recycle();

        setDialogLayoutResource(R.layout.dialog_http_uri_preference);

    }

    public HttpUriPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public HttpUriPreference(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.dialogPreferenceStyle);
    }

    public HttpUriPreference(Context context) {
        this(context, null);
    }

    @NonNull
    @Override
    protected DialogFragment onCreateDialogFragment(String key) {
        final HttpUriPreferenceDialogFragment fragment = new HttpUriPreferenceDialogFragment();
        final Bundle b  = new Bundle(1);
        b.putString(PreferenceDialogFragment.ARG_KEY, key);
        b.putString(HttpUriPreferenceDialogFragment.ARG_KEY_USERNAME, keyUsername);
        b.putString(HttpUriPreferenceDialogFragment.ARG_KEY_PASSWORD, keyPassword);
        fragment.setArguments(b);
        return fragment;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        final boolean wasBlocking = shouldDisableDependents();
        final boolean changed = !TextUtils.equals(this.uri, uri);
        this.uri = uri;

        if (changed) {
            persistString(uri);
            notifyChanged();
        }

        final boolean isBlocking = shouldDisableDependents();
        if (isBlocking != wasBlocking) {
            notifyDependencyChange(isBlocking);
        }
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        final boolean changed = !TextUtils.equals(this.username, username);
        this.username = username;

        if (changed) {
            setString(keyUsername, username);
            notifyChanged();
        }
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        final boolean changed = !TextUtils.equals(this.password, password);
        this.password = password;

        if (changed) {
            setString(keyPassword, password);
            notifyChanged();
        }
    }

    private void setString(String key, String value) {
        Settings.putString(key, value);
    }

    public void setOnUsernameChangeListener(OnPreferenceChangeListener onUsernameChangeListener) {
        this.onUsernameChangeListener = onUsernameChangeListener;
    }

    public void setOnPasswordChangeListener(OnPreferenceChangeListener onPasswordChangeListener) {
        this.onPasswordChangeListener = onPasswordChangeListener;
    }

    public boolean callOnUsernameChangeListener(Object newValue) {
        return onUsernameChangeListener == null || onUsernameChangeListener.onPreferenceChange(this, newValue);
    }

    public boolean callOnPasswordChangeListener(Object newValue) {
        return onPasswordChangeListener == null || onPasswordChangeListener.onPreferenceChange(this, newValue);
    }

    @Override
    protected Object onGetDefaultValue(TypedArray a, int index) {
        return a.getString(index);
    }

    @Override
    protected void onSetInitialValue(boolean restorePersistedValue, Object defaultValue) {
        setUri(restorePersistedValue ? getPersistedString(uri) : (String) defaultValue);
        setUsername(Settings.getString(keyUsername, null));
        setPassword(Settings.getString(keyPassword, null));
    }


    @Override
    public CharSequence getSummary() {
        final CharSequence uri = getUri();
        final CharSequence username = getUsername();
        if (mSummary == null) {
            return super.getSummary();
        } else {
            if (uri != null) {
                if (username != null && username.length() != 0) {
                    return String.format(mSummaryWhenAuthorizationSet, uri);
                } else {
                    return String.format(mSummary, uri);
                }
            } else {
                return "";
            }
        }
    }


    @Override
    public void setSummary(CharSequence summary) {
        super.setSummary(summary);
        if (summary == null && mSummary != null) {
            mSummary = null;
        } else if (summary != null && !summary.equals(mSummary)) {
            mSummary = summary.toString();
        }
    }
}
