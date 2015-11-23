package model

import utils.OsUtils
import java.util.*

class FinalCodeGenerator {
    public val StaticImports: MutableList<String> = ArrayList()
    public val StarImports: MutableList<String> = ArrayList()
    public var packageName: String = ""
    public var className: String = ""
    public var ControllerType: String = ""

    public var ViewType: String = ""

    public val BuildControlsLines: MutableList<String> = ArrayList();

    public fun generateCode(): String {
        val stringBuilder = StringBuilder()
        if (!OsUtils.isNullOrEmpty(packageName)) {
            stringBuilder.append("package ")
            stringBuilder.append(packageName)
            stringBuilder.appendln(";")
            stringBuilder.appendln()
        }
        for (import in StaticImports) {
            stringBuilder.append("import ")
            stringBuilder.append(import)
            stringBuilder.appendln(";")
        }
        stringBuilder.appendln()
        for (import in StarImports) {
            stringBuilder.append("import ")
            stringBuilder.append(import)
            stringBuilder.appendln(".*;")
        }

        stringBuilder.appendln()

        stringBuilder.append("public final class ");
        stringBuilder.append(className)
        stringBuilder.appendln(" {")

        if (!OsUtils.isNullOrEmpty(ControllerType)) {
            stringBuilder.append("        public ");
            stringBuilder.append(ControllerType);
            stringBuilder.appendln(" _controller;");
        }

        stringBuilder.append("        public ");
        stringBuilder.append(ViewType);
        stringBuilder.appendln(" _view;");


        stringBuilder.append("public ");
        stringBuilder.append(className)
        stringBuilder.appendln("() {")


        if (!OsUtils.isNullOrEmpty(ControllerType)) {

            stringBuilder.append("        _controller = new ");
            stringBuilder.append(ControllerType);
            stringBuilder.appendln("();");
        }

        for (line in this.BuildControlsLines) {
            stringBuilder.append("        ");
            stringBuilder.append(line);
            stringBuilder.appendln(";");
        }
        stringBuilder.appendln("}")


        stringBuilder.appendln("}")


        return stringBuilder.toString()
    }

}