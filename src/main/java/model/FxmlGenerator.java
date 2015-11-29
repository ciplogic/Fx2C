package model;

import model.CodeGenerator;
import model.JavaTiny;
import model.XmlDocToJavaTiny;
import org.w3c.dom.Document;
import utils.OsUtils;
import utils.ReflectionResolver;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static java.lang.System.out;

public class FxmlGenerator {
    private Document _doc;
    List<String> imports = new ArrayList<>();
    String path;
    public FxmlGenerator(String fileName){
        _doc = OsUtils.readXmlPlain(fileName);
        File pathFile = new File(fileName);
        path = pathFile.getParent();
    }

    public void process(String className, String packageName) {
        XmlDocToJavaTiny xmlDataTranslator = new XmlDocToJavaTiny();
        JavaTiny tinyNode = xmlDataTranslator.buildNodeInfo(_doc, imports);
        ReflectionResolver resolver = new ReflectionResolver(imports);

        CodeGenerator codeGenerator = new CodeGenerator();

        setupCodeGenerator(codeGenerator, tinyNode, resolver, className, packageName);

        String generatedCode = codeGenerator.generateCode();
        out.println("Code: ");
        out.println(generatedCode);
        String fullFilePath = path + "\\" + codeGenerator.className + ".java";
        OsUtils.writeAllText(fullFilePath, generatedCode);
    }

    private void setupCodeGenerator(CodeGenerator codeGenerator, JavaTiny tinyNode ,
                                    ReflectionResolver resolver, String clzName, String packageName) {
        codeGenerator.packageName = packageName;
        codeGenerator.className = clzName;
        codeGenerator.StarImports.addAll(resolver.Imports);
        codeGenerator.StaticImports.addAll(resolver.FixedTypes.keySet());
        codeGenerator.ControllerType = tinyNode.extractAttribute("fx:controller");
        codeGenerator.ViewType = tinyNode.getName();
        tinyNode.extractAttribute("xmlns:fx");

        ControlFactory builder = new ControlFactory(codeGenerator.BuildControlsLines, tinyNode, resolver);
        builder.process();
    }
}
