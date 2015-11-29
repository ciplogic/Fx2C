package model;


public final class TypeCode {
    public final static int Byte = 0;
    public final static int Char = 1;
    public final static int Int = 2;
    public final static int String = 3;
    public final static int Double = 4;
    public final static int Float = 5;
    public final static int Object = 6;
    public static int TypeNameToTypeCode(Class<?> type) {
        String typeName = type.getTypeName();
        switch (typeName) {
            case "byte" : return Byte;
            case "char" : return Char;
            case "int" : return Int;
            case "double" : return Double;
            case "float" : return Float;
            case "java.lang.String" : return String;
        }
        return Object;
    }
}
