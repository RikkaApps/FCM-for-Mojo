package moe.shizuku.fcmformojo;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.IntDef;
import android.support.annotation.StringDef;
import android.support.v4.provider.DocumentFile;

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
    public static final String DOWNLOADS_URI = "downloads_uri";
    public static final String LOCAL_GROUP_WHITELIST = "group_whitelist";
    public static final String LOCAL_DISCUSS_WHITELIST = "discuss_whitelist";
    public static final String NEW_TOKEN = "new_token";
    public static final String CLEAR_NOTIFICATION_WHEN_TAP = "clear_notification_when_tap";

    public static void init(Context context) {
        Settings.init(context);
    }

    public static String getBaseUrl() {
        return Settings.getString(BASE_URL, "http://0.0.0.0:5005");
    }

    public static Profile getProfile() {
        return ProfileList.getProfile(Settings.getString(QQ_PACKAGE, ""));
    }

    public static boolean getNotificationEnabled(boolean group) {
        return Settings.getBoolean(group ? "notification_group" : "notification", !group);
    }

    public static int getNotificationPriority(boolean group) {
        String value = Settings.getString(group ? "priority_group" : "priority", "1");
        return Integer.parseInt(value);
    }

    @IntDef({
            Vibrate.DISABLED,
            Vibrate.DEFAULT,
            Vibrate.SHORT,
            Vibrate.LONG,
    })
    @Retention(SOURCE)
    public @interface Vibrate {
        int DISABLED = 0;
        int DEFAULT = 1;
        int SHORT = 2;
        int LONG = 3;
    }

    public static @Vibrate int getNotificationVibrate(boolean group) {
        String value = Settings.getString(group ? "vibrate_group" : "vibrate", "1");
        return Integer.parseInt(value);
    }

    public static boolean getNotificationLight(boolean group) {
        return Settings.getBoolean(group ? "light_group" : "light", true);
    }

    public static Uri getNotificationSound(boolean group) {
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

    public static void putForegroundImpl(@ForegroundImpl String impl) {
        Settings.putString(GET_FOREGROUND, ForegroundImpl.NONE);
    }

    public static void putDownloadUri(String uri) {
        Settings.putString(DOWNLOADS_URI, uri);
    }

    public static DocumentFile getDownloadDir(Context context) {
        String uri = Settings.getString(DOWNLOADS_URI, null);
        if (uri == null) {
            return null;
        }

        DocumentFile pickedDir = DocumentFile.fromTreeUri(context, Uri.parse(uri));
        DocumentFile dir = pickedDir.findFile("FFM");

        if (dir == null)
            pickedDir = pickedDir.createDirectory("FFM");
        else
            pickedDir = dir;

        return pickedDir;
    }

    public static void putLocalGroupWhitelistValue(int value) {
        Settings.putInt(LOCAL_GROUP_WHITELIST, value);
    }

    public static int getLocalGroupWhitelistValue() {
        return Settings.getInt(LOCAL_GROUP_WHITELIST, -1);
    }

    public static void putLocalDiscussWhitelistValue(int value) {
        Settings.putInt(LOCAL_DISCUSS_WHITELIST, value);
    }

    public static int getLocalDiscussWhitelistValue() {
        return Settings.getInt(LOCAL_DISCUSS_WHITELIST, -1);
    }

    public static void putNewToken(String token) {
        Settings.putString(NEW_TOKEN, token);
    }

    public static String getNewToken() {
        return Settings.getString(NEW_TOKEN, null);
    }

    public static boolean isClearNotificationWhenTap() {
        return Settings.getBoolean(CLEAR_NOTIFICATION_WHEN_TAP, true);
    }

    public static String getSelfUid() {
        return Settings.getString("qq_account", "0");
    }

}
