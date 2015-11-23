package utils

import java.lang.reflect.Method
import java.util.*

class ClassResolver(imports: List<String>) {
    val Imports: MutableList<String> = ArrayList()
    val FixedTypes: MutableMap<String, Class<*>> = HashMap()

    init {
        for (import in imports) {
            if (import.endsWith(".*")) {
                val importSubstracted = import.removeRange(import.length - 2, import.length)
                Imports.add(importSubstracted)
            } else {
                val clazz = Class.forName(import)
                FixedTypes[import] = clazz
            }
        }
    }

    public fun resolve(typeName: String): Class<*> {
        if (FixedTypes.containsKey(typeName))
            return FixedTypes.get(typeName) as Class<*>;
        for (import in Imports) {
            val baseName = import + "." + typeName;
            try {
                val clazz = ClassLoader.getSystemClassLoader().loadClass(baseName)
                return clazz
            } catch(e: Exception) {
                continue;
            }
        }
        throw ClassNotFoundException()
    }

    fun getMethod(clz: Class<*>, name: String, paramCount: Optional<Int>): Method? {
        try {
            val methods = clz.getMethods()
            for (mth in methods) {
                if (paramCount.isPresent && mth.parameterCount == paramCount.get()) {
                    if (mth.name == name) {
                        return mth
                    }
                }
            }
            return null
        } catch(ex: NoSuchMethodException) {
            return null
        }
    }


    public fun resolveClassProperty(clz: Class<*>, name: String, isSetter: Boolean): Method? {
        var mth: Method?
        if (isSetter) {
            mth = getMethod(clz, "set" + OsUtils.indent(name), Optional.of(1))
        } else {
            mth = getMethod(clz, "get" + OsUtils.indent(name), Optional.of(0))
        }
        return mth
    }
}