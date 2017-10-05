package moe.shizuku.fcmformojo.viewholder;

import android.graphics.drawable.Drawable;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.crashlytics.android.Crashlytics;

import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import moe.shizuku.fcmformojo.R;
import moe.shizuku.fcmformojo.model.Discuss;
import moe.shizuku.utils.recyclerview.BaseViewHolder;

/**
 * Created by rikka on 2017/9/2.
 */

public class DiscussWhitelistViewHolder extends WhitelistItemViewHolder<Discuss> {

    public static final Creator CREATOR = new Creator<Pair<Discuss, Boolean>>() {

        @Override
        public BaseViewHolder<Pair<Discuss, Boolean>> createViewHolder(LayoutInflater inflater, ViewGroup parent) {
            return new DiscussWhitelistViewHolder(inflater.inflate(R.layout.item_blacklist_item, parent ,false));
        }
    };

    public DiscussWhitelistViewHolder(View itemView) {
        super(itemView);

        summary.setVisibility(View.GONE);
    }

    @Override
    public void onBind() {
        title.setText(getData().first.getName());
        /*summary.setText(getData().first.getId() == 0 ? itemView.getContext().getString(R.string.whitelist_group_no_uid) :
                String.format(Locale.ENGLISH, "%d", getData().first.getId()));*/
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

        super.onBind();
    }

    @Override
    public void setEnabled(boolean enabled) {
        if (getData().first.getId() == 0) {
            enabled = false;
        }

        super.setEnabled(enabled);
    }
}
