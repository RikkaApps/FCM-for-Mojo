package moe.shizuku.fcmformojo;

/**
 * Created by rikka on 2017/7/29.
 */

public class FFMStatic {

    public static final String ACTION_UPDATE_ICON = BuildConfig.APPLICATION_ID + ".action.UPDATE_ICON";
    public static final String ACTION_DOWNLOAD_QRCODE = BuildConfig.APPLICATION_ID + ".action.DOWNLOAD_QRCODE";
    public static final String ACTION_REPLY = BuildConfig.APPLICATION_ID + ".action.REPLY";
    public static final String ACTION_CONTENT = BuildConfig.APPLICATION_ID + ".action.CONTENT";
    public static final String ACTION_DELETE = BuildConfig.APPLICATION_ID + ".action.DELETE";
    public static final String ACTION_OPEN_SCAN = BuildConfig.APPLICATION_ID + ".action.OPEN_SCAN";
    public static final String ACTION_REFRESH_STATUS = BuildConfig.APPLICATION_ID + ".action.REFRESH_STATUS";
    public static final String ACTION_RESTART_WEBQQ = BuildConfig.APPLICATION_ID + ".action.RESTART_WEBQQ";
    public static final String ACTION_DISMISS_SYSTEM_NOTIFICATION = BuildConfig.APPLICATION_ID + ".action.DISMISS_SYSTEM_NOTIFICATION";
    public static final String ACTION_UPDATE_URL = BuildConfig.APPLICATION_ID + ".action.URL_UPDATED";
    public static final String ACTION_COPY_TO_CLIPBOARD = BuildConfig.APPLICATION_ID + ".action.COPY_TO_CLIPBOARD";

    public static final String EXTRA_CONTENT = BuildConfig.APPLICATION_ID + ".extra.CONTENT";
    public static final String EXTRA_CHAT = BuildConfig.APPLICATION_ID + ".extra.CHAT";
    public static final String EXTRA_URL = BuildConfig.APPLICATION_ID + ".extra.URL";
    public static final String EXTRA_ALL = BuildConfig.APPLICATION_ID + ".extra.ALL";

    public static final String NOTIFICATION_CHANNEL_FRIENDS = "friend_message_channel";
    public static final String NOTIFICATION_CHANNEL_GROUPS = "group_message_channel";
    public static final String NOTIFICATION_CHANNEL_PROGRESS = "progress_channel";
    public static final String NOTIFICATION_CHANNEL_SERVER = "server_channel";


    public static final int NOTIFICATION_ID_GROUP_SUMMARY = -10000;
    public static final int NOTIFICATION_ID_SYSTEM = -10001;
    public static final int NOTIFICATION_ID_PROGRESS = -10002;

    public static final int REQUEST_CODE_OPEN_URI = 10000;
    public static final int REQUEST_CODE_OPEN_SCAN = 10001;
    public static final int REQUEST_CODE_DISMISS_SYSTEM_NOTIFICATION = 10002;
    public static final int REQUEST_CODE_RESTART_WEBQQ = 10003;
    public static final int REQUEST_CODE_COPY = 10004;
    public static final int REQUEST_CODE_SEND = 10005;

    public static final String NOTIFICATION_INPUT_KEY = "reply";

    public static final int NOTIFICATION_MAX_MESSAGES = 8;

    public static final String FILE_PROVIDER_AUTHORITY = BuildConfig.APPLICATION_ID + ".fileprovider";

}
