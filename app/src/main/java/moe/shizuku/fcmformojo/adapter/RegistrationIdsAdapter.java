package moe.shizuku.fcmformojo.adapter;

import java.util.HashSet;
import java.util.Set;

import moe.shizuku.fcmformojo.model.RegistrationId;
import moe.shizuku.support.recyclerview.BaseRecyclerViewAdapter;
import moe.shizuku.support.recyclerview.ClassCreatorPool;

/**
 * Created by rikka on 2017/8/16.
 */

public class RegistrationIdsAdapter extends BaseRecyclerViewAdapter {

    @Override
    public ClassCreatorPool getCreatorPool() {
        return (ClassCreatorPool) super.getCreatorPool();
    }

    public Set<RegistrationId> getRegistrationIds() {
        Set<RegistrationId> ids = new HashSet<>();
        for (Object obj : getItems()) {
            if (obj instanceof RegistrationId) {
                ids.add((RegistrationId) obj);
            }
        }
        return ids;
    }
}
