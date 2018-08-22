package moe.shizuku.fcmformojo.notification;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.support.annotation.IntRange;
import android.support.annotation.Nullable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;

import java.io.File;

import moe.shizuku.fcmformojo.R;
import moe.shizuku.fcmformojo.model.Chat.ChatType;
import moe.shizuku.fcmformojo.utils.DrawableUtils;
import moe.shizuku.fcmformojo.utils.FileUtils;


public class UserIcon {

    private static final int[] COLORS = {
            R.color.colorNotificationRed,
            R.color.colorNotificationOrange,
            R.color.colorNotificationGreen,
            R.color.colorNotificationIndigo,
            R.color.colorNotificationBlue,
            R.color.colorNotificationPurple,
    };

    private static final String PATH_FRIEND = "/head/friend/%s";
    private static final String PATH_GROUP = "/head/group/%s";

    /**
     * 返回头像文件。
     *
     * @param context Context
     * @param uid QQ 号码或群号码
     * @param type 聊天类型
     * @return File
     */
    public static File getIconFile(Context context, long uid, @ChatType int type) {
        return FileUtils.getCacheFile(context,
                String.format(type == ChatType.GROUP ? PATH_GROUP : PATH_FRIEND, Long.toString(uid)));
    }

    public static Bitmap requestIcon(Context context, long uid, @ChatType int type) {
        return getIcon(context, uid, type);
    }

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
            bitmap = getDefault(context, (int) uid, type != ChatType.FRIEND);
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

        File file = getIconFile(context, uid, type);
        if (file.exists()) {
            return BitmapFactory.decodeFile(file.getAbsolutePath());
        }
        return null;
    }

    /**
     * 返回生成的默认默认头像。
     *
     * @param context Context
     * @param random 预设颜色
     * @param isGroup 是否是群组
     * @return 头像 Bitmap
     */
    public static Bitmap getDefault(Context context, int random, boolean isGroup) {
        Drawable drawable = context.getDrawable(isGroup ? R.drawable.ic_noti_group_48dp : R.drawable.ic_noti_person_48dp);
        if (drawable != null) {
            drawable.setTint(context.getColor(COLORS[Math.abs(random) % 6]));

            return DrawableUtils.toBitmap(drawable);
        }
        return null;
    }

    /**
     * 把 Bitmap 变圆。
     *
     * @param context Context
     * @param bitmap 要处理的 Bitmap
     * @return 圆形的 Bitmap
     */
    public static Bitmap clipToRound(Context context, Bitmap bitmap) {
        final RoundedBitmapDrawable drawable = RoundedBitmapDrawableFactory.create(context.getResources(), bitmap);
        drawable.setAntiAlias(true);
        drawable.setCircular(true);
        drawable.setBounds(0, 0, bitmap.getWidth(), bitmap.getHeight());

        bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.draw(canvas);

        return bitmap;
    }
}
