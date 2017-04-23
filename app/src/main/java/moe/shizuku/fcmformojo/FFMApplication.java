package moe.shizuku.fcmformojo;

import android.app.Application;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Handler;

import moe.shizuku.fcmformojo.notification.NotificationBuilder;
import moe.shizuku.fcmformojo.utils.UsageStatsUtils;
import moe.shizuku.support.utils.Settings;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by Rikka on 2017/4/19.
 */

public class FFMApplication extends Application {

    private NotificationBuilder mNotificationBuilder = new NotificationBuilder();
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

        mNotificationBuilder = new NotificationBuilder();

        mRetrofit = new Retrofit.Builder()
                .baseUrl(FFMSettings.getBaseUrl())
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        try {
            mIsSystem = (getPackageManager().getApplicationInfo(getPackageName(), 0).flags & ApplicationInfo.FLAG_SYSTEM) != 0;
        } catch (PackageManager.NameNotFoundException ignored) {
        }
    }

    public void runInMainTheard(Runnable runnable) {
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
        return UsageStatsUtils.getForegroundPackage(this);
    }

    public boolean isSystem() {
        return mIsSystem;
    }
}
