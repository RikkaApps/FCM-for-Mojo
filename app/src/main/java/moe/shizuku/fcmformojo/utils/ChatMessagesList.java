package moe.shizuku.fcmformojo.utils;

import android.support.annotation.NonNull;

import java.util.Collection;
import java.util.LinkedList;

import moe.shizuku.fcmformojo.model.Chat;
import moe.shizuku.fcmformojo.model.Message;

import static moe.shizuku.fcmformojo.FFMStatic.NOTIFICATION_MAX_MESSAGES;

/**
 * 专为 {@link Chat#getMessages()} 更改的 List。
 */

public class ChatMessagesList extends LinkedList<Message> {

    public ChatMessagesList() {
        super();
    }

    public ChatMessagesList(@NonNull Collection<? extends Message> c) {
        super(c);
    }

    @Override
    public boolean add(Message message) {
        if (size() >= NOTIFICATION_MAX_MESSAGES) {
            removeFirst();
        }
        return super.add(message);
    }
}
