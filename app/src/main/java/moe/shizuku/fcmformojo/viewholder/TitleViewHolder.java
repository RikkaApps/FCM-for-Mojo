package moe.shizuku.fcmformojo.viewholder;

import android.view.View;
import android.widget.TextView;

import moe.shizuku.fcmformojo.R;
import moe.shizuku.support.recyclerview.BaseViewHolder;

/**
 * Created by rikka on 2017/8/16.
 */

public class TitleViewHolder extends BaseViewHolder<CharSequence> {

    public static final Creator<CharSequence> CREATOR = (inflater, parent) -> new TitleViewHolder(inflater.inflate(R.layout.item_header, parent ,false));

    private TextView title;

    public TitleViewHolder(View itemView) {
        super(itemView);

        title = itemView.findViewById(android.R.id.title);
    }

    @Override
    public void onBind() {
        title.setText(getData());
    }
}
