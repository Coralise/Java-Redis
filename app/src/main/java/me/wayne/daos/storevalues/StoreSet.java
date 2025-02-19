package me.wayne.daos.storevalues;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class StoreSet extends LinkedHashSet<String> {

    @Override
    public String toString() {
        return new PrintableList<>(this).toString();
    }

    public static StoreSet difference(List<StoreSet> hashSets) {
        StoreSet difference = (StoreSet) hashSets.get(0).clone();
        for (int i = 1;i < hashSets.size();i++) difference.removeAll(hashSets.get(i));
        return difference;
    }

    public static StoreSet difference(StoreSet... hashSets) {
        StoreSet difference = (StoreSet) hashSets[0].clone();
        for (int i = 1;i < hashSets.length;i++) difference.removeAll(hashSets[i]);
        return difference;
    }

    public static Set<String> intersection(List<StoreSet> hashSets) {
        StoreSet intersection = (StoreSet) hashSets.get(0).clone();
        for (int i = 1;i < hashSets.size();i++) intersection.retainAll(hashSets.get(i));
        return intersection;
    }

    public static StoreSet intersection(StoreSet... hashSets) {
        StoreSet intersection = (StoreSet) hashSets[0].clone();
        for (int i = 1;i < hashSets.length;i++) intersection.retainAll(hashSets[i]);
        return intersection;
    }

    public static StoreSet union(List<StoreSet> hashSets) {
        StoreSet union = new StoreSet();
        for (StoreSet hashSet : hashSets) union.addAll(hashSet);
        return union;
    }

    public static StoreSet union(StoreSet... hashSets) {
        StoreSet union = new StoreSet();
        for (StoreSet hashSet : hashSets) union.addAll(hashSet);
        return union;
    }

}