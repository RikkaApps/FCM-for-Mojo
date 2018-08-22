package moe.shizuku.fcmformojo.app;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.Person;

/**
 * Created by Rikka on 2017/4/19.
 */

public class MessagingStyle extends NotificationCompat.MessagingStyle {

    private CharSequence mSummaryText;

    /**
     * @param userDisplayName the name to be displayed for any replies sent by the user before the
     *                        posting app reposts the notification with those messages after they've been actually
     *                        sent and in previous messages sent by the user added in
     *                        {@link #addMessage(Message)}
     */
    public MessagingStyle(CharSequence userDisplayName) {
        super(new Person.Builder().setName(userDisplayName).build());
    }

    public MessagingStyle(@NonNull Person user) {
        super(user);
    }

    public void setSummaryText(CharSequence summaryText) {
        mSummaryText = summaryText;
    }

    @Override
    public void addCompatExtras(Bundle extras) {
        super.addCompatExtras(extras);

        extras.putCharSequence(NotificationCompat.EXTRA_SUMMARY_TEXT, mSummaryText);
    }

    @SuppressLint("RestrictedApi")
    @Override
    protected void restoreFromCompatExtras(Bundle extras) {
        super.restoreFromCompatExtras(extras);

        mSummaryText = extras.getCharSequence(NotificationCompat.EXTRA_SUMMARY_TEXT);
    }
}
