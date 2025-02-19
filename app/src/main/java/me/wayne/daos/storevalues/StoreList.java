package me.wayne.daos.storevalues;

import java.util.ArrayList;
import java.util.List;

public class StoreList extends ArrayList<String> {

    public StoreList() {
        super();
    }

    public PrintableList<String> range(int start, int stop) {
        PrintableList<String> range = new PrintableList<>();
        while (stop < 0) stop += size();
        for (int i = Math.max(start, 0);i <= stop && i < size();i++) range.add(get(i));
        return range;
    }

    public void lPush(String... values) {
        for (String value : values) addFirst(value);
    }

    public void lPush(List<String> values) {
        for (String value : values) addFirst(value);
    }

    public void rPush(String... values) {
        for (String value : values) addLast(value);
    }

    public void rPush(List<String> values) {
        for (String value : values) addLast(value);
    }

    public List<String> lPop(int count) {
        PrintableList<String> removeds = new PrintableList<>();
        for (int i = 0;i < count && !isEmpty();i++) removeds.add(removeFirst());
        return removeds;
    }

    public List<String> rPop(int count) {
        PrintableList<String> removeds = new PrintableList<>();
        for (int i = 0;i < count && !isEmpty();i++) removeds.add(removeLast());
        return removeds;
    }

}