package moe.shizuku.fcmformojo.preference;

import android.content.Context;
import android.util.AttributeSet;

import moe.shizuku.preference.SwitchPreference;

/**
 * 戳了不会变的 SwitchPreference
 */

public class StateInformationPreference extends SwitchPreference {

    public StateInformationPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public StateInformationPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public StateInformationPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public StateInformationPreference(Context context) {
        super(context);
    }

    @Override
    protected void onClick() {
    }
}
