import model.FinalCodeGenerator
import model.TinyNode
import org.w3c.dom.Document
import utils.ClassResolver
import utils.OsUtils
import java.io.File
import java.util.*

class FxmlProcessor(fileName: String) {
    private var _doc: Document
    val imports: MutableList<String> = ArrayList()
    val path: String

    init {
        _doc = OsUtils.readXmlPlain(fileName);
        val pathFile = File(fileName)
        path = pathFile.directory.path

    }

    fun process(className: String, packageName: String) {
        var xmlDataTranslator = XmlDocToTinyNode()
        var tinyNode = xmlDataTranslator.buildNodeInfo(_doc, imports)
        val resolver = ClassResolver(imports)

        val codeGenerator = FinalCodeGenerator()

        setupCodeGenerator(codeGenerator, tinyNode, resolver, className, packageName)

        val generatedCode = codeGenerator.generateCode()
        println("Code: ")
        println(generatedCode)
        val fullFilePath = path + "\\" + codeGenerator.className + ".java"
        OsUtils.writeAllText(fullFilePath, generatedCode)
    }

    private fun setupCodeGenerator(codeGenerator: FinalCodeGenerator, tinyNode: TinyNode,
                                   resolver: ClassResolver, clzName: String, packageName: String) {
        codeGenerator.packageName = packageName
        codeGenerator.className = clzName
        codeGenerator.StarImports.addAll(resolver.Imports)
        codeGenerator.StaticImports.addAll(resolver.FixedTypes.keys)
        codeGenerator.ControllerType = tinyNode.extractAttribute("fx:controller")
        codeGenerator.ViewType = tinyNode.Name
        tinyNode.extractAttribute("xmlns:fx");

        val builder = ControlBuilder(codeGenerator.BuildControlsLines, tinyNode, resolver)
        builder.process()
    }

}