package moe.shizuku.fcmformojo.model;

import android.support.annotation.Keep;
import android.util.Pair;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

/**
 * Created by rikka on 2017/8/28.
 */

@Keep
public class BlacklistState {

    private boolean enabled;
    private List<Long> list;
    private transient List<Pair<Group, Boolean>> states;

    public BlacklistState(boolean enabled, List<Long> list) {
        this.enabled = enabled;
        this.list = list;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public List<Long> getList() {
        return list;
    }

    public List<Pair<Group, Boolean>> getStates() {

        return states;
    }

    public void generateStates(Collection<Group> groups) {
        if (states == null) {
            states = new ArrayList<>();
        }
        states.clear();

        for (Group group : groups) {
            boolean checked = true;
            for (long uid : list) {
                if (group.getUid() == uid) {
                    checked = false;
                    break;
                }
            }

            states.add(new Pair<>(group, checked));
        }

        states.sort(new Comparator<Pair<Group, Boolean>>() {
            @Override
            public int compare(Pair<Group, Boolean> o1, Pair<Group, Boolean> o2) {
                return Boolean.compare(o1.second, o2.second);
            }
        });
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        BlacklistState that = (BlacklistState) o;

        if (enabled != that.enabled) return false;
        return list.equals(that.list);
    }

    @Override
    public int hashCode() {
        int result = (enabled ? 1 : 0);
        result = 31 * result + list.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "BlacklistState{" +
                "enabled=" + enabled +
                ", list=" + list +
                '}';
    }
}
