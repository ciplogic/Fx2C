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
