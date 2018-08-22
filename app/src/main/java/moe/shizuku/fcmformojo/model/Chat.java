package moe.shizuku.fcmformojo.model;


import android.content.Context;
import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.IntDef;
import android.support.annotation.Keep;

import java.lang.annotation.Retention;
import java.lang.ref.WeakReference;
import java.util.Collections;

import moe.shizuku.fcmformojo.notification.UserIcon;
import moe.shizuku.fcmformojo.utils.ChatMessagesList;

import static java.lang.annotation.RetentionPolicy.SOURCE;

/**
 * Created by Rikka on 2016/9/20.
 */

@Keep
public class Chat implements Parcelable {

    /** 定义聊天类型 */
    @IntDef({
            ChatType.SYSTEM,
            ChatType.FRIEND,
            ChatType.GROUP,
            ChatType.DISCUSS,
    })
    @Retention(SOURCE)
    public @interface ChatType {
        /** 好友 */
        int FRIEND = 0;
        /** 群 */
        int GROUP = 1;
        /** 讨论组 */
        int DISCUSS = 2;
        /** 系统 */
        int SYSTEM = 3;
    }

    /** 类型 */
    private final @ChatType int type;

    /** 唯一 id */
    private final long id;

    /** 可见 id（如 QQ 号） */
    private final long uid;

    /** 名字 */
    private final String name;

    /** 消息们 */
    private ChatMessagesList messages = new ChatMessagesList();

    /** 图标 */
    private WeakReference<Bitmap> icon = new WeakReference<>(null);

    /**
     * 返回该聊天的聊天类型
     *
     * @return {@link ChatType} 中定义的聊天类型
     */
    public @ChatType int getType() {
        return type;
    }

    /**
     * 返回该聊天的唯一 id（该 id 可能因为重新登陆而变化）。
     *
     * @return 唯一 id
     */
    public long getId() {
        return id;
    }

    /**
     * 返回该聊天对应的用户可见 id，即 QQ 号码或群号码。
     *
     * @return 用户可见 id
     */
    public long getUid() {
        return uid;
    }

    /**
     * 范围一个唯一的 id，通常会使用 uid，uid 为 0 时使用 id。
     *
     * @return 唯一的 id
     */
    public long getUniqueId() {
        if (getUid() == 0) {
            return isGroup() ? -getId() : getId();
        } else {
            return isGroup() ? -getUid() : getUid();
        }
    }

    /**
     * 返回该聊天的名称（当前插件写法是优先使用备注名称）。
     *
     * @return 好友名称或群名称
     */
    public String getName() {
        return name;
    }

    /**
     * 返回该聊天的消息列表。
     *
     * @return 消息列表
     */
    public ChatMessagesList getMessages() {
        return messages;
    }

    public void setMessages(ChatMessagesList messages) {
        this.messages = messages;
    }

    /**
     * 返回该聊天的消息列表的最后一项。
     *
     * @return 消息列表的最后一项
     */
    public Message getLatestMessage() {
        return messages.get(messages.size() - 1);
    }

    /**
     * 返回是否是好友。
     *
     * @return 是否是好友
     */
    public boolean isFriend() {
        return type == ChatType.FRIEND;
    }

    /**
     * 返回是否是群或讨论组。
     *
     * @return 是否是群组
     */
    public boolean isGroup() {
        return type == ChatType.GROUP || type == ChatType.DISCUSS;
    }

    /**
     * 返回是否是系统消息（如登陆成功）。
     *
     * @return 是否是系统消息
     */
    public boolean isSystem() {
        return type == ChatType.SYSTEM;
    }

    public WeakReference<Bitmap> getIcon() {
        return icon;
    }

    public void setIcon(WeakReference<Bitmap> icon) {
        this.icon = icon;
    }

    /**
     * 返回该聊天的头像，若本地没有头像将使用生成的头像。
     *
     * @return 头像
     */
    public Bitmap loadIcon(Context context) {
        if (isSystem()) {
            return null;
        }

        if (icon == null || icon.get() == null) {
            icon = new WeakReference<>(UserIcon.getIcon(context, uid, type));
        }
        return icon.get();
    }

    @Override
    public String toString() {
        return "Chat{" +
                "type=" + type +
                ", id=" + id +
                ", name='" + name + '\'' +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.type);
        dest.writeLong(this.id);
        dest.writeLong(this.uid);
        dest.writeString(this.name);
        dest.writeTypedList(Collections.singletonList(getLatestMessage()));
    }

    protected Chat(Parcel in) {
        this.type = in.readInt();
        this.id = in.readLong();
        this.uid = in.readLong();
        this.name = in.readString();
        this.messages = new ChatMessagesList(in.createTypedArrayList(Message.CREATOR));
    }

    public static final Parcelable.Creator<Chat> CREATOR = new Parcelable.Creator<Chat>() {
        @Override
        public Chat createFromParcel(Parcel source) {
            return new Chat(source);
        }

        @Override
        public Chat[] newArray(int size) {
            return new Chat[size];
        }
    };
}
