package model

import java.util.*

class TinyNode(name: String) {
    val Name = name
    val attrs: MutableMap<String, String> = HashMap()
    val Children: MutableList<TinyNode> = ArrayList()

    override fun toString(): String {
        return Name
    }

    public fun extractAttribute(attrName: String): String {
        if (attrs.containsKey(attrName)) {
            var result = attrs.get(attrName) as String
            attrs.remove(attrName)
            return result
        }
        return ""
    }

    fun hasChildren(): Boolean {
        return Children.count() != 0
    }
}