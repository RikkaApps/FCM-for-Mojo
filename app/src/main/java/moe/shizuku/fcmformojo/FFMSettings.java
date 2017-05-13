package moe.shizuku.fcmformojo;

import android.content.Context;
import android.net.Uri;

import moe.shizuku.support.utils.Settings;

/**
 * Created by Rikka on 2017/4/22.
 */

public class FFMSettings {

    public static final String BASE_URL = "server_url";
    public static final String SERVER_HTTP_USERNAME = "server_http_username";
    public static final String SERVER_HTTP_PASSWORD = "server_http_password";
    public static final String QQ_PACKAGE = "qq_package";
    public static final String NOTIFICATION_NAME = "notification_app_name";
    public static final String GET_FOREGROUND = "get_foreground";

    public static String getBaseUrl() {
        return Settings.getString(BASE_URL, "http://0.0.0.0:5000");
    }

    public static void setBaseUrl(Context context, String url) {
        FFMApplication.get(context).updateBaseUrl(url);
        Settings.putString(BASE_URL, url);
    }

    public static String getQQPackageName() {
        return Settings.getString(QQ_PACKAGE, "com.tencent.mobileqq");
    }

    public static String getNotificationAppName() {
        return Settings.getString(NOTIFICATION_NAME, "QQ");
    }

    public static boolean getNotification(boolean group) {
        return Settings.getBoolean(group ? "notification_group" : "notification", !group);
    }

    public static int getPriority(boolean group) {
        String value = Settings.getString(group ? "priority_group" : "priority", group ? "0" : "1");
        return Integer.parseInt(value);
    }

    public static int getVibrate(boolean group) {
        String value = Settings.getString(group ? "vibrate_group" : "vibrate", "1");
        return Integer.parseInt(value);
    }

    public static boolean getLight(boolean group) {
        return Settings.getBoolean(group ? "light_group" : "light", true);
    }

    public static Uri getSound(boolean group) {
        return Uri.parse(Settings.getString(group ? "sound_group" : "sound", android.provider.Settings.System.DEFAULT_NOTIFICATION_URI.toString()));
    }
}
