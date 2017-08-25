package moe.shizuku.fcmformojo;

import android.app.Application;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.util.Log;

import java.util.UUID;

import moe.shizuku.fcmformojo.FFMSettings.ForegroundImpl;
import moe.shizuku.fcmformojo.api.FFMService;
import moe.shizuku.fcmformojo.api.OpenQQService;
import moe.shizuku.fcmformojo.interceptor.HttpBasicAuthorizationInterceptor;
import moe.shizuku.fcmformojo.notification.NotificationBuilder;
import moe.shizuku.fcmformojo.utils.URLFormatUtils;
import moe.shizuku.fcmformojo.utils.UsageStatsUtils;
import moe.shizuku.privileged.api.PrivilegedAPIs;
import moe.shizuku.privileged.api.receiver.TokenUpdateReceiver;
import moe.shizuku.support.utils.Settings;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

import static moe.shizuku.fcmformojo.FFMSettings.GET_FOREGROUND;

/**
 * Created by Rikka on 2017/4/19.
 */

public class FFMApplication extends Application {

    private static NotificationBuilder sNotificationBuilder;
    private static Retrofit sRxRetrofit;

    public static OpenQQService OpenQQService;
    public static FFMService FFMService;

    public static PrivilegedAPIs sPrivilegedAPIs;

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

    private static void initShizuku(Context context) {
        if (sPrivilegedAPIs == null) {
            return;
        }

        PrivilegedAPIs.setPermitNetworkThreadPolicy();

        sPrivilegedAPIs = new PrivilegedAPIs(FFMSettings.getToken());
        if (!sPrivilegedAPIs.authorized()) {
            if (!PrivilegedAPIs.installed(context)) {
                return;
            }

            UUID token = sPrivilegedAPIs.requestToken(context);
            if (token != null) {
                FFMSettings.putToken(token);

                Log.i("FFM", "update shizuku service token: " + token);
            } else {
                Settings.putString(GET_FOREGROUND, "disable");
            }
        }
        sPrivilegedAPIs.registerTokenUpdateReceiver(context, new TokenUpdateReceiver() {
            @Override
            public void onTokenUpdate(Context context, UUID token) {
                FFMSettings.putToken(token);

                sPrivilegedAPIs.updateToken(token);

                Log.i("FFM", "update shizuku service token: " + token);
            }
        });
    }

    @Override
    public void onCreate() {
        super.onCreate();

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
                    return sPrivilegedAPIs.getForegroundPackageName();
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
