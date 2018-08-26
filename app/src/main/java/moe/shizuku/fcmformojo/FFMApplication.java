package moe.shizuku.fcmformojo;

import android.app.Application;
import android.app.TaskStackListener;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.os.Process;
import android.os.RemoteException;
import android.util.Log;

import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.core.CrashlyticsCore;

import io.fabric.sdk.android.Fabric;
import moe.shizuku.api.ShizukuActivityManagerV26;
import moe.shizuku.api.ShizukuClient;
import moe.shizuku.fcmformojo.FFMSettings.ForegroundImpl;
import moe.shizuku.fcmformojo.api.FFMService;
import moe.shizuku.fcmformojo.api.OpenQQService;
import moe.shizuku.fcmformojo.compat.ShizukuCompat;
import moe.shizuku.fcmformojo.interceptor.HttpBasicAuthorizationInterceptor;
import moe.shizuku.fcmformojo.model.openqq.User;
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
    private static OkHttpClient sOkHttpClient;

    public static OpenQQService OpenQQService;
    public static FFMService FFMService;

    private Handler mMainHandler;

    private boolean mIsSystem;
    private TaskStackListener mTaskStackListener;

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

    private static void createOkHttpClient() {
        sOkHttpClient = new OkHttpClient.Builder()
                .addInterceptor(new HttpBasicAuthorizationInterceptor())
                .build();
    }

    public static OkHttpClient getOkHttpClient() {
        if (sOkHttpClient == null) {
            createOkHttpClient();
        }
        return sOkHttpClient;
    }

    private static void createRetrofit(String baseUrl) {
        if (!URLFormatUtils.isValidURL(baseUrl)) {
            baseUrl = "http://0.0.0.0/";
        }

        baseUrl = URLFormatUtils.addEndSlash(baseUrl);

        OkHttpClient okHttpClient = getOkHttpClient();

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
        if (FFMSettings.getForegroundImpl().equals(ForegroundImpl.SHIZUKU)
                && (ShizukuClient.getManagerVersion(context) < 106 || !ShizukuClient.checkSelfPermission(context))) {
            FFMSettings.putForegroundImpl(ForegroundImpl.NONE);
            return;
        }

        ShizukuClient.initialize(context);

        if (FFMSettings.getForegroundImpl().equals(ForegroundImpl.SHIZUKU)) {
            AsyncTask.execute(() -> FFMApplication.get(context).registerTaskStackListener());
        }
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
        User.setSelf(new User(0, FFMSettings.getSelfUid(), null));

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

    public boolean isTop() {
        try {
            switch (FFMSettings.getForegroundImpl()) {
                case ForegroundImpl.USAGE_STATS:
                    return FFMSettings.getProfile().getPackageName().equals(UsageStatsUtils.getForegroundPackage(this));
                case ForegroundImpl.SHIZUKU: {
                    return ShizukuCompat.isPackageTop(FFMSettings.getProfile().getPackageName(), Process.myUserHandle().hashCode());
                }
                case ForegroundImpl.NONE:
                default:
                    return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean isSystem() {
        return mIsSystem;
    }

    public void registerTaskStackListener() {
        if (mTaskStackListener != null) {
            return;
        }

        ShizukuClient.setContext(this);
        if (ShizukuClient.getState().isAuthorized()) {
            final Context context = this;

            if (Build.VERSION.SDK_INT >= 26 && Build.VERSION.SDK_INT < 28) {
                mTaskStackListener = new TaskStackListener() {

                    @Override
                    public void onTaskStackChanged() throws RemoteException {
                        final String pkg = ShizukuActivityManagerV26.getTasks(1, 0).get(0).topActivity.getPackageName();

                        //Log.d("FFM", "Foreground: " + pkg);
                        if (FFMSettings.getProfile().getPackageName().equals(pkg)) {
                            FFMSettings.getProfile().getPackageName();
                            FFMApplication.get(context).getNotificationBuilder()
                                    .clearMessages();

                            Log.i("FFM", "Foreground is selected QQ, clear notification.");
                        }
                    }
                };

                ShizukuActivityManagerV26.registerTaskStackListener(mTaskStackListener);
            }
        }
    }

    public void unregisterTaskStackListener() {
        if (mTaskStackListener == null) {
            return;
        }

        if (ShizukuClient.getState().isAuthorized()) {
            ShizukuActivityManagerV26.unregisterTaskStackListener(mTaskStackListener);

            mTaskStackListener = null;
        }
    }
}
