package moe.shizuku.fcmformojo.notification;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.support.annotation.IntRange;
import android.support.annotation.Nullable;

import java.io.File;

import moe.shizuku.fcmformojo.R;
import moe.shizuku.fcmformojo.model.Chat;
import moe.shizuku.fcmformojo.model.Chat.ChatType;
import moe.shizuku.fcmformojo.utils.DrawableUtils;
import moe.shizuku.fcmformojo.utils.FileUtils;

/**
 * Created by rikka on 2017/7/29.
 */

public class ChatIcon {

    private static final int[] COLORS = {
            R.color.colorNotificationRed,
            R.color.colorNotificationOrange,
            R.color.colorNotificationYellow,
            R.color.colorNotificationGreen,
            R.color.colorNotificationIndigo,
            R.color.colorNotificationBlue,
            R.color.colorNotificationPurple,
    };

    private static final String PATH_FRIEND = "/head/friend/";
    private static final String PATH_GROUP = "/head/group/";

    /**
     * 根据聊天类型来生成头像 Bitmap。<p>
     *
     * 对于好友或群会先尝试从文件读取，若没有将会使用内置的资源生成。<br>
     * 对于讨论组会强制使用内置的资源生成。<br>
     * 对于系统消息会直接返回 null。
     *
     * @param context Context
     * @param uid QQ 号码或群号码
     * @param type 聊天类型
     * @return Bitmap
     */
    @Nullable
    public static Bitmap getIcon(Context context, long uid, @ChatType int type) {
        if (type == ChatType.SYSTEM) {
            return null;
        }

        Bitmap bitmap = loadIcon(context, uid, type);
        if (bitmap == null) {
            bitmap = getDefault(context, (int) uid % 7, type != ChatType.FRIEND);
        }
        return bitmap;
    }
    /**
     * 尝试从文件读取头像。
     *
     * @param context Context
     * @param uid QQ 号码或群号码
     * @param type 聊天类型
     * @return 头像 Bitmap，若不存在则返回 null
     */
    @Nullable
    public static Bitmap loadIcon(Context context, long uid, @ChatType int type) {
        if (type == ChatType.DISCUSS
                || type == ChatType.SYSTEM) {
            return null;
        }

        File file = FileUtils.getCacheFile(context,
                type == ChatType.GROUP ? PATH_GROUP : PATH_FRIEND + uid);
        if (file.exists()) {
            return BitmapFactory.decodeFile(file.getAbsolutePath());
        }
        return null;
    }

    /**
     * 返回生成的默认默认头像。
     *
     * @param context Context
     * @param color 预设颜色
     * @param isGroup 是否是群组
     * @return 头像 Bitmap
     */
    public static Bitmap getDefault(Context context, @IntRange(from = 0, to = 6) int color, boolean isGroup) {
        Drawable drawable = context.getDrawable(isGroup ? R.drawable.ic_noti_group_48dp : R.drawable.ic_noti_person_48dp);
        if (drawable != null) {
            drawable.setTint(context.getColor(COLORS[color]));

            return DrawableUtils.toBitmap(drawable);
        }
        return null;
    }
}
