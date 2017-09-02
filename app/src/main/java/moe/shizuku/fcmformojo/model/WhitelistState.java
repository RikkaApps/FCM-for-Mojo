package moe.shizuku.fcmformojo.model;

import android.support.annotation.Keep;
import android.util.Pair;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

/**
 * Created by rikka on 2017/9/2.
 */

@Keep
public abstract class WhitelistState<T, W> {

    private boolean enabled;
    private Set<T> list;
    private transient List<Pair<W, Boolean>> states;

    public WhitelistState(boolean enabled, Set<T> list) {
        this.enabled = enabled;
        this.list = list;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public Set<T> getList() {
        return list;
    }

    public List<Pair<W, Boolean>> getStates() {
        return states;
    }

    /**
     * 将带有更多属性的完整列表与获取到的列表合并
     *
     * @param full 完整列表
     */
    public void generateStates(Collection<W> full) {
        if (states == null) {
            states = new ArrayList<>();
        }
        states.clear();

        for (W item : full) {
            boolean checked = false;
            for (T t : list) {
                if (equals(t, item)) {
                    checked = true;
                    break;
                }
            }

            states.add(new Pair<>(item, checked));
        }

        states.sort(new Comparator<Pair<W, Boolean>>() {
            @Override
            public int compare(Pair<W, Boolean> o1, Pair<W, Boolean> o2) {
                return Boolean.compare(o2.second, o1.second);
            }
        });
    }

    public abstract boolean equals(T o1, W o2);

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        WhitelistState that = (WhitelistState) o;

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
        return "WhitelistState{" +
                "enabled=" + enabled +
                ", list=" + list +
                '}';
    }
}
