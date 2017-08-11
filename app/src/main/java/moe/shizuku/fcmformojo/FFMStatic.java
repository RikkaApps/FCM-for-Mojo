package moe.shizuku.fcmformojo;

/**
 * Created by rikka on 2017/7/29.
 */

public class FFMStatic {

    public static final String ACTION_UPDATE_ICON = BuildConfig.APPLICATION_ID + ".action.UPDATE_ICON";
    public static final String ACTION_REPLY = BuildConfig.APPLICATION_ID + ".action.REPLY";
    public static final String ACTION_CONTENT = BuildConfig.APPLICATION_ID + ".action.CONTENT";
    public static final String ACTION_DELETE = BuildConfig.APPLICATION_ID + ".action.DELETE";

    public static final String EXTRA_CONTENT = BuildConfig.APPLICATION_ID + ".extra.CONTENT";
    public static final String EXTRA_CHAT = BuildConfig.APPLICATION_ID + ".extra.CHAT";

    public static final String NOTIFICATION_CHANNEL_FRIENDS = "friend_message_channel";
    public static final String NOTIFICATION_CHANNEL_GROUPS = "group_message_channel";
    public static final String NOTIFICATION_CHANNEL_PROGRESS = "progress_channel";

    public static final int NOTIFICATION_ID_GROUP_SUMMARY = -10000;
    public static final int NOTIFICATION_ID_SYSTEM = -10001;
    public static final int NOTIFICATION_ID_PROGRESS = -10002;

    public static final String NOTIFICATION_INPUT_KEY = "reply";

    public static final int NOTIFICATION_MAX_MESSAGES = 8;

}
