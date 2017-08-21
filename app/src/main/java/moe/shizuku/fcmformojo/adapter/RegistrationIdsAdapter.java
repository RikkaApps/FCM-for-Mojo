package moe.shizuku.fcmformojo.adapter;

import java.util.ArrayList;
import java.util.List;

import moe.shizuku.fcmformojo.model.RegistrationId;
import moe.shizuku.utils.recyclerview.BaseRecyclerViewAdapter;

/**
 * Created by rikka on 2017/8/16.
 */

public class RegistrationIdsAdapter extends BaseRecyclerViewAdapter {

    public List<RegistrationId> getRegistrationIds() {
        List<RegistrationId> ids = new ArrayList<>();
        for (Object obj : getItems()) {
            if (obj instanceof RegistrationId) {
                ids.add((RegistrationId) obj);
            }
        }
        return ids;
    }
}
