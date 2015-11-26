package utils;

import java.lang.reflect.Method;
import java.util.*;

/**
 * Created by Ciprian on 11/26/2015.
 */
public class ReflectionResolver {
    public List<String>  Imports = new ArrayList<>();
    public Map<String, Class<?>> FixedTypes = new HashMap<>();
    public ReflectionResolver(List<String> imports)  {
        for (String imprt : imports) {
            if (imprt.endsWith(".*")) {
                String importSubstracted = imprt.substring(0, imprt.length() - 2);
                Imports.add(importSubstracted);
            } else {
                Class clazz = null;
                try {
                    clazz = Class.forName(imprt);
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
                FixedTypes.put(imprt, clazz);
            }
        }

    }


    public Class<?> resolve(String typeName) {
        if (FixedTypes.containsKey(typeName))
            return FixedTypes.get(typeName);
        for (String imprt: Imports) {
            String baseName = imprt+ "." + typeName;
            try {
                Class clazz = ClassLoader.getSystemClassLoader().loadClass(baseName);
                return clazz;
            } catch( Exception ex) {
                continue;
            }
        }
        return null;
    }

    Method getMethod(Class<?> clz,  String name, Optional<Integer> paramCount) {
        Method[] methods = clz.getMethods();
        int paramenterCount = paramCount.isPresent()? paramCount.get():-1;
        for (Method mth : methods) {
            int methodparameterCount = mth.getParameterCount();
            if ((paramenterCount != -1) &&  (methodparameterCount== paramenterCount)) {
                String methodName =mth.getName();
                if (methodName.equals(name)) {
                    return mth;
                }
            }
        }
        return null;
    }


    public Method resolveClassProperty(Class<?> clz, String name, boolean isSetter) {
        Method mth;
        if (isSetter) {
            mth = getMethod(clz, "set" + OsUtils.indent(name), Optional.of(1));
        } else {
            mth = getMethod(clz, "get" + OsUtils.indent(name), Optional.of(0));
        }
        return mth;
    }
}
