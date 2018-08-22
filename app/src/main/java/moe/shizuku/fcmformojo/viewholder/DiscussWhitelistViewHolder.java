package moe.shizuku.fcmformojo.viewholder;

import android.util.Pair;
import android.view.View;

import com.crashlytics.android.Crashlytics;

import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import moe.shizuku.fcmformojo.R;
import moe.shizuku.fcmformojo.model.openqq.Discuss;

/**
 * Created by rikka on 2017/9/2.
 */

public class DiscussWhitelistViewHolder extends WhitelistItemViewHolder<Discuss> {

    public static final Creator CREATOR = (Creator<Pair<Discuss, Boolean>>) (inflater, parent) -> new DiscussWhitelistViewHolder(inflater.inflate(R.layout.item_blacklist_item, parent ,false));

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
                .subscribe(drawable -> icon.setImageDrawable(drawable), throwable -> {
                    throwable.printStackTrace();

                    Crashlytics.log("load icon");
                    Crashlytics.logException(throwable);
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
