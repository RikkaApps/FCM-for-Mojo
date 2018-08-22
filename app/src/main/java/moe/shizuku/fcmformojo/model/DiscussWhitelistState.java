package moe.shizuku.fcmformojo.model;

import java.util.Objects;
import java.util.Set;

import moe.shizuku.fcmformojo.model.openqq.Discuss;

/**
 * Created by rikka on 2017/9/2.
 */

public class DiscussWhitelistState extends WhitelistState<String, Discuss> {

    public DiscussWhitelistState(boolean enabled, Set<String> list) {
        super(enabled, list);
    }

    @Override
    public boolean equals(String o1, Discuss o2) {
        return Objects.equals(o1, o2.getName());
    }
}
