package utils;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;



public class OsUtils {
    public static final String substringBeforeLast(String _this, String delimiter){
        int index = _this.lastIndexOf(delimiter);
        if (index == -1) return _this;
        return _this.substring(0, index);
    }
    public static String removePrefix(String _this, String prefix) {
        if (_this.startsWith(prefix)) {
            return _this.substring(prefix.length());
        }
        return _this;
    }
    public static String removeSuffix(String _this, String suffix) {
        if (_this.endsWith(suffix)) {
            return _this.substring(0, _this.length() - suffix.length());
        }
        return _this;
    }
    public static String[] GetDirectoryFiles(String path) {
        ArrayList<String> resultList = new ArrayList<>();
        try {
            Files.walk(Paths.get(path)).forEach(filePath -> {
                if (Files.isRegularFile(filePath)) {
                    String filePathName = filePath.toString();
                    resultList.add(filePathName);
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
        return GetStringsOfArray(resultList);
    }

    private static String[] GetStringsOfArray(List<String> resultList) {
        String[] result = new String[resultList.size()];

        for (int i = 0; i < resultList.size(); i++) {
            String item = resultList.get(i);
            result[i] = item;
        }
        return result;
    }

    public static String[] getFileLines(String fileName) {
        List<String> lines = new ArrayList<>();
        try {
            lines.addAll(Files.readAllLines(Paths.get("/tmp/test.csv"), Charset.forName("UTF-8")));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return GetStringsOfArray(lines);
    }

    public static boolean isNullOrEmpty(String str) {
        return str == null || str.length() == 0;
    }

    public static final String join(String separator, Collection<String> items) {
        StringBuilder sb = new StringBuilder();
        boolean isFirst = true;
        for (String item : items) {
            if (isFirst) {
                isFirst = false;
            } else {
                sb.append(", ");
            }
            sb.append(item);
        }

        return sb.toString();
    }

    public static final String indent(String text) {
        String startString = text.substring(0, 1).toUpperCase();
        String endString = text.substring(1, text.length());
        return startString + endString;
    }

    public static Document readXmlPlain(String fileName) {
        Document dom = null;
        // Make an  instance of the DocumentBuilderFactory
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

        try {
            // use the factory to take an instance of the document builder
            DocumentBuilder db = dbf.newDocumentBuilder();
            // parse using the builder to get the DOM mapping of the
            // XML file
            File file = new File(fileName);
            dom = db.parse(file);

        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return dom;
    }

    public static void writeAllText( String pathName, String generateCode) {
        try (PrintWriter out = new PrintWriter(pathName)) {
            out.print(generateCode);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

    }

    public static List<String> readAllLines( String file) {
        try {
            List<String> lines = Files.readAllLines(Paths.get(file));
            return lines;
        } catch (Exception ex) {
            return new ArrayList<>();
        }
    }
}
