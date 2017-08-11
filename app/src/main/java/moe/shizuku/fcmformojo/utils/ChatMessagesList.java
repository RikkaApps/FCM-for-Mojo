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

    private int size;

    public ChatMessagesList() {
        super();

        size = 0;
    }

    public ChatMessagesList(@NonNull Collection<? extends Message> c) {
        super(c);

        size = c.size();
    }

    @Override
    public boolean add(Message message) {
        if (size() >= NOTIFICATION_MAX_MESSAGES) {
            removeFirst();
        }
        size++;
        return super.add(message);
    }

    public int getSize() {
        return size;
    }
}
