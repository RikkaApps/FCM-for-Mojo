package moe.shizuku.fcmformojo.widget;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.widget.FrameLayout;

import moe.shizuku.fcmformojo.R;

/**
 * Created by rikka on 2017/8/20.
 */

public class AnimationView extends FrameLayout {

    private float mPosition = 0f;

    private int mSpan;

    public AnimationView(@NonNull Context context) {
        super(context);
    }

    public AnimationView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public AnimationView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public AnimationView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mSpan = h;
        setPosition(mPosition);
    }

    public void setPosition(float position) {
        mPosition = position;
        setY((mSpan > 0) ? (mPosition * mSpan) : 0);

        setTranslationZ(mPosition * getResources().getDimensionPixelSize(R.dimen.dir_elevation));
    }
}
