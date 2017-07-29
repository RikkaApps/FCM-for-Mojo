package moe.shizuku.fcmformojo;

import android.net.Uri;
import android.support.annotation.StringDef;

import java.lang.annotation.Retention;
import java.util.UUID;

import moe.shizuku.fcmformojo.profile.Profile;
import moe.shizuku.fcmformojo.profile.ProfileList;
import moe.shizuku.support.utils.Settings;

import static java.lang.annotation.RetentionPolicy.SOURCE;

/**
 * Created by Rikka on 2017/4/22.
 */

public class FFMSettings {

    public static final String BASE_URL = "server_url";
    public static final String SERVER_HTTP_USERNAME = "server_http_username";
    public static final String SERVER_HTTP_PASSWORD = "server_http_password";
    public static final String QQ_PACKAGE = "qq_package";
    public static final String GET_FOREGROUND = "get_foreground";

    public static String getBaseUrl() {
        return Settings.getString(BASE_URL, "http://0.0.0.0:5000");
    }

    public static Profile getProfile() {
        return ProfileList.getProfile(Settings.getString(QQ_PACKAGE, ""));
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

    /**
     * 从设置读取 Shizuku Server 的 token。
     *
     * @return token
     */
    public static UUID getToken() {
        long mostSig = Settings.getLong("token_most", 0);
        long leastSig = Settings.getLong("token_least", 0);
        return new UUID(mostSig, leastSig);
    }

    /**
     * 将 Shizuku Server 的 token 保存到设置。
     *
     * @param token token
     */
    public static void putToken(UUID token) {
        if (token != null) {
            Settings.putLong("token_most", token.getMostSignificantBits());
            Settings.putLong("token_least", token.getLeastSignificantBits());
        } else {
            Settings.putLong("token_most", 0);
            Settings.putLong("token_least", 0);
        }
    }

    /** 定义获取前台应用的方法 */
    @StringDef({
            ForegroundImpl.NONE,
            ForegroundImpl.SHIZUKU,
            ForegroundImpl.USAGE_STATS,
    })
    @Retention(SOURCE)
    public @interface ForegroundImpl {
        /** 不获取 */
        String NONE = "disable";
        /** 使用 Shizuku Server */
        String SHIZUKU = "privileged_server";
        /** 使用“使用情况访问” */
        String USAGE_STATS = "usage_stats";
    }

    /**
     * 返回获得前台应用的实现方法。
     *
     * @return {@link ForegroundImpl} 中定义类型
     */
    public static @ForegroundImpl String getForegroundImpl() {
        return Settings.getString(GET_FOREGROUND, ForegroundImpl.NONE);
    }
}
