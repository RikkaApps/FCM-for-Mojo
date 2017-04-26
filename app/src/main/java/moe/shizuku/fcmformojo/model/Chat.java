package moe.shizuku.fcmformojo.model;


import android.support.annotation.Keep;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by Rikka on 2016/9/20.
 */

@Keep
public class Chat {

    public static class Message {
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

        public long getMsgId() {
            return msgId;
        }

        public String getSender() {
            return sender;
        }

        public long getSenderUid() {
            return senderUid;
        }

        public String getContent() {
            return content;
        }

        public boolean isAt() {
            return isAt;
        }

        public long getTimestamp() {
            return timestamp;
        }
    }

    private int type;
    private long id;
    private long uid;
    private String name;
    private List<Message> messages;

    public Chat(PushMessage message) {
        this.type = message.getSenderType();
        this.id = message.getSenderId();
        this.uid = message.getUid();
        this.name = message.getTitle();
        this.messages = new LinkedList<>();
    }

    public int getType() {
        return type;
    }

    public long getId() {
        return id;
    }

    public long getUid() {
        return uid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Message> getMessages() {
        return messages;
    }

    public Message getLastMessage() {
        return messages.get(messages.size() - 1);
    }

    public boolean isSystemMessage() {
        return type == 0;
    }

    @Override
    public String toString() {
        return "Chat{" +
                "type=" + type +
                ", id=" + id +
                ", name='" + name + '\'' +
                '}';
    }
}
