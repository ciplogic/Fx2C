package model.reflectutils;

import infrastructure.JavaTiny;
import infrastructure.TypeCode;
import model.MathUtils;
import utils.ReflectionResolver;
import utils.StringUtils;

import java.lang.reflect.Method;
import java.text.MessageFormat;
import java.util.*;

import static java.lang.System.out;

/**
 * Created by Ciprian on 6/24/2016.
 */
public class ConvertUtils {
    public static String computeColorAttributeName(Class<?> parameterType, String attributeValue) {
        if (attributeValue.startsWith("0x")) {
            String hexColorValue = "Color.web(\"" + attributeValue + "\")";
            return hexColorValue;
        }
        return ConvertUtils.computeEnumAttributeName(parameterType, attributeValue);
    }


    public static String handleSettingInnerText(ReflectionResolver resolver, Class<?> parentClass, JavaTiny child, String innerText, JavaTiny tinyNode, String parentControl) {
        Method method = resolver.resolveClassProperty(parentClass, child.getName(), false);
        Class<?> returnType = method.getReturnType();
        Method methodAddAll = resolver.getMethod(returnType, "addAll", Optional.of(1));
        Class<?> collectionType = methodAddAll.getParameterTypes()[0];
        Method methodGetInCollection = resolver.getMethod(collectionType, "get", Optional.of(1));
        String returnNameType = methodGetInCollection.getReturnType().getSimpleName();

        String arrayData = "";
        switch (returnNameType) {
            case "float":
                arrayData = MathUtils.parseFloatToCombinedString(innerText);
                break;
            case "int":
                arrayData = MathUtils.parseIntToCombinedString(innerText);
                break;
            case "double":
                arrayData = MathUtils.parseDoubleToCombinedString(innerText);
                break;
            default:
                System.out.println("Never handled");
        }
        String resultCodeLine = MessageFormat.format("{0}.get{1}().setAll({2})",
                parentControl,
                StringUtils.indent(child.getName()),
                arrayData);

        return resultCodeLine;
    }

    public static String prepareFunctionParam(String attributeValue, Class<?> parameterType) {
        String parameterValue;
        int typeCodeParameter = TypeCode.TypeNameToTypeCode(parameterType);
        switch (typeCodeParameter) {
            case TypeCode.String: {
                parameterValue = "\"" + attributeValue + "\"";
                break;
            }
            case TypeCode.Enum: {
                parameterValue = ConvertUtils.computeEnumAttributeName(parameterType, attributeValue);
                break;
            }
            case TypeCode.Color: {
                parameterValue = ConvertUtils.computeColorAttributeName(parameterType, attributeValue);
                break;
            }
            case TypeCode.Object: {
                if ("javafx.event.EventHandler".equals(parameterType.getTypeName())) {
                    parameterValue = "_controller::" + attributeValue.substring(1);
                } else {
                    parameterValue = attributeValue;
                }
                break;
            }
            case TypeCode.Double: {
                parameterValue = ConvertUtils.computeDoubleAttributeName(attributeValue);
                break;
            }
            default: {
                parameterValue = attributeValue;
                break;
            }
        }
        return parameterValue;
    }

    public static String computeDoubleAttributeName(String attributeValue) {
        String parameterValue;
        if (attributeValue.equalsIgnoreCase("Infinity")) {
            parameterValue = "Double.POSITIVE_INFINITY";
        } else if (attributeValue.equalsIgnoreCase("-Infinity")) {
            parameterValue = "Double.NEGATIVE_INFINITY";
        } else {
            parameterValue = attributeValue;
        }
        return parameterValue;
    }

    public static String computeEnumAttributeName(Class<?> parameterType, String attributeValue) {
        Object[] constants = parameterType.getEnumConstants();
        Map<String, String> enumNames = new HashMap<>();
        for (Object constant : constants) {
            String constText = constant.toString();
            enumNames.put(constText.toLowerCase(), constText);
        }
        String attributeNameLowered = attributeValue.toLowerCase();
        String foundMappedName = enumNames.getOrDefault(attributeNameLowered, "");

        String parameterTypeName = parameterType.toString();
        String remainingName = StringUtils.removeAfterLastSeparator(parameterTypeName, "\\.");
        String remainingNameCorrected = remainingName.replace('$', '.');

        String result = remainingNameCorrected + "." + foundMappedName;

        return result;

    }


    public static String addCodeForSetter(List<String> childControlNames, ReflectionResolver resolver,
                                          Class<?> parentClass, String parentControl, String containerMethod) {
        Method method = resolver.resolveClassProperty(parentClass, containerMethod, false);
        boolean isList = false;
        if (method == null) {
            out.println("Method $containerMethod not found");
            return "";
        }
        if (childControlNames.isEmpty()) {
            return "";
        }
        Class<?> returnType = method.getReturnType();
        if (returnType.getName().equals(returnType.getName().startsWith("javafx.collections."))) {
            isList = true;
        }
        String codeLine;
        if (isList) {
            codeLine = MessageFormat.format("{0}.{1}().addAll({2})",
                    parentControl,
                    method.getName(),
                    StringUtils.join(", ", childControlNames));
        } else {
            codeLine = parentControl + ".set" + StringUtils.indent(containerMethod) + "(" + childControlNames.get(0) + ")";
        }
        return codeLine;
    }

    public static String addCodeValues(JavaTiny tinyNode, String controlName) {
        List<String> parameters = new ArrayList<>();
        for (Map.Entry<String, String> entry : tinyNode.Attributes.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            parameters.add(value);
        }
        String paramLines = StringUtils.join(", ", parameters);
        String codeLine = MessageFormat.format("{0} {1} = new {2}({3})",
                tinyNode.getName(),
                controlName,
                tinyNode.getName(),
                paramLines);
        return codeLine;
    }

    public static String buildFunctionCode(String attributeValue, String controlName, String methodName, Class<?> parameterType) {
        String parameterValue = prepareFunctionParam(attributeValue, parameterType);

        String codeLine = controlName + "." + methodName + "(" + parameterValue + ")";
        return codeLine;
    }
}
