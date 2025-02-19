package me.wayne.daos;

public class Pair<T, U> implements Printable {
    private final T first;
    private final U second;

    public Pair(T first, U second) {
        this.first = first;
        this.second = second;
    }

    public Pair() {
        this.first = null;
        this.second = null;
    }

    public T getFirst() {
        return first;
    }

    public U getSecond() {
        return second;
    }

    @Override
    public String toString() {
        return toPrint(0);
    }

    @Override
    public String toPrint(int indent) {
        StringBuilder sb = new StringBuilder();
        sb.append("1) " + first + "\n");
        for (int i = 0; i < indent*3; i++) sb.append(" ");
        sb.append("2) " + second);
        return sb.toString();
    }
    
}
