package model;

import infrastructure.JavaTiny;
import javafx.util.Pair;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import utils.OsUtils;

import java.util.List;

public class XmlDocToJavaTiny {

    public Pair<JavaTiny, GeneratorConfiguration> buildNodeInfo(Document document, List<String> importsList) {
        Node firstChild = document.getFirstChild();
        processImports(firstChild, importsList);

        GeneratorConfiguration configuration = new GeneratorConfiguration();
        processFlags(firstChild, configuration);

        Element element = document.getDocumentElement();
        JavaTiny tinyNode = populateElement(element);
        Pair<JavaTiny, GeneratorConfiguration> result = new Pair<>(tinyNode, configuration);
        return result;
    }

    private void processFlags(Node firstChild, GeneratorConfiguration configuration) {
        Node startNode = firstChild;
        while (startNode != null) {
            if ("#comment".equals(startNode.getNodeName())) {
                String commentValue = startNode.getNodeValue().trim();
                configuration.isKotlinController = true;
            }

            startNode = startNode.getNextSibling();
        }
    }

    JavaTiny populateElement(Element element) {
        JavaTiny result = new JavaTiny(
                element.getTagName()
        );

        if (element.hasChildNodes()) {
            String text = element.getFirstChild().getTextContent();
            if (!OsUtils.isNullOrEmpty(text.trim())) {
                result.setInnerText(text);
            }
        }
        int attrLength = element.getAttributes().getLength();
        for (int i = 0; i < attrLength; i++) {
            Node attrNode = element.getAttributes().item(i);
            result.Attributes.put(attrNode.getNodeName(), attrNode.getNodeValue());
        }

        int childrenLengh = element.getChildNodes().getLength();
        for (int i = 0; i < childrenLengh; i++) {
            Node nodeUntyped = element.getChildNodes().item(i);
            if (!(nodeUntyped instanceof Element)) {
                continue;
            }
            result.Children.add(populateElement((Element) nodeUntyped));
        }

        return result;
    }

    private void processImports(Node firstChild, List<String> importsList) {
        Node startNode = firstChild;
        while (startNode != null && "import".equals(startNode.getNodeName())) {
            importsList.add(startNode.getNodeValue());
            startNode = startNode.getNextSibling();
        }
    }
}
