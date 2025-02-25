package me.wayne.daos;

public interface Printable {
    
    public String toPrint(int indent);

    public static String print(int indent, Object[] arr) {
        if (arr.length == 0) return "[]";
        else if (arr.length == 1) return arr[0].toString();

        StringBuilder sb = new StringBuilder();
        for (int i = 0;i < arr.length;i++) {
            Object obj = arr[i];
            if (i != 0) for (int j = 0; j < indent*3; j++) sb.append(" ");
            sb.append((i+1) + ") ").append(obj.toString());
        }
        return sb.toString();
    }

}
