package moe.shizuku.fcmformojo.model;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.Keep;

import moe.shizuku.fcmformojo.R;


@Keep
public class Message implements Parcelable {

    private final String sender;
    private final String content;
    private final long timestamp;
    private final boolean isAt;

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
        return isAt;
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
        dest.writeString(this.sender);
        dest.writeString(this.content);
        dest.writeLong(this.timestamp);
        dest.writeByte(this.isAt ? (byte) 1 : (byte) 0);
    }

    protected Message(Parcel in) {
        this.sender = in.readString();
        this.content = in.readString();
        this.timestamp = in.readLong();
        this.isAt = in.readByte() != 0;
    }

    public static final Parcelable.Creator<Message> CREATOR = new Parcelable.Creator<Message>() {
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
