package me.wayne.daos.storevalues;

import java.util.ArrayList;
import java.util.Collection;

import me.wayne.daos.Printable;

public class PrintableList<E> extends ArrayList<E> {

    private int indent;

    public void setIndent(int indent) {
        this.indent = indent;
    }

    public PrintableList() {
        this(0);
    }

    public PrintableList(Collection<E> list) {
        this(list, 0);
    }

    public PrintableList(int indent) {
        super();
        this.indent = indent;
    }

    public PrintableList(Collection<E> list, int indent) {
        super(list);
        this.indent = indent;
    }

    @Override
    public String toString() {
        if (isEmpty()) return "[]";
        if (size() == 1) {
            if (get(0) instanceof Collection) {
                PrintableList<?> subsequentList = new PrintableList<>((Collection<?>) get(0), indent);
                return subsequentList.toString();
            } else if (get(0) instanceof Printable printable) {
                return printable.toPrint(indent);
            } else {
                return get(0).toString();
            }
        }
        
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < size(); i++) {
            if (i > 0) for (int j = 0; j < indent*3; j++) stringBuilder.append(" ");
            stringBuilder.append(i + 1).append(") ");
            if (get(i) instanceof Collection) {
                PrintableList<?> subsequentList = new PrintableList<>((Collection<?>) get(i), indent + 1);
                stringBuilder.append(subsequentList.toString());
            } else if (get(i) instanceof Printable printable) {
                stringBuilder.append(printable.toPrint(indent + 1));

            } else {
                stringBuilder.append(get(i).toString());
            }
            if (i < size() - 1) stringBuilder.append("\n");
        }
        return stringBuilder.toString();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + indent;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        if (!super.equals(obj)) return false;

        PrintableList<?> that = (PrintableList<?>) obj;

        return indent == that.indent;
    }
    
}
