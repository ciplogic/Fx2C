package model;

import static java.lang.System.out;
import static utils.StringUtils.quote;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import infrastructure.JavaTiny;
import infrastructure.TypeCode;
import javafx.geometry.Pos;
import javafx.scene.layout.Priority;
import utils.OsUtils;
import utils.ReflectionResolver;
import utils.StringUtils;

public class ControlFactory {

	public static final String FX_NODE_ID = "fx:id";

	public static final String propPostfixInt = "index"; // complex node properties ending, like  -rowindex/-columnindex
    
    public static Map<String, Class<?>> specPropClass = new LinkedHashMap<>();
    static {
    	specPropClass.put( "alignment", Pos.class );
    	specPropClass.put( "hgrow", Priority.class );
    	specPropClass.put( "vgrow", Priority.class );
    	specPropClass.put( "row", Integer.class );
    	specPropClass.put( "column", Integer.class );
    }
	
    public static Map<String, Class<?>> attr2Children = new HashMap<>();
    static {
    	attr2Children.put( "styleClass", String.class );
    }
    
    private final List<String> _controlLines;
    private final JavaTiny _tinyNode;
    private final ReflectionResolver _resolver;
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
        addCodeLine("_view = " + firstControl + ";");
    }

    private String setupControl(JavaTiny tinyNode, ReflectionResolver resolver) {

        String controlName = newControlName( tinyNode );
        Class controlClass = resolver.resolve(tinyNode.getName());

        if (resolver.hasDefaultConstructor(controlClass)) {
            addCodeLine(tinyNode.getName() + " " + controlName + " = new " + tinyNode.getName() + "()");
            setupId(tinyNode, controlName);
            setupAttributes(tinyNode, resolver, controlName, controlClass);
        } else {
            addCodeValues(tinyNode, controlClass, controlName, resolver);
        }

        if (tinyNode.hasChildren()) {

            buildChildrenControls(resolver, tinyNode, controlName, controlClass);
        }

        return controlName;

    }

    String addCodeForSetter(List<String> childControlNames, ReflectionResolver resolver,
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

    private String newControlName( JavaTiny jnode ) {
    	String name = jnode.Attributes.get(FX_NODE_ID);
    	if( name != null )
    		return name;
    	
        String result = "ctrl_" + _controlIndex;
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
            addCodeLine("_controller.set" + StringUtils.indent(id) + "(" + controlName + ");");
        } else {
            addCodeLine("_controller." + id + " = " + controlName);
        }
    }

    void setupAttributes(JavaTiny controlNode, ReflectionResolver resolver, String controlName, Class<?> controlClass) {

        Map<String, String> attrs = controlNode.getAttributes();
        for (Map.Entry<String, String> attr : attrs.entrySet()) {
            String attrName = attr.getKey();
            String codeLine;
            if( attr2Children.get( attrName ) != null ){
            	List<String> childControlNames = new ArrayList<>();
            	childControlNames.add( prepareFunctionParam(attr.getValue(), attr2Children.get( attrName )) );
            	codeLine = addCodeForSetter( childControlNames, resolver, controlClass, controlName, attrName );
            } else {
	            Method resolvedMethod = resolver.resolveClassProperty(controlClass, attrName, true);
	
	            if (resolvedMethod == null) {
	            	if( isNodeProperty(attrName) )
	            		setupNodeProperty( attrName, attr.getValue(), controlName );
	            	else 
	            		out.println("cannot find method '" + attrName + "'");
	                continue;
	            }
            
	            Class<?> parameterType = resolvedMethod.getParameterTypes()[0];
	            codeLine = buildFunctionCode(attr.getValue(), controlName, resolvedMethod.getName(), parameterType);
            }
            addCodeLine(codeLine);
        }
    }


	private String buildFunctionCode(String attributeValue, String controlName, String methodName, Class<?> parameterType) {
        String parameterValue = prepareFunctionParam(attributeValue, parameterType);

        String codeLine = controlName + "." + methodName + "(" + parameterValue + ")";
        return codeLine;
    }

	private String prepareFunctionParam(String attributeValue, Class<?> parameterType) {
		String parameterValue;
		int typeCodeParameter = TypeCode.TypeNameToTypeCode(parameterType);
        switch (typeCodeParameter) {
            case TypeCode.String: {
                parameterValue = "\"" + attributeValue + "\"";
                break;
            }
            case TypeCode.Enum: {
                parameterValue = computeEnumAttributeName(parameterType, attributeValue);
                break;
            }
            case TypeCode.Color: {
                parameterValue = computeColorAttributeName(parameterType, attributeValue);
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
            	parameterValue = computeDoubleAttributeName(attributeValue);
            	break;
            }
            default: {
                parameterValue = attributeValue;
                break;
            }
        }
		return parameterValue;
	}


    private void addCodeLine(String codeLine) {
        _controlLines.add(codeLine);
    }

    private String computeColorAttributeName(Class<?> parameterType, String attributeValue) {
        if (attributeValue.startsWith("0x")) {
            String hexColorValue = "Color.web(\"" + attributeValue + "\")";
            return hexColorValue;
        }
        return computeEnumAttributeName(parameterType, attributeValue);
    }

    private String computeEnumAttributeName(Class<?> parameterType, String attributeValue) {
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
    
	private String computeDoubleAttributeName(String attributeValue) {
		String parameterValue;
		if (attributeValue.equalsIgnoreCase("Infinity"))
			parameterValue = "Double.POSITIVE_INFINITY";
		else if (attributeValue.equalsIgnoreCase("-Infinity"))
			parameterValue = "Double.NEGATIVE_INFINITY";
		else 
			parameterValue = attributeValue;
		return parameterValue;
	}

    private void addCodeValues(JavaTiny tinyNode, Class controlClass, String controlName, ReflectionResolver resolver) {
        String codeLine = tinyNode.getName() + " " + controlName + " = new " + tinyNode.getName() + "(";
        List<String> parameters = new ArrayList<>();
        for (Map.Entry<String, String> entry : tinyNode.Attributes.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            parameters.add(value);
        }
        codeLine = codeLine + StringUtils.join(", ", parameters);
        codeLine = codeLine + ")";
        addCodeLine(codeLine);
    }
    
	private boolean isNodeProperty(String attrName) {
		return attrName.contains(".");
	}

    private void setupNodeProperty(String attrName, String value, String controlName) {
    	
    	Class<?> valueClass = String.class;
    	attrName = prepareAttrName(attrName);
    	Entry<String, Class<?>> specProp = findSpecProp(attrName);
    	if (specProp != null)
    		valueClass = specProp.getValue();
    	
    	value = prepareFunctionParam(value, valueClass);
    	
    	String codeLine = controlName + ".getProperties().put(" + quote(attrName) + ","+value +")";
    	addCodeLine(codeLine);
	}

	private String prepareAttrName(String attrName) {
		attrName = attrName.replace('.', '-').toLowerCase();
    	if( attrName.endsWith(propPostfixInt) )
    		attrName = attrName.substring( 0,attrName.length()-5 );
		return attrName;
	}

    

	private Entry<String,Class<?>> findSpecProp(String attrName){
   	for (Entry<String,Class<?>> prop : specPropClass.entrySet()){
    		if (attrName.endsWith(prop.getKey()))
    			return prop;
    	}
    	return null;
    }
}