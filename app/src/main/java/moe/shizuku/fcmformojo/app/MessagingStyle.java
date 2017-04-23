package moe.shizuku.fcmformojo.app;

import android.os.Bundle;
import android.support.v4.app.NotificationCompat;

/**
 * Created by Rikka on 2017/4/19.
 */

public class MessagingStyle extends NotificationCompat.MessagingStyle {

    private CharSequence mSummaryText;
    private String mSubName;

    /**
     * @param userDisplayName the name to be displayed for any replies sent by the user before the
     *                        posting app reposts the notification with those messages after they've been actually
     *                        sent and in previous messages sent by the user added in
     *                        {@link #addMessage(Message)}
     */
    public MessagingStyle(CharSequence userDisplayName) {
        super(userDisplayName);
    }

    public void setSummaryText(CharSequence summaryText) {
        mSummaryText = summaryText;
    }

    @Override
    public void addCompatExtras(Bundle extras) {
        super.addCompatExtras(extras);

        extras.putCharSequence(NotificationCompat.EXTRA_SUMMARY_TEXT, mSummaryText);
        //extras.putString("android.substName", "QQQQQQ");
    }

    @Override
    protected void restoreFromCompatExtras(Bundle extras) {
        super.restoreFromCompatExtras(extras);

        //mSubName = extras.getString("android.substName");
        mSummaryText = extras.getCharSequence(NotificationCompat.EXTRA_SUMMARY_TEXT);
    }
}
