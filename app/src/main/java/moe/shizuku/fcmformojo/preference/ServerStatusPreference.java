package moe.shizuku.fcmformojo.preference;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.support.annotation.AttrRes;
import android.support.annotation.ColorInt;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.CardView;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import moe.shizuku.fcmformojo.BuildConfig;
import moe.shizuku.fcmformojo.FFMSettings;
import moe.shizuku.fcmformojo.R;
import moe.shizuku.fcmformojo.model.FFMStatus;
import moe.shizuku.preference.Preference;
import moe.shizuku.preference.PreferenceViewHolder;

import static moe.shizuku.fcmformojo.FFMApplication.FFMService;
import static moe.shizuku.fcmformojo.FFMStatic.ACTION_REFRESH_STATUS;

/**
 * Created by rikka on 2017/8/21.
 */

public class ServerStatusPreference extends Preference {

    private Disposable mDisposable;
    private PreferenceViewHolder mViewHolder;

    public ServerStatusPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);

        BroadcastReceiver refreshBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                refresh();
            }
        };
        LocalBroadcastManager.getInstance(context)
                .registerReceiver(refreshBroadcastReceiver, new IntentFilter(ACTION_REFRESH_STATUS));
    }

    public ServerStatusPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public ServerStatusPreference(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.dialogPreferenceStyle);
    }

    public ServerStatusPreference(Context context) {
        this(context, null);
    }

    @Override
    public void onBindViewHolder(PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);

        if (mViewHolder == null) {
            refresh();
        }

        mViewHolder = holder;
        mViewHolder.itemView.setOnClickListener(null);

        ((ViewGroup) mViewHolder.itemView).getChildAt(0).setOnClickListener(view -> refresh());
    }

    private void updateTokenChanged() {
        if (mViewHolder != null) {
            View tokenChangedCard = ((ViewGroup) mViewHolder.itemView).getChildAt(2);

            if (FFMSettings.getNewToken() != null) {
                tokenChangedCard.setVisibility(View.VISIBLE);
            } else {
                tokenChangedCard.setVisibility(View.GONE);
            }
        }
    }

    private void updateVersion(String server) {
        if (mViewHolder != null) {
            View versionCard = ((ViewGroup) mViewHolder.itemView).getChildAt(1);

            if (server != null
                    && !server.equals(BuildConfig.REQUIRE_SERVER_VERSION)) {
                TextView status = versionCard.findViewById(android.R.id.text2);
                status.setText(getContext().getString(R.string.status_version_not_match, server, BuildConfig.REQUIRE_SERVER_VERSION));

                versionCard.setVisibility(View.VISIBLE);
            } else {
                versionCard.setVisibility(View.GONE);
            }
        }
    }

    private void updateStatus(CharSequence text, @AttrRes int attr, Drawable icon) {
        if (mViewHolder != null) {
            CardView statusCard = (CardView) ((ViewGroup) mViewHolder.itemView).getChildAt(0);
            TextView status = statusCard.findViewById(android.R.id.text1);

            if (icon != null) {
                icon.setBounds(0, 0, icon.getIntrinsicWidth(), icon.getIntrinsicHeight());
                status.setCompoundDrawablesRelative(icon, null, null, null);
            } else {
                status.setCompoundDrawablesRelative(null, null, null, null);
            }
            status.setText(text);

            int[] attrs = {attr};
            TypedArray a = getContext().obtainStyledAttributes(attrs);
            int color = a.getColor(0, 0);
            a.recycle();

            statusCard.setCardBackgroundColor(color);
        }
    }

    private void updateStatus(int count) {
        Context context = getContext();

        if (count > 0) {
            Drawable icon = context.getDrawable(R.drawable.ic_status_ok_24dp);
            String text = context.getResources().getQuantityString(R.plurals.status_running, count, count);

            updateStatus(text, R.attr.colorSafe, icon);
        } else {
            Drawable icon = context.getDrawable(R.drawable.ic_status_error_24dp);
            String text = context.getString(R.string.status_running_no_device);

            updateStatus(text, R.attr.colorWarning, icon);
        }
    }

    private void updateStatus(FFMStatus status) {
        if (status.getGroupBlacklist() != null) {
            FFMSettings.putLocalGroupWhitelistValue(status.getGroupBlacklist().isEnabled() ? status.getGroupBlacklist().getCount() : -1);
        }

        if (status.getDiscussWhitelist() != null) {
            FFMSettings.putLocalDiscussWhitelistValue(status.getDiscussWhitelist().isEnabled() ? status.getDiscussWhitelist().getCount() : -1);
        }

        if (status.isRunning()) {
            updateStatus(status.getDevices());
        } else {
            Context context = getContext();

            Drawable icon = context.getDrawable(R.drawable.ic_status_error_24dp);
            updateStatus(context.getString(R.string.status_webqq_dead), R.attr.colorSafe, icon);
        }
    }

    private void updateStatus(String error) {
        Context context = getContext();
        Drawable icon = context.getDrawable(R.drawable.ic_status_error_24dp);
        updateStatus(context.getString(R.string.status_cannot_connect_server_error, error), R.attr.colorAlert, icon);
    }

    private void refresh() {
        if (mDisposable != null && !mDisposable.isDisposed()) {
            return;
        }

        mDisposable = FFMService.getStatus()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(status -> {
                    if (status != null) {
                        updateStatus(status);
                        updateVersion(status.getVersion());
                    }
                    updateTokenChanged();
                }, throwable -> {
                    updateStatus(throwable.getMessage());
                    updateVersion(null);
                    updateTokenChanged();
                });
    }
}
