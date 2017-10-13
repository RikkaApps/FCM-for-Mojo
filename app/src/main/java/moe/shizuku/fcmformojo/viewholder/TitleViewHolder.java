package moe.shizuku.fcmformojo.viewholder;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import moe.shizuku.fcmformojo.R;
import moe.shizuku.support.recyclerview.BaseViewHolder;

/**
 * Created by rikka on 2017/8/16.
 */

public class TitleViewHolder extends BaseViewHolder<CharSequence> {

    public static final Creator CREATOR = new Creator<CharSequence>() {

        @Override
        public BaseViewHolder<CharSequence> createViewHolder(LayoutInflater inflater, ViewGroup parent) {
            return new TitleViewHolder(inflater.inflate(R.layout.item_header, parent ,false));
        }
    };

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
