package moe.shizuku.fcmformojo.preference;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.support.annotation.ColorInt;
import android.support.v4.content.LocalBroadcastManager;
import android.util.AttributeSet;
import android.widget.TextView;

import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import moe.shizuku.fcmformojo.R;
import moe.shizuku.fcmformojo.model.RegistrationId;
import moe.shizuku.preference.Preference;
import moe.shizuku.preference.PreferenceViewHolder;
import retrofit2.Response;

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
    }

    private void updateStatus(CharSequence text, @ColorInt int color, Drawable icon) {
        if (mViewHolder != null) {
            TextView status = (TextView) mViewHolder.findViewById(android.R.id.text1);
            if (icon != null) {
                icon.setBounds(0, 0, icon.getIntrinsicWidth(), icon.getIntrinsicHeight());
                status.setCompoundDrawablesRelative(icon, null, null, null);
            } else {
                status.setCompoundDrawablesRelative(null, null, null, null);
            }
            status.setText(text);
            status.setTextColor(color);
            status.setCompoundDrawableTintList(ColorStateList.valueOf(color));
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

    private void updateStatus(int code, String body) {
        Context context = getContext();
        int color = context.getColor(R.color.serverProblem);
        Drawable icon = context.getDrawable(R.drawable.ic_status_error_24dp);
        if ("webqq dead".equals(body)) {
            updateStatus(context.getString(R.string.status_webqq_dead), color, icon);
        } else if ("webqq error".equals(body)) {
            updateStatus(context.getString(R.string.status_webqq_error), color, icon);
        } else {
            updateStatus(context.getString(R.string.status_server_error, code, body), color, icon);

        }
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

        mDisposable = FFMService.getRegistrationIdsResponse()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<Response<List<RegistrationId>>>() {
                    @Override
                    public void accept(Response<List<RegistrationId>> response) throws Exception {
                        if (response.isSuccessful() && response.body() != null) {
                            updateStatus(response.body().size());
                        } else {
                            updateStatus(response.code(), response.errorBody().string());
                        }
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        updateStatus(throwable.getMessage());
                    }
                });
    }

    @Override
    protected void onClick() {
        refresh();
    }
}
