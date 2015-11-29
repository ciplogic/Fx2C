import model.JavaTiny;
import utils.OsUtils;
import utils.ReflectionResolver;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static java.lang.System.out;

public class ControlFactory {
    private final List<String> _controlLines;
    private final JavaTiny _tinyNode;
    private final ReflectionResolver _resolver;

    int _controlIndex = 1;

    public ControlFactory(List<String> buildControlsLines, JavaTiny tinyNode, ReflectionResolver resolver){
        _controlLines = buildControlsLines;
        _tinyNode = tinyNode;
        _resolver = resolver;
    }
    public void process() {

        String firstControl = setupControl(_tinyNode, _resolver);
        addCodeLine("_view = "+firstControl+";");
    }

    private String setupControl(JavaTiny tinyNode, ReflectionResolver resolver) {

        String controlName = newControlName();
        addCodeLine(tinyNode.getName() + " " + controlName + " = new " + tinyNode.getName() + "()");
        Class controlClass = resolver.resolve(tinyNode.getName());
        setupId(tinyNode, controlName);
        setupAttributes(tinyNode, resolver, controlName, controlClass);

        if (tinyNode.hasChildren()) {

            buildChildrenControls(resolver, tinyNode, controlName, controlClass);
        }

        return controlName;

    }

    private void buildChildrenControls(ReflectionResolver resolver, JavaTiny tinyNode, String parentControl, Class<?> parentClass) {
        for (JavaTiny child : tinyNode.getChildren()) {
            List<JavaTiny> controlChildNodes = child.getChildren();
            List<String>  childControlNames = new ArrayList<>();
            for (JavaTiny childControlNode : controlChildNodes) {
                String childControlName = setupControl(childControlNode, resolver);
                childControlNames.add(childControlName);
            }
            String containerMethod = child.getName();
            Method method = resolver.resolveClassProperty(parentClass, containerMethod, false);
            boolean isList = false;
            Class<?> returnType = method.getReturnType();
            if (method == null) {
                out.println("Method $containerMethod not found");
                continue;
            }
            if(returnType.getName().equals("javafx.collections.ObservableList"))
            {
                isList = true;
            }
            String codeLine;
            if(isList)
            {
                codeLine = parentControl+"."+method.getName()+"().addAll("+ OsUtils.join(", ", childControlNames)+")";
            }else{
                codeLine = parentControl+".set"+OsUtils.indent(containerMethod)+"("+ childControlNames.get(0)+")";
            }


            addCodeLine(codeLine);
        }


    }

    private String newControlName() {
        String result = "ctrl_" + _controlIndex;
        _controlIndex++;
        return result;
    }

    private void setupId(JavaTiny tinyNode , String controlName) {
        String id = tinyNode.extractAttribute("fx:id");
        String idDirect = tinyNode.extractAttribute("id");
        if (OsUtils.isNullOrEmpty(id)) {
            id = idDirect;
        }
        if (!OsUtils.isNullOrEmpty(id)) {
            addCodeLine("_controller."+id+" = "+controlName);
        }
    }

    void setupAttributes(JavaTiny controlNode , ReflectionResolver resolver , String controlName, Class<?> controlClass) {

        Map<String, String> attrs = controlNode.getAttributes();
        for (Map.Entry<String, String> attr : attrs.entrySet()) {
            String attrName = attr.getKey();
            Method resolvedMethod = resolver.resolveClassProperty(controlClass, attrName, true);

            if (resolvedMethod == null) {
                out.println("cannot find method '" + attrName+ "'");
                continue;
            }
            Class<?> parameterType = resolvedMethod.getParameterTypes()[0];
            String codeLine = buildFunctionCode(attr.getValue(), controlName, resolvedMethod.getName(), parameterType.getTypeName());
            addCodeLine(codeLine);
        }
    }

    private String buildFunctionCode(String attributeValue, String controlName, String methodName, String parameterTypeName) {
        String parameterValue;
        int typeCodeParameter = TypeCode.TypeNameToTypeCode(parameterTypeName);
        switch (typeCodeParameter) {
            case TypeCode.String : {
                parameterValue = "\"$attributeValue\"";
                break;
            }
            case TypeCode.Object : {
                if ("javafx.event.EventHandler".equals(parameterTypeName)) {
                    parameterValue = "_controller::"+attributeValue.substring(1);
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

        String codeLine = "    "+ controlName+"."+methodName+"("+ parameterValue+")";
        return codeLine;
    }

    private void addCodeLine(String codeLine) {
        _controlLines.add(codeLine);
    }
}
