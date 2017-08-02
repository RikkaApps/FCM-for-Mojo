package moe.shizuku.fcmformojo.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.Keep;

/**
 * TODO 这里的东西不是很合理
 */

@Keep
public class Message implements Parcelable {

    private final long msgId;
    private final long senderUid;
    private final String sender;
    private final String content;
    private final long timestamp;
    private final boolean isAt;

    public Message(PushMessage message) {
        this.msgId = message.getMsgId();
        this.senderUid = message.getUid();
        this.sender = message.getSender();
        this.content = message.getContent();
        this.timestamp = System.currentTimeMillis();
        this.isAt = message.isAt();
    }

    /**
     * 返回该条消息的 id。
     *
     * @return 消息 id
     */
    public long getMsgId() {
        return msgId;
    }

    /**
     * 返回该条消息的发送者名称，若是群组消息将返回发送者的名称而不是群组名称。
     *
     * @return 发送者名称
     */
    public String getSender() {
        return sender;
    }

    /**
     * 返回发送者用户可见 id，即 QQ 号码或群号码。
     *
     * @return 用户可见 id
     */
    public long getSenderUid() {
        return senderUid;
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
        dest.writeLong(this.msgId);
        dest.writeLong(this.senderUid);
        dest.writeString(this.sender);
        dest.writeString(this.content);
        dest.writeLong(this.timestamp);
        dest.writeByte(this.isAt ? (byte) 1 : (byte) 0);
    }

    protected Message(Parcel in) {
        this.msgId = in.readLong();
        this.senderUid = in.readLong();
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
