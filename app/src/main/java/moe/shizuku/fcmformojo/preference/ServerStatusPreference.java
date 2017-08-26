package moe.shizuku.fcmformojo.preference;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
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

    private BroadcastReceiver mRefreshBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            refresh();
        }
    };

    public ServerStatusPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);

        LocalBroadcastManager.getInstance(context)
                .registerReceiver(mRefreshBroadcastReceiver, new IntentFilter(ACTION_REFRESH_STATUS));
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

        ((ViewGroup) mViewHolder.itemView).getChildAt(0).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                refresh();
            }
        });
    }

    private void updateVersion(String server) {
        if (mViewHolder != null) {
            CardView versionCard = (CardView) ((ViewGroup) mViewHolder.itemView).getChildAt(1);

            if (server != null
                    && !server.equals(BuildConfig.REQUIRE_SERVER_VERSION)) {
                TextView status = versionCard.findViewById(android.R.id.text2);
                status.setText(getContext().getString(R.string.version_not_match, server, BuildConfig.VERSION_NAME));

                versionCard.setVisibility(View.VISIBLE);
            } else {
                versionCard.setVisibility(View.GONE);
            }
        }
    }

    private void updateStatus(CharSequence text, @ColorInt int color, Drawable icon) {
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
            statusCard.setCardBackgroundColor(color);
        }
    }

    private void updateStatus(int count) {
        Context context = getContext();

        if (count > 0) {
            int color = context.getColor(R.color.serverRunning);
            Drawable icon = context.getDrawable(R.drawable.ic_status_ok_24dp);
            String text = context.getResources().getQuantityString(R.plurals.status_running, count, count);

            updateStatus(text, color, icon);
        } else {
            int color = context.getColor(R.color.serverProblem);
            Drawable icon = context.getDrawable(R.drawable.ic_status_error_24dp);
            String text = context.getString(R.string.status_running_no_device);

            updateStatus(text, color, icon);
        }
    }

    private void updateStatus(FFMStatus status) {
        if (status.isRunning()) {
            updateStatus(status.getDevices());
        } else {
            Context context = getContext();

            int color = context.getColor(R.color.serverProblem);
            Drawable icon = context.getDrawable(R.drawable.ic_status_error_24dp);
            updateStatus(context.getString(R.string.status_webqq_dead), color, icon);
        }

        updateVersion(status.getVersion());
    }

    private void updateStatus(String error) {
        Context context = getContext();
        int color = context.getColor(R.color.serverError);
        Drawable icon = context.getDrawable(R.drawable.ic_status_error_24dp);
        updateStatus(context.getString(R.string.status_cannot_connect_server_error, error), color, icon);
    }

    private void refresh() {
        if (mDisposable != null && !mDisposable.isDisposed()) {
            return;
        }

        mDisposable = FFMService.getStatus()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<FFMStatus>() {
                    @Override
                    public void accept(FFMStatus status) throws Exception {
                        if (status != null) {
                            updateStatus(status);
                        }
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        updateStatus(throwable.getMessage());
                        updateVersion(null);
                    }
                });
    }
}
