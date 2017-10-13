package moe.shizuku.fcmformojo.viewholder;

import android.graphics.ColorFilter;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.support.annotation.NonNull;
import android.util.Pair;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import io.reactivex.disposables.Disposable;
import moe.shizuku.fcmformojo.adapter.WhitelistAdapter;
import moe.shizuku.support.recyclerview.BaseViewHolder;

/**
 * Created by rikka on 2017/9/2.
 */

public abstract class WhitelistItemViewHolder<W> extends BaseViewHolder<Pair<W, Boolean>> implements View.OnClickListener {

    protected TextView title;
    protected TextView summary;
    protected ImageView icon;
    protected CompoundButton toggle;

    protected Disposable mDisposable;

    public WhitelistItemViewHolder(View itemView) {
        super(itemView);

        title = itemView.findViewById(android.R.id.title);
        summary = itemView.findViewById(android.R.id.summary);
        icon = itemView.findViewById(android.R.id.icon);
        toggle = itemView.findViewById(android.R.id.switch_widget);

        itemView.setOnClickListener(this);
    }

    @Override
    public WhitelistAdapter getAdapter() {
        return (WhitelistAdapter) super.getAdapter();
    }

    @Override
    public void onBind() {
        setEnabled(getAdapter().isEnabled());
    }

    @Override
    public void onBind(@NonNull List<Object> payloads) {
        for (Object payload : payloads) {
            if (payload instanceof Boolean) {
                setEnabled((Boolean) payload);
            } else {
                toggle.setChecked(getData().second);
            }
        }
    }

    private static final ColorFilter sColorFilter;

    static {
        ColorMatrix cm = new ColorMatrix();
        cm.setSaturation(0);
        sColorFilter = new ColorMatrixColorFilter(cm);
    }

    public void setEnabled(boolean enabled) {
        itemView.setEnabled(enabled);
        toggle.setEnabled(enabled);
        title.setEnabled(enabled);
        summary.setEnabled(enabled);

        if (enabled) {
            icon.setAlpha(1f);
            icon.setColorFilter(null);
        } else {
            icon.setAlpha(0.5f);
            icon.setColorFilter(sColorFilter);
        }
    }

    @Override
    public void onClick(View view) {
        setData(Pair.create(getData().first, !getData().second), new Object());
    }

    @Override
    public void onRecycle() {
        mDisposable.dispose();
    }
}
