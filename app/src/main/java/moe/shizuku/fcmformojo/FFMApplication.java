package moe.shizuku.fcmformojo;

import android.app.Application;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.RemoteException;

import moe.shizuku.fcmformojo.notification.NotificationBuilder;
import moe.shizuku.fcmformojo.utils.UsageStatsUtils;
import moe.shizuku.privileged.api.PrivilegedServer;
import moe.shizuku.support.utils.Settings;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static moe.shizuku.fcmformojo.FFMSettings.GET_FOREGROUND;

/**
 * Created by Rikka on 2017/4/19.
 */

public class FFMApplication extends Application {

    private NotificationBuilder mNotificationBuilder;
    private Retrofit mRetrofit;

    private Handler mMainHandler;

    private boolean mIsSystem;

    public static FFMApplication get(Context context) {
        return (FFMApplication) context.getApplicationContext();
    }

    @Override
    public void onCreate() {
        super.onCreate();

        Settings.init(this);

        mMainHandler = new Handler(getMainLooper());

        mNotificationBuilder = new NotificationBuilder(this);

        mRetrofit = new Retrofit.Builder()
                .baseUrl(FFMSettings.getBaseUrl())
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        try {
            mIsSystem = (getPackageManager().getApplicationInfo(getPackageName(), 0).flags & ApplicationInfo.FLAG_SYSTEM) != 0;
        } catch (PackageManager.NameNotFoundException ignored) {
        }

        PrivilegedServer.bind(this);
    }

    public void runInMainThread(Runnable runnable) {
        mMainHandler.post(runnable);
    }

    public Retrofit getRetrofit() {
        return mRetrofit;
    }

    public void updateBaseUrl(String url) {
        mRetrofit = mRetrofit.newBuilder()
                .baseUrl(url)
                .build();
    }

    public NotificationBuilder getNotificationBuilder() {
        return mNotificationBuilder;
    }

    public String getForegroundPackage() {
        switch (Settings.getString(GET_FOREGROUND, "disable")) {
            case "usage_stats":
                return UsageStatsUtils.getForegroundPackage(this);
            case "privileged_server":
                PrivilegedServer.bind(this);
                if (PrivilegedServer.isServiceBind()) {
                    return PrivilegedServer.getForegroundPackageName();
                } else {
                    return null;
                }
            case "disable":
            default:
                return null;
        }
    }

    public boolean isSystem() {
        return mIsSystem;
    }
}
