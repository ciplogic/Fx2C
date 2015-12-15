package model;

import model.JavaTiny;
import utils.OsUtils;
import utils.ReflectionResolver;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static java.lang.System.out;
import java.util.HashMap;
import javafx.scene.image.ImageView;
import utils.StringUtils;

public class ControlFactory {

    private final List<String> _controlLines;
    private final JavaTiny _tinyNode;
    private final ReflectionResolver _resolver;

    int _controlIndex = 1;

    public ControlFactory(List<String> buildControlsLines, JavaTiny tinyNode, ReflectionResolver resolver) {
        _controlLines = buildControlsLines;
        _tinyNode = tinyNode;
        _resolver = resolver;
    }

    public void process() {

        String firstControl = setupControl(_tinyNode, _resolver);
        addCodeLine("_view = " + firstControl + ";");
    }

    private String setupControl(JavaTiny tinyNode, ReflectionResolver resolver) {
        
        String controlName = newControlName();
        Class controlClass = resolver.resolve(tinyNode.getName());
        if (resolver.hasDefaultConstructor(controlClass)) {
            addCodeLine(tinyNode.getName() + " " + controlName + " = new " + tinyNode.getName() + "()");
            setupAttributes(tinyNode, resolver, controlName, controlClass);
        } else {
            addCodeValues(tinyNode, controlClass, controlName, resolver);
        }

        setupId(tinyNode, controlName);

        if (tinyNode.hasChildren()) {

            buildChildrenControls(resolver, tinyNode, controlName, controlClass);
        }

        return controlName;

    }

    String addCodeForSetter(List<String> childControlNames, ReflectionResolver resolver,
            Class<?> parentClass, String parentControl, String containerMethod) {
        Method method = resolver.resolveClassProperty(parentClass, containerMethod, false);
        boolean isList = false;
        Class<?> returnType = method.getReturnType();
        if (method == null) {
            out.println("Method $containerMethod not found");
            return "";
        }
        if(childControlNames.isEmpty()){
            return "";
        }
        if (returnType.getName().equals("javafx.collections.ObservableList")) {
            isList = true;
        }
        String codeLine;
        if (isList) {
            codeLine = parentControl + "." + method.getName() + "().addAll(" + StringUtils.join(", ", childControlNames) + ")";
        } else {
            codeLine = parentControl + ".set" + StringUtils.indent(containerMethod) + "(" + childControlNames.get(0) + ")";
        }
        return codeLine;

    }

    private void buildChildrenControls(ReflectionResolver resolver, JavaTiny tinyNode, String parentControl, Class<?> parentClass) {
        for (JavaTiny child : tinyNode.getChildren()) {
            List<JavaTiny> controlChildNodes = child.getChildren();
            List<String> childControlNames = new ArrayList<>();
            for (JavaTiny childControlNode : controlChildNodes) {
                String childControlName = setupControl(childControlNode, resolver);
                childControlNames.add(childControlName);
            }
            String containerMethod = child.getName();
            String childrenSetterCode = addCodeForSetter(childControlNames, resolver, parentClass, parentControl, containerMethod);
            if (OsUtils.isNullOrEmpty(childrenSetterCode)) {
                continue;
            }
            addCodeLine(childrenSetterCode);
        }

    }

    private String newControlName() {
        String result = "ctrl_" + _controlIndex;
        _controlIndex++;
        return result;
    }

    private void setupId(JavaTiny tinyNode, String controlName) {
        String id = tinyNode.extractAttribute("fx:id");
        String idDirect = tinyNode.extractAttribute("id");
        if (OsUtils.isNullOrEmpty(id)) {
            id = idDirect;
        }
        if (!OsUtils.isNullOrEmpty(id)) {
            addCodeLine("_controller." + id + " = " + controlName);
        }
    }

    void setupAttributes(JavaTiny controlNode, ReflectionResolver resolver, String controlName, Class<?> controlClass) {

        Map<String, String> attrs = controlNode.getAttributes();
        for (Map.Entry<String, String> attr : attrs.entrySet()) {
            String attrName = attr.getKey();
            Method resolvedMethod = resolver.resolveClassProperty(controlClass, attrName, true);

            if (resolvedMethod == null) {
                out.println("cannot find method '" + attrName + "'");
                continue;
            }
            Class<?> parameterType = resolvedMethod.getParameterTypes()[0];
            String codeLine = buildFunctionCode(attr.getValue(), controlName, resolvedMethod.getName(), parameterType);
            addCodeLine(codeLine);
        }
    }

    private String buildFunctionCode(String attributeValue, String controlName, String methodName, Class<?> parameterType) {
        String parameterValue;
        int typeCodeParameter = TypeCode.TypeNameToTypeCode(parameterType);
        switch (typeCodeParameter) {
            case TypeCode.String: {
                parameterValue = "\""+attributeValue+"\"";
                break;
            }
            case TypeCode.Enum: {
                parameterValue = computeAttributeName(parameterType, attributeValue);
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
            default: {
                parameterValue = attributeValue;
                break;
            }
        }

        String codeLine = controlName + "." + methodName + "(" + parameterValue + ")";
        return codeLine;
    }

    private void addCodeLine(String codeLine) {
        _controlLines.add(codeLine);
    }

    private String computeAttributeName(Class<?> parameterType, String attributeValue) {
        Object[] constants = parameterType.getEnumConstants();
        Map<String, String> enumNames = new HashMap<>();
        for (Object constant : constants) {
            String constText = constant.toString();
            enumNames.put(constText.toLowerCase(), constText);
        }
        String attrValueLower = attributeValue.toLowerCase();
        String foundMappedName = enumNames.getOrDefault(attributeValue, "");

        String result = parameterType.getSimpleName() + "." + foundMappedName;

        return result;

    }

    private void addCodeValues(JavaTiny tinyNode, Class controlClass, String controlName, ReflectionResolver resolver) {
        String codeLine = tinyNode.getName() + " " + controlName + " = new " + tinyNode.getName() + "(";
        List<String> parameters = new ArrayList<>();
        for (Map.Entry<String, String> entry : tinyNode.Attributes.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            parameters.add(value);
        }
        codeLine = codeLine+ StringUtils.join(", ", parameters);
        codeLine =codeLine+ ")";
        addCodeLine(codeLine);
    }
}
