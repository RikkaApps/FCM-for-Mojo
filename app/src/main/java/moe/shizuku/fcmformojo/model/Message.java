package moe.shizuku.fcmformojo.model;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.Keep;

import moe.shizuku.fcmformojo.R;
import moe.shizuku.fcmformojo.model.openqq.User;


@Keep
public class Message implements Parcelable {

    private final User sender_user;
    private final String sender;
    private final String content;
    private final long timestamp;
    private final int isAt;

    public User getSenderUser() {
        if (sender_user == null) {
            return new User(0, 0, sender);
        }
        return sender_user;
    }

    /**
     * 返回该条消息的发送者名称。
     *
     * @return 发送者名称
     */
    public String getSender() {
        return sender;
    }

    /**
     * 返回该条消息的内容。
     *
     * @return 消息内容
     */
    public String getContent() {
        return content;
    }

    /**
     * 返回该条消息的内容，若为空返回 [图片]。
     *
     * @param context Context
     * @return 消息内容
     */
    public String getContent(Context context) {
        return content != null ? content : context.getString(R.string.notification_message_image);
    }

    /**
     * 返回该条消息是否 @ 了用户自己。
     *
     * @return 是否 @ 了用户自己
     */
    public boolean isAt() {
        return isAt == 1;
    }

    /**
     * 返回接收到该条消息时的时间戳。
     *
     * @return 时间戳
     */
    public long getTimestamp() {
        return timestamp;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(this.sender_user, flags);
        dest.writeString(this.sender);
        dest.writeString(this.content);
        dest.writeLong(this.timestamp);
        dest.writeInt(this.isAt);
    }

    public static Message createSelfMessage(String content, long timestamp) {
        return new Message(User.getSelf(), null, content, timestamp, 0);
    }

    public Message(User sender_user, String sender, String content, long timestamp, int isAt) {
        this.sender_user = sender_user;
        this.sender = sender;
        this.content = content;
        this.timestamp = timestamp;
        this.isAt = isAt;
    }

    protected Message(Parcel in) {
        this.sender_user = in.readParcelable(User.class.getClassLoader());
        this.sender = in.readString();
        this.content = in.readString();
        this.timestamp = in.readLong();
        this.isAt = in.readInt();
    }

    public static final Creator<Message> CREATOR = new Creator<Message>() {
        @Override
        public Message createFromParcel(Parcel source) {
            return new Message(source);
        }

        @Override
        public Message[] newArray(int size) {
            return new Message[size];
        }
    };
}
