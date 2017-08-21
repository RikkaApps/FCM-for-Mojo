package moe.shizuku.fcmformojo.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.RemoteInput;

import moe.shizuku.fcmformojo.BuildConfig;
import moe.shizuku.fcmformojo.FFMApplication;
import moe.shizuku.fcmformojo.FFMSettings;
import moe.shizuku.fcmformojo.model.Chat;
import moe.shizuku.fcmformojo.service.FFMIntentService;

import static moe.shizuku.fcmformojo.FFMStatic.ACTION_CONTENT;
import static moe.shizuku.fcmformojo.FFMStatic.ACTION_DELETE;
import static moe.shizuku.fcmformojo.FFMStatic.ACTION_OPEN_SCAN;
import static moe.shizuku.fcmformojo.FFMStatic.ACTION_REPLY;
import static moe.shizuku.fcmformojo.FFMStatic.EXTRA_CHAT;
import static moe.shizuku.fcmformojo.FFMStatic.NOTIFICATION_INPUT_KEY;

/**
 * Created by Rikka on 2017/4/19.
 */

public class NotificationReceiver extends BroadcastReceiver {

    public static Intent replyIntent(Chat chat) {
        return new Intent(ACTION_REPLY)
                .putExtra(EXTRA_CHAT, chat)
                .setPackage(BuildConfig.APPLICATION_ID);
    }

    public static Intent contentIntent(Chat chat) {
        return new Intent(ACTION_CONTENT)
                .putExtra(EXTRA_CHAT, chat)
                .setPackage(BuildConfig.APPLICATION_ID);
    }

    public static Intent deleteIntent(Chat chat) {
        return new Intent(ACTION_DELETE)
                .putExtra(EXTRA_CHAT, chat)
                .setPackage(BuildConfig.APPLICATION_ID);
    }

    public static Intent openScanIntent() {
        return new Intent(ACTION_OPEN_SCAN)
                .setPackage(BuildConfig.APPLICATION_ID);
    }

    @Override
    public void onReceive(final Context context, Intent intent) {
        if (intent == null) {
            return;
        }

        Chat chat = intent.getParcelableExtra(EXTRA_CHAT);
        switch (intent.getAction()) {
            case ACTION_REPLY:
                Bundle remoteInput = RemoteInput.getResultsFromIntent(intent);
                handleReply(context, remoteInput, chat);
                break;
            case ACTION_CONTENT:
                handleContent(context, chat);
                break;
            case ACTION_DELETE:
                handleDelete(context, chat);
                break;
            case ACTION_OPEN_SCAN:
                handleOpenScan(context);
                break;
        }
    }

    private void handleReply(final Context context, Bundle remoteInput, Chat chat) {
        CharSequence reply = null;
        if (remoteInput != null) {
            reply = remoteInput.getCharSequence(NOTIFICATION_INPUT_KEY);
        }

        FFMIntentService.startReply(context, reply, chat);
    }

    private void handleContent(Context context, @Nullable Chat chat) {
        if (chat == null) {
            FFMApplication.get(context).getNotificationBuilder()
                    .clearMessages();

            FFMSettings.getProfile().onStartChatActivity(context, null);
        } else {
            FFMApplication.get(context).getNotificationBuilder()
                        .clearMessages();

            FFMSettings.getProfile().onStartChatActivity(context, chat);
        }
    }

    private void handleDelete(Context context, @Nullable Chat chat) {
        if (chat != null) {
            FFMApplication.get(context).getNotificationBuilder()
                    .clearMessages(chat.getUniqueId());
        } else {
            FFMApplication.get(context).getNotificationBuilder()
                    .clearMessages();
        }
    }

    private void handleOpenScan(Context context) {
        FFMSettings.getProfile().onStartQrCodeScanActivity(context);
    }
}
