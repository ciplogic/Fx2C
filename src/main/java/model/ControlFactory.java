package model;

import infrastructure.JavaTiny;
import javafx.geometry.Pos;
import javafx.scene.layout.Priority;
import utils.OsUtils;
import utils.ReflectionResolver;
import utils.StringUtils;

import java.lang.reflect.Method;
import java.text.MessageFormat;
import java.util.*;
import java.util.Map.Entry;

import static java.lang.System.out;
import static java.text.MessageFormat.format;
import static model.reflectutils.ConvertUtils.*;
import static utils.StringUtils.quote;

public class ControlFactory {

    public static final String FX_NODE_ID = "fx:id";

    public static final String PROP_POSTFIX_INT = "index"; // complex node properties ending, like  -rowindex/-columnindex

    public static Map<String, Class<?>> specPropClass = new LinkedHashMap<>();
    public static Map<String, Class<?>> attr2Children = new HashMap<>();

    static {
        specPropClass.put("alignment", Pos.class);
        specPropClass.put("hgrow", Priority.class);
        specPropClass.put("vgrow", Priority.class);
        specPropClass.put("row", Integer.class);
        specPropClass.put("column", Integer.class);
    }

    static {
        attr2Children.put("styleClass", String.class);
    }

    private final List<String> _controlLines;
    private final JavaTiny _tinyNode;
    private final ReflectionResolver _resolver;
    private Class<?> controllerClass;

    int _controlIndex = 1;
    private GeneratorConfiguration configuration;

    public ControlFactory(List<String> buildControlsLines, JavaTiny tinyNode, ReflectionResolver resolver, GeneratorConfiguration configuration) {
        _controlLines = buildControlsLines;
        _tinyNode = tinyNode;
        _resolver = resolver;
        this.configuration = configuration;
    }

    public void process() {
        String firstControl = setupControl(_tinyNode, _resolver);
        addCodeLine(format("_view = {0};", firstControl));


    }

    private String setupControl(JavaTiny tinyNode, ReflectionResolver resolver) {

        String controlName = newControlName(tinyNode);
        Class controlClass = resolver.resolve(tinyNode.getName());

        if (resolver.hasDefaultConstructor(controlClass)) {
            addCodeLine(format("{0} {1} = new {2}()",
                    tinyNode.getName(),
                    controlName,
                    tinyNode.getName()));

            setupId(tinyNode, controlName);
            setupAttributes(tinyNode, resolver, controlName, controlClass);
        } else {
            String codeValues = addCodeValues(tinyNode, controlName);

            addCodeLine(codeValues);
        }

        if (tinyNode.hasChildren()) {
            buildChildrenControls(resolver, tinyNode, controlName, controlClass);
        }

        return controlName;

    }

    private void buildChildrenControls(ReflectionResolver resolver, JavaTiny tinyNode, String parentControl, Class<?> parentClass) {
        for (JavaTiny child : tinyNode.getChildren()) {
            if (handleSpecialChild(child, parentControl)) {
                continue;
            }
            if (!OsUtils.isNullOrEmpty(child.getInnerText())) {
                String codeLine = handleSettingInnerText(resolver, parentClass, child, child.getInnerText(), tinyNode, parentControl);
                addCodeLine(codeLine);
                continue;
            }
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

    private String newControlName(JavaTiny jnode) {
        String name = jnode.Attributes.get(FX_NODE_ID);
        if (name != null) {
            return name;
        }

        String result = MessageFormat.format("ctrl_{0}", _controlIndex);
        _controlIndex++;
        return result;
    }

    private void setupId(JavaTiny tinyNode, String controlName) {
        String id = tinyNode.extractAttribute(FX_NODE_ID).toString();

        boolean isKotlin = configuration.isKotlinController;

        if (OsUtils.isNullOrEmpty(id)) {
            return;
        }
        if (isKotlin) {
            addCodeLine(format("_controller.set{0}({1});", StringUtils.indent(id), controlName));
        } else {
            addCodeLine(format("_controller.{0} = {1}", id, controlName));
        }
    }

    private void setupAttributes(JavaTiny controlNode, ReflectionResolver resolver, String controlName, Class<?> controlClass) {

        Map<String, String> attrs = controlNode.getAttributes();
        for (Map.Entry<String, String> attr : attrs.entrySet()) {
            String attrName = attr.getKey();
            String attrValue = attr.getValue();
            String codeLine;
            if (attr2Children.get(attrName) != null) {
                List<String> childControlNames = new ArrayList<>();
                childControlNames.add(prepareFunctionParam(attr.getValue(), attr2Children.get(attrName)));
                codeLine = addCodeForSetter(childControlNames, resolver, controlClass, controlName, attrName);
            } else {
                if (specialHandleAttribute(attr, controlName)) {
                    continue;
                }
                if (staticMethodHandleAttribute(attr, controlName)) {
                    continue;
                }
                Method resolvedMethod = resolver.resolveClassProperty(controlClass, attrName, true);

                if (resolvedMethod == null) {
                    if (isNodeProperty(attrName)) {
                        setupNodeProperty(attrName, attrValue, controlName);
                    } else {
                        out.println("cannot find method '" + attrName + "'");
                    }
                    continue;
                }

                Class<?> parameterType = resolvedMethod.getParameterTypes()[0];
                codeLine = buildFunctionCode(attr.getValue(), controlName, resolvedMethod.getName(), parameterType);
            }
            addCodeLine(codeLine);
        }
    }

    private boolean staticMethodHandleAttribute(Entry<String, String> attr, String controlName) {
        String attrName = attr.getKey();
        String attrValue = attr.getValue();
        if(!attrName.contains(".")){
            return false;
        }
        String[] itemsExpression = attrName.split("\\.");

        String className = itemsExpression[0];
        String staticMethodName = itemsExpression[1];

        Class<?> clsResolved = _resolver.resolve(className);
        if(clsResolved==null){
            out.println("staticMethodHandleAttribute cannot resolve"+className);
            return false;
        }

        Method setterMethod = _resolver.resolveClassStaticSetter(clsResolved, staticMethodName);

        String codeLine = format("{0}.{1}({2}, {3})", className, setterMethod.getName(),controlName, attrValue);
        addCodeLine(codeLine);
        return true;
    }

    private boolean specialHandleAttribute(Entry<String, String> attr, String controlName) {
        String attrName = attr.getKey();
        String attrValue = attr.getValue();
        switch (attrName) {
            case "xmlns": {
                return true;
            }
            case "stylesheets": {
                String codeLine = format("{0}.getStylesheets().add(\"{1}\")", controlName, attrValue.substring(1));
                addCodeLine(codeLine);
                return true;
            }
            default: {
                return false;
            }
        }
    }


    private boolean handleSpecialChild(JavaTiny child, String parentControl) {
        String childName = child.getName();
        switch (childName) {
            case "styleClass":
                handleStyleClass(parentControl, child);
                return true;
            default: {
                return false;
            }
        }
    }

    private void handleStyleClass(String parentControl, JavaTiny child) {
        for (JavaTiny childStyleRow : child.Children) {
            Map<String, String> attrs = childStyleRow.getAttributes();
            for (Map.Entry<String, String> attr : attrs.entrySet()) {
                String attrValue = attr.getValue();
                String codeLine = format("{0}.getStyleClass().add(\"{1}\")", parentControl, attrValue);

                addCodeLine(codeLine);
            }
        }
    }


    private void addCodeLine(String codeLine) {
        if (OsUtils.isNullOrEmpty(codeLine)) {
            return;
        }
        _controlLines.add(codeLine);
    }


    private boolean isNodeProperty(String attrName) {
        return attrName.contains(".");
    }

    private void setupNodeProperty(String attrName, String value, String controlName) {
        Class<?> valueClass = String.class;

        attrName = prepareAttrName(attrName);
        Entry<String, Class<?>> specProp = findSpecProp(attrName);
        if (specProp != null) {
            valueClass = specProp.getValue();
        }

        value = prepareFunctionParam(value, valueClass);

        String codeLine = format("{0}.getProperties().put({1},{2})", controlName, quote(attrName), value);
        addCodeLine(codeLine);
    }

    private String prepareAttrName(String attrName) {
        String localAttrName = attrName;
        localAttrName = localAttrName.replace('.', '-').toLowerCase();
        if (localAttrName.endsWith(PROP_POSTFIX_INT)) {
            localAttrName = localAttrName.substring(0, localAttrName.length() - 5);
        }
        return localAttrName;
    }

    private Entry<String, Class<?>> findSpecProp(String attrName) {
        for (Entry<String, Class<?>> prop : specPropClass.entrySet()) {
            if (attrName.endsWith(prop.getKey())) {
                return prop;
            }
        }
        return null;
    }
}
