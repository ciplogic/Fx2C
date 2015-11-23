import utils.OsUtils
import java.io.File

fun main(args: Array<String>) {
    var path = "/Users/Ciprian/Dropbox/Work/DeskTools/src/"
    if (args.count() > 0) {
        path = args[0]
    }
    val files = OsUtils.GetDirectoryFiles(path);
    for (file in files) {


        if (file.endsWith(".fxml")) {
            println("To compile: " + file)

            compile(file)
        }
    }
}

fun getPackageName(path: String): String {
    val files = OsUtils.GetDirectoryFiles(path);
    for (file in files) {
        if (!file.endsWith(".java")) continue
        val lines: List<String> = OsUtils.readAllLines(file)
        for (line in lines) {
            val lineTrimmed = line.trim()
            if (!lineTrimmed.startsWith("package")) {
                continue
            }
            val packageLine = lineTrimmed.removePrefix("package").removeSuffix(";")
            return packageLine
        }
    }
    return ""
}

fun compile(file: String) {
    val processor = FxmlProcessor(file)
    val fileData = File(file)
    val packageName = getPackageName(fileData.directory.path)
    var className = fileData.nameWithoutExtension
    processor.process("Fx" + OsUtils.indent(className), packageName)
}

