package moe.shizuku.fcmformojo.model;

/**
 * TODO 这里的东西不是很合理
 */

public class Message {

    private long msgId;
    private long senderUid;
    private String sender;
    private String content;
    private long timestamp;
    private boolean isAt;

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
}
