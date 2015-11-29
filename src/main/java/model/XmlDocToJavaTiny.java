package model;

import infrastructure.Utf8String;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import java.util.List;

public class XmlDocToJavaTiny {
    public JavaTiny buildNodeInfo(Document document, List<String> importsList){
        Node firstChild = document.getFirstChild();
        processImports(firstChild, importsList);

        Element element= document.getDocumentElement();
        JavaTiny tinyNode = populateElement(element);
        return tinyNode;
    }

    JavaTiny populateElement(Element element){
        JavaTiny result = new JavaTiny(
                new Utf8String( element.getTagName()
        ));
        int attrLength = element.getAttributes().getLength();
        for (int i= 0; i<attrLength ;i++) {
            Node attrNode = element.getAttributes().item(i);
            result.Attributes.put(attrNode.getNodeName(),  attrNode.getNodeValue());
        }

        int childrenLengh = element.getChildNodes().getLength();
        for (int i = 0; i<childrenLengh; i++) {
            Node nodeUntyped = element.getChildNodes().item(i);
            if (!(nodeUntyped instanceof Element))
            continue;
            result.Children.add(populateElement((Element) nodeUntyped));
        }

        return result;
    }

    private void processImports(Node firstChild, List<String> importsList) {
        Node startNode = firstChild;
        while (startNode != null && startNode.getNodeName() == "import") {
            importsList.add(startNode.getNodeValue());
            startNode = startNode.getNextSibling();
        }
    }
}
