import javafx.application.Application;
import javafx.stage.Stage;
import model.reader.FxmlGenerator;
import utils.OsUtils;
import utils.StringUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static java.lang.System.out;

public class MainApplication extends Application {
    static String[] appArgs;

    public static void main(String[] args) {

        appArgs = args;

        Application.launch(args);

    }

    void run() {
        String[] args = appArgs;
        String path = "C:\\Oss\\DeskTools\\src\\main\\java\\FxRoot";
        if (args.length > 0) {
            path = args[0];
        }


        boolean generatePreloader = true;
        String[] files = OsUtils.GetDirectoryFiles(path, true,
                file -> file.getName().endsWith(".fxml"));
        List<String> MappedTypesToCreate = new ArrayList<>();
        for (String file : files) {
            if (file.endsWith(".fxml")) {
                out.println("To compile: " + file);

                compile(file, MappedTypesToCreate);
            }
        }
        if (generatePreloader) {
            computePreloader(path, MappedTypesToCreate);
        }
        System.exit(0);
    }

    private void computePreloader(String path, List<String> mappedTypesToCreate) {
        String fullFilePath = path + "Fx2CPreloader.java";
        StringBuilder stringBuilder = new StringBuilder();

        String packageName = getPackageName(path);
        if (!OsUtils.isNullOrEmpty(packageName)) {
            stringBuilder.append("package ");
            stringBuilder.append(packageName);
            stringBuilder.append(";");
            stringBuilder.append("\n\r");
        }
        stringBuilder.append("public class Fx2CPreloader {");
        stringBuilder.append("\n\r");
        stringBuilder.append(" public static void preload() {");
        stringBuilder.append("\n\r");
        for (String clazz : mappedTypesToCreate) {
            stringBuilder.append(" new ").append(clazz).append("();").append("\n\r");
        }

        stringBuilder.append("}");
        stringBuilder.append("}");
        stringBuilder.append("\n\r");

        String generatedCode = stringBuilder.toString();
        OsUtils.writeAllText(fullFilePath, generatedCode);


    }

    void compile(String file, List<String> mappedTypesToCreate) {
        FxmlGenerator processor = new FxmlGenerator(file);
        File fileData = new File(file);
        String packageName = getPackageName(fileData.getParent());
        String className = StringUtils.substringBeforeLast(fileData.getName(), ".");

        String fxClassName = "Fx" + StringUtils.indent(className);
        processor.process(fxClassName, packageName);
        if (OsUtils.isNullOrEmpty(packageName)) {
            mappedTypesToCreate.add(fxClassName.trim());
        } else {
            mappedTypesToCreate.add(packageName + "." + fxClassName.trim());
        }
    }

    String getPackageName(String path) {
        String[] files = OsUtils.GetDirectoryFiles(path, false, file ->
                (file.getName().endsWith(".java")
                        || file.getName().endsWith(".kt"))
                        && (!file.getName().startsWith("Fx"))
        );

        for (String file : files) {
            List<String> lines = OsUtils.readAllLines(file);
            for (String line : lines) {
                String lineTrimmed = line.trim();
                if (!lineTrimmed.startsWith("package")) {
                    continue;
                }
                String packageLine =
                        StringUtils.removeSuffix(
                                StringUtils.removePrefix(lineTrimmed, "package"),
                                ";");
                return packageLine;
            }
        }
        return "";
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        run();
    }
}
