package moe.shizuku.fcmformojo.viewholder;

import android.graphics.Bitmap;
import android.graphics.ColorFilter;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.crashlytics.android.Crashlytics;

import java.util.List;
import java.util.Locale;

import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import moe.shizuku.fcmformojo.R;
import moe.shizuku.fcmformojo.adapter.BlacklistAdapter;
import moe.shizuku.fcmformojo.model.Group;
import moe.shizuku.utils.recyclerview.BaseViewHolder;

/**
 * Created by rikka on 2017/8/28.
 */

public class BlacklistItemViewHolder extends BaseViewHolder<Pair<Group, Boolean>> implements View.OnClickListener {

    public static final Creator CREATOR = new Creator<Pair<Group, Boolean>>() {

        @Override
        public BaseViewHolder<Pair<Group, Boolean>> createViewHolder(LayoutInflater inflater, ViewGroup parent) {
            return new BlacklistItemViewHolder(inflater.inflate(R.layout.item_blacklist_item, parent ,false));
        }
    };

    private TextView title;
    private TextView summary;
    private ImageView icon;
    private CompoundButton toggle;

    private Disposable mDisposable;

    public BlacklistItemViewHolder(View itemView) {
        super(itemView);

        title = itemView.findViewById(android.R.id.title);
        summary = itemView.findViewById(android.R.id.summary);
        icon = itemView.findViewById(android.R.id.icon);
        toggle = itemView.findViewById(android.R.id.switch_widget);

        itemView.setOnClickListener(this);
    }

    @Override
    public BlacklistAdapter getAdapter() {
        return (BlacklistAdapter) super.getAdapter();
    }

    @Override
    public void onBind() {
        title.setText(getData().first.getName());
        summary.setText(String.format(Locale.ENGLISH, "%d", getData().first.getUid()));
        toggle.setChecked(getData().second);

        mDisposable = Single.just(getData().first.loadIcon(itemView.getContext()))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<Drawable>() {
                    @Override
                    public void accept(Drawable drawable) throws Exception {
                        icon.setImageDrawable(drawable);
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        throwable.printStackTrace();

                        Crashlytics.log("load icon");
                        Crashlytics.logException(throwable);
                    }
                });

        onBind(getAdapter().isEnabled());
    }

    @Override
    public void onBind(@NonNull List<Object> payloads) {
        for (Object payload : payloads) {
            if (payload instanceof Boolean) {
                onBind((Boolean) payload);
            }
        }
    }

    private static final ColorFilter sColorFilter;

    static {
        ColorMatrix cm = new ColorMatrix();
        cm.setSaturation(0);
        sColorFilter = new ColorMatrixColorFilter(cm);
    }

    public void onBind(boolean enabled) {
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
        setData(Pair.create(getData().first, !getData().second));
        getAdapter().getItems().set(getAdapterPosition(), getData());
        toggle.setChecked(getData().second);
    }

    @Override
    public void onRecycle() {
        mDisposable.dispose();
    }
}
