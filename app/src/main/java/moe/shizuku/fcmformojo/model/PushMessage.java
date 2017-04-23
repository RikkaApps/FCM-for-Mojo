package moe.shizuku.fcmformojo.model;

import android.support.annotation.Keep;

/**
 * Created by Rikka on 2017/4/19.
 */

@Keep
public class PushMessage {

    private String type;
    private long uid;
    private long senderId;
    private long msgId;
    private String title;
    private String sender;
    private String content;
    private int senderType;
    private boolean isAt;

    public String getType() {
        return type;
    }

    public String getTitle() {
        return title;
    }

    public String getSender() {
        return sender;
    }

    public long getUid() {
        return uid;
    }

    public long getSenderId() {
        return senderId;
    }

    public String getContent() {
        return content;
    }

    public long getMsgId() {
        return msgId;
    }

    public int getSenderType() {
        return senderType;
    }

    public boolean isAt() {
        return isAt;
    }

    @Override
    public String toString() {
        return "PushMessage{" +
                "type='" + type + '\'' +
                ", senderId=" + senderId +
                ", msgId=" + msgId +
                ", title='" + title + '\'' +
                ", sender='" + sender + '\'' +
                ", content='" + content + '\'' +
                ", senderType=" + senderType +
                ", isAt=" + isAt +
                '}';
    }
}
