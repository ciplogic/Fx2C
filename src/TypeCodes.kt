object TypeCodes {

    public val Byte = 0
    public val Char = 1
    public val Int = 2
    public val String = 3
    public val Double = 4
    public val Float = 5
    public val Object = 6
    public fun TypeNameToTypeCode(typeName: String): Int {
        when (typeName) {
            "byte" -> return Byte
            "char" -> return Char
            "int" -> return Int
            "double" -> return Double
            "float" -> return Float
            "java.lang.String" -> return String
        }
        return Object
    }
}