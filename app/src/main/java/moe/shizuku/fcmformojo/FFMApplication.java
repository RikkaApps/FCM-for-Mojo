package moe.shizuku.fcmformojo;

import android.app.Application;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Handler;

import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.core.CrashlyticsCore;

import io.fabric.sdk.android.Fabric;
import moe.shizuku.api.ShizukuClient;
import moe.shizuku.fcmformojo.FFMSettings.ForegroundImpl;
import moe.shizuku.fcmformojo.api.FFMService;
import moe.shizuku.fcmformojo.api.OpenQQService;
import moe.shizuku.fcmformojo.compat.ShizukuCompat;
import moe.shizuku.fcmformojo.interceptor.HttpBasicAuthorizationInterceptor;
import moe.shizuku.fcmformojo.notification.NotificationBuilder;
import moe.shizuku.fcmformojo.utils.URLFormatUtils;
import moe.shizuku.fcmformojo.utils.UsageStatsUtils;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by Rikka on 2017/4/19.
 */

public class FFMApplication extends Application {

    private static NotificationBuilder sNotificationBuilder;
    private static Retrofit sRxRetrofit;

    public static OpenQQService OpenQQService;
    public static FFMService FFMService;

    private Handler mMainHandler;

    private boolean mIsSystem;

    public static FFMApplication get(Context context) {
        return (FFMApplication) context.getApplicationContext();
    }

    public static void updateBaseUrl(String url) {
        url = URLFormatUtils.addEndSlash(url);

        sRxRetrofit = sRxRetrofit
                .newBuilder()
                .baseUrl(url)
                .build();

        createServices(sRxRetrofit);
    }

    private static void createRetrofit(String baseUrl) {
        if (!URLFormatUtils.isValidURL(baseUrl)) {
            baseUrl = "http://0.0.0.0/";
        }

        baseUrl = URLFormatUtils.addEndSlash(baseUrl);

        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .addInterceptor(new HttpBasicAuthorizationInterceptor())
                .build();

        sRxRetrofit = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .client(okHttpClient)
                .build();
    }

    private static void createServices() {
        createServices(sRxRetrofit);
    }

    private static void createServices(Retrofit retrofit) {
        if (retrofit == null) {
            createRetrofit(FFMSettings.getBaseUrl());
            retrofit = sRxRetrofit;
        }

        OpenQQService = retrofit.create(OpenQQService.class);
        FFMService = retrofit.create(FFMService.class);
    }

    private static void initShizuku(final Context context) {
        // not installed, version too low, no permission
        if (ShizukuClient.getManagerVersion(context) < 106
                || !ShizukuClient.checkSelfPermission(context)) {
            FFMSettings.putForegroundImpl(ForegroundImpl.NONE);
            return;
        }

        ShizukuClient.initialize(context);
    }

    private static void initCrashReport(Context context) {
        Crashlytics crashlyticsKit = new Crashlytics.Builder()
                .core(new CrashlyticsCore.Builder().disabled(BuildConfig.DEBUG).build())
                .build();

        Fabric.with(context, crashlyticsKit);
    }

    @Override
    public void onCreate() {
        super.onCreate();

        initCrashReport(this);

        mMainHandler = new Handler(getMainLooper());

        sNotificationBuilder = new NotificationBuilder(this);

        try {
            mIsSystem = (getPackageManager().getApplicationInfo(getPackageName(), 0).flags & ApplicationInfo.FLAG_SYSTEM) != 0;
        } catch (PackageManager.NameNotFoundException ignored) {
        }

        FFMSettings.init(this);

        createRetrofit(FFMSettings.getBaseUrl());
        createServices();

        initShizuku(this);
    }

    public void runInMainThread(Runnable runnable) {
        mMainHandler.post(runnable);
    }

    public NotificationBuilder getNotificationBuilder() {
        return sNotificationBuilder;
    }

    public String getForegroundPackage() {
        try {
            switch (FFMSettings.getForegroundImpl()) {
                case ForegroundImpl.USAGE_STATS:
                    return UsageStatsUtils.getForegroundPackage(this);
                case ForegroundImpl.SHIZUKU:
                    return ShizukuCompat.getForegroundPackage();
                case ForegroundImpl.NONE:
                default:
                    return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public boolean isSystem() {
        return mIsSystem;
    }
}
