package model.reader;

import infrastructure.JavaTiny;
import javafx.fxml.Initializable;
import javafx.util.Pair;
import model.ControlFactory;
import model.GeneratorConfiguration;
import model.XmlDocToJavaTiny;
import model.generator.CodeGenerator;
import org.w3c.dom.Document;
import utils.OsUtils;
import utils.ReflectionResolver;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class FxmlGenerator {

    List<String> imports = new ArrayList<>();
    String path;
    private Document _doc;

    public FxmlGenerator(String fileName) {
        _doc = OsUtils.readXmlPlain(fileName);
        File pathFile = new File(fileName);
        path = pathFile.getParent();
        if (path == null) {
            path = ".";
        }
    }

    public void process(String className, String packageName) {
        XmlDocToJavaTiny xmlDataTranslator = new XmlDocToJavaTiny();

        Pair<JavaTiny, GeneratorConfiguration> result = xmlDataTranslator.buildNodeInfo(_doc, imports);

        JavaTiny tinyNode = result.getKey();
        GeneratorConfiguration configuration = result.getValue();

        ReflectionResolver resolver = new ReflectionResolver(imports);

        CodeGenerator codeGenerator = new CodeGenerator();

        setupCodeGenerator(codeGenerator, tinyNode, resolver, className, packageName, configuration);

        String generatedCode = codeGenerator.generateCode();

        String fullFilePath = path + File.separator + codeGenerator.className + ".java";
        OsUtils.writeAllText(fullFilePath, generatedCode);
    }

    private void setupCodeGenerator(CodeGenerator codeGenerator, JavaTiny tinyNode,
            ReflectionResolver resolver, String clzName, String packageName, GeneratorConfiguration configuration) {
        codeGenerator.packageName = packageName;
        codeGenerator.className = clzName;
        codeGenerator.StarImports.addAll(resolver.Imports);
        codeGenerator.StaticImports.addAll(resolver.FixedTypes.keySet());
        codeGenerator.ControllerType = tinyNode.extractAttribute("fx:controller").toString();
        codeGenerator.ViewType = tinyNode.getName();
        tinyNode.extractAttribute("xmlns:fx");

        ControlFactory builder = new ControlFactory(codeGenerator.BuildControlsLines, tinyNode, resolver, configuration);
        builder.process();
        setupInitializableControllerCode(codeGenerator, resolver);
    }

    private void setupInitializableControllerCode(CodeGenerator codeGenerator, ReflectionResolver resolver) {
        if (OsUtils.isNullOrEmpty(codeGenerator.ControllerType)) {
            return;
        }
        Class<?> controllerClass = resolver.resolve(codeGenerator.ControllerType);
        if (controllerClass == null || !Initializable.class.isAssignableFrom(controllerClass)) {
            return;
        }
        codeGenerator.BuildControlsLines.add("_controller.initialize(null, null);");
    }
}
