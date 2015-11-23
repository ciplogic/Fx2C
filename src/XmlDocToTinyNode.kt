import model.TinyNode
import org.w3c.dom.Document
import org.w3c.dom.Element
import org.w3c.dom.Node

class XmlDocToTinyNode {

    public fun buildNodeInfo(document: Document, importsList: MutableList<String>): TinyNode {
        var firstChild = document.firstChild
        processImports(firstChild, importsList)

        val element: Element = document.documentElement
        var tinyNode = populateElement(element)
        return tinyNode
    }

    fun populateElement(element: Element): TinyNode {
        val result = TinyNode(element.tagName)
        var attrLength = element.attributes.length
        for (i in 0..attrLength - 1) {
            val attrNode = element.attributes.item(i)
            result.attrs[attrNode.nodeName] = attrNode.nodeValue
        }

        val childrenLengh = element.childNodes.length
        for (i in 0..childrenLengh - 1) {
            val nodeUntyped = element.childNodes.item(i)
            if (!(nodeUntyped is Element))
                continue
            result.Children.add(populateElement(nodeUntyped))
        }

        return result
    }

    private fun processImports(firstChild: Node?, importsList: MutableList<String>) {
        var startNode = firstChild
        while (startNode != null && startNode.nodeName == "import") {
            importsList.add(startNode.nodeValue)
            startNode = startNode.nextSibling
        }
    }
}