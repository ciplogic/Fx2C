import model.TinyNode
import utils.ClassResolver
import utils.OsUtils
import java.util.*

class ControlBuilder(buildControlsLines: MutableList<String>, tinyNode: TinyNode, resolver: ClassResolver) {
    val controlLines = buildControlsLines
    val tinyNode = tinyNode
    val resolver = resolver


    fun process() {

        val firstControl = setupControl(tinyNode, resolver)
        addCodeLine("_view = $firstControl")
    }

    var controlIndex = 1

    private fun setupControl(tinyNode: TinyNode, resolver: ClassResolver): String {

        val controlName = newControlName()
        addCodeLine(tinyNode.Name + " " + controlName + " = new " + tinyNode.Name + "()")
        val controlClass = resolver.resolve(tinyNode.Name)
        setupId(tinyNode, controlName)
        setupAttributes(tinyNode, resolver, controlName, controlClass)

        if (tinyNode.hasChildren()) {

            buildChildrenControls(resolver, tinyNode, controlName, controlClass)
        }

        return controlName

    }

    private fun buildChildrenControls(resolver: ClassResolver, tinyNode: TinyNode, parentControl: String, parentClass: Class<*>) {
        for (child in tinyNode.Children) {
            val controlChildNodes = child.Children
            val childControlNames: MutableList<String> = ArrayList()
            for (childControlNode in controlChildNodes) {
                val childControlName = setupControl(childControlNode, resolver)
                childControlNames.add(childControlName)
            }
            val containerMethod = child.Name;
            val method = resolver.resolveClassProperty(parentClass, containerMethod, false);
            if (method == null) {
                println("Method $containerMethod not found")
                continue
            }
            var codeLine = "$parentControl.${method.name}().addAll(${OsUtils.join(", ", childControlNames)})"


            addCodeLine(codeLine)
        }


    }

    private fun newControlName(): String {
        val result = "ctrl_" + controlIndex
        controlIndex++
        return result
    }

    private fun setupId(tinyNode: TinyNode, controlName: String) {
        var id = tinyNode.extractAttribute("fx:id");
        var idDirect = tinyNode.extractAttribute("id");
        if (OsUtils.isNullOrEmpty(id)) {
            id = idDirect
        }
        if (!OsUtils.isNullOrEmpty(id)) {
            addCodeLine("_controller.$id = $controlName")
        }
    }

    fun setupAttributes(controlNode: TinyNode, resolver: ClassResolver, controlName: String, controlClass: Class<*>) {

        var attrs = controlNode.attrs
        for (attr in attrs) {
            val attrName = attr.key
            val resolvedMethod = resolver.resolveClassProperty(controlClass, attrName, true)

            if (resolvedMethod == null) {
                println("cannot find method + " + attrName)
                continue
            }
            var parameterType = resolvedMethod.parameterTypes[0]
            val codeLine = buildFunctionCode(attr.value, controlName, resolvedMethod.name, parameterType.typeName)
            addCodeLine(codeLine)
        }
    }

    private fun buildFunctionCode(attributeValue: String, controlName: String, methodName: String?, parameterTypeName: String): String {
        var parameterValue: String
        val typeCodeParameter = TypeCodes.TypeNameToTypeCode(parameterTypeName)
        when (typeCodeParameter) {
            TypeCodes.String -> {
                parameterValue = "\"$attributeValue\""
            }
            TypeCodes.Object -> {
                if (parameterTypeName == "javafx.event.EventHandler") {
                    parameterValue = "_controller::${attributeValue.removeRange(0, 1)}"
                } else {
                    parameterValue = attributeValue
                }
            }
            else -> {
                parameterValue = attributeValue
            }
        }

        val codeLine = "    $controlName.$methodName($parameterValue)"
        return codeLine
    }

    private fun addCodeLine(codeLine: String) {
        controlLines.add(codeLine);
    }

}
