package moe.shizuku.fcmformojo.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.RemoteInput;
import android.util.Log;
import android.widget.Toast;

import moe.shizuku.fcmformojo.BuildConfig;
import moe.shizuku.fcmformojo.FFMApplication;
import moe.shizuku.fcmformojo.FFMSettings;
import moe.shizuku.fcmformojo.R;
import moe.shizuku.fcmformojo.api.WebQQService;
import moe.shizuku.fcmformojo.model.Chat;
import moe.shizuku.fcmformojo.model.SendResult;
import moe.shizuku.fcmformojo.notification.NotificationBuilder;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

/**
 * Created by Rikka on 2017/4/19.
 */

public class NotificationReceiver extends BroadcastReceiver {

    private static final String ACTION_REPLY = BuildConfig.APPLICATION_ID + ".intent.action.REPLY";
    private static final String ACTION_CONTENT = BuildConfig.APPLICATION_ID + ".intent.action.CONTENT";
    private static final String ACTION_DELETE = BuildConfig.APPLICATION_ID + ".intent.action.DELETE";

    private static final String EXTRA_ID = BuildConfig.APPLICATION_ID + ".intent.extra.ID";
    private static final String EXTRA_TYPE = BuildConfig.APPLICATION_ID + ".intent.extra.TYPE";
    private static final String EXTRA_ALL = BuildConfig.APPLICATION_ID + ".intent.extra.ALL";

    public static Intent replyIntent(long senderId, int type) {
        return new Intent(ACTION_REPLY)
                .putExtra(EXTRA_ID, senderId)
                .putExtra(EXTRA_TYPE, type);
    }

    public static Intent contentIntent(long senderId, boolean all) {
        return new Intent(ACTION_CONTENT)
                .putExtra(EXTRA_ID, senderId)
                .putExtra(EXTRA_ALL, all);
    }

    public static Intent deleteIntent(long senderId, boolean all) {
        return new Intent(ACTION_DELETE)
                .putExtra(EXTRA_ID, senderId)
                .putExtra(EXTRA_ALL, all);
    }

    @Override
    public void onReceive(final Context context, Intent intent) {
        long id = intent.getLongExtra(EXTRA_ID, 0);
        boolean all = intent.getBooleanExtra(EXTRA_ALL, false);

        switch (intent.getAction()) {
            case ACTION_REPLY:
                handleReply(context, intent);
                break;
            case ACTION_CONTENT:
                handleContent(context, id, all);
                break;
            case ACTION_DELETE:
                if (!all) {
                    FFMApplication.get(context).getNotificationBuilder()
                            .clearMessages(id);
                } else {
                    FFMApplication.get(context).getNotificationBuilder()
                            .clearMessages();
                }
                break;
        }
    }

    private void handleReply(final Context context, Intent intent) {
        Bundle remoteInput = RemoteInput.getResultsFromIntent(intent);
        CharSequence reply = null;
        if (remoteInput != null) {
            reply = remoteInput.getCharSequence("reply");
        }

        final long id = intent.getLongExtra(EXTRA_ID, 0);
        int type = intent.getIntExtra(EXTRA_TYPE, 1);

        if (reply == null || id == 0) {
            return;
        }

        Log.d("Reply", "try reply to " + id + " " + reply.toString());

        Retrofit retrofit = FFMApplication.get(context).getRetrofit();

        WebQQService service = retrofit.create(WebQQService.class);
        Call<SendResult> call;
        switch (type) {
            case 1:
                call = service.sendFriendMessage(id, reply.toString());
                break;
            case 2:
                call = service.sendGroupMessage(id, reply.toString());
                break;
            case 3:
                call = service.sendDiscussMessage(id, reply.toString());
                break;
            default:
                return;
        }

        call.enqueue(new Callback<SendResult>() {
            @Override
            public void onResponse(Call<SendResult> call, final Response<SendResult> response) {
                Log.d("Reply", response.body().getStatus());

                if (response.body().getCode() != 0) {
                    FFMApplication.get(context).runInMainTheard(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(context, response.body().getStatus(), Toast.LENGTH_LONG).show();
                        }
                    });
                }

                NotificationBuilder nb = FFMApplication.get(context).getNotificationBuilder();
                nb.clearMessages(id);
            }

            @Override
            public void onFailure(Call<SendResult> call, final Throwable t) {
                Log.d("Reply", "failed " + t.toString());

                FFMApplication.get(context).runInMainTheard(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(context, t.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });

                FFMApplication.get(context).getNotificationBuilder()
                        .clearMessages(id);
            }
        });
    }

    private void handleContent(Context context, long id, boolean all) {
        if (id == -2) {
            Chat chat = FFMApplication.get(context).getNotificationBuilder().getChat(id);
            if (chat != null) {
                context.startActivity(Intent.createChooser(new Intent(Intent.ACTION_VIEW, Uri.parse(chat.getLastMessage().getContent())), context.getString(R.string.open)));
            }

            FFMApplication.get(context).getNotificationBuilder()
                    .clearMessages(id);
            return;
        }

        FFMApplication.get(context).getNotificationBuilder()
                .clearMessages();

        Intent intent = context.getPackageManager().getLaunchIntentForPackage(FFMSettings.getQQPackageName());
        if (intent == null) {
            return;
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        context.startActivity(intent);
    }
}
