package wb.util;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.Text;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class XmlHelper {

	public static DocumentBuilderFactory getDomFactory() {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setNamespaceAware(false);
		factory.setValidating(false);
		return factory;
	}

	public static DocumentBuilder newDomBuilder() {
		try {
			return getDomFactory().newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			throw new RuntimeException(e);
		}
	}

	public static Document parseString(String s) 
			throws SAXException, IOException {
		return parse(new InputSource(new StringReader(s)));
	}

	public static Document parse(InputSource inputSource) 
			throws SAXException, IOException {
		DocumentBuilder db = newDomBuilder();
		return db.parse(inputSource);
	}

	public static Document parse(File file) throws SAXException, IOException {
		InputStream in = new BufferedInputStream(new FileInputStream(file));
		try {
			return parse(new InputSource(in));
		} finally {
			in.close();
		}
	}

	public static Element element(Node parent, String name, boolean caseSensitive) {
		for (Node n = parent.getFirstChild(); n != null; n = n.getNextSibling()) {
			if (n.getNodeType() == Node.ELEMENT_NODE) {
				boolean eq;
				if (caseSensitive) {
					eq = n.getNodeName().equals(name);
				} else {
					eq = n.getNodeName().equalsIgnoreCase(name);
				}
				if (eq) {
					return (Element) n;
				}
			}
		}
		return null;
	}

	public static List<Element> elements(Node parent, String name, boolean caseSensitive) {
		List<Element> list = new ArrayList<Element>();
		for (Node n = parent.getFirstChild(); n != null; n = n.getNextSibling()) {
			if (n.getNodeType() == Node.ELEMENT_NODE) {
				boolean eq;
				if (caseSensitive) {
					eq = n.getNodeName().equals(name);
				} else {
					eq = n.getNodeName().equalsIgnoreCase(name);
				}
				if (eq) {
					list.add((Element) n);
				}
			}
		}
		return list;
	}

	public static List<Element> elements(Element parent) {
		List<Element> list = new ArrayList<Element>();
		for (Node n = parent.getFirstChild(); n != null; n = n.getNextSibling()) {
			if (n.getNodeType() == Node.ELEMENT_NODE) {
				list.add((Element) n);
			}
		}
		return list;
	}

	public static String text(Node node, boolean trim) {
		
		String value;
		
		if (node instanceof Text) {
			value = ((Text) node).getNodeValue();
		} else if (node instanceof Element) {
			StringBuilder sb = new StringBuilder();
			Node n = node.getFirstChild();
			while (n != null) {
				sb.append(text(n, false));
				n = n.getNextSibling();
			}
			value = sb.toString();
		} else {
			value = node.getNodeValue();
		}
		
		if (value != null && trim) {
			value = value.trim();
		}
		
		return value;
	}

	public static Document newDocument() {
		return newDomBuilder().newDocument();
	}

	public static String toXml(Node node) throws SAXException {
		StringWriter sw = new StringWriter();
		toXml(node, sw);
		return sw.toString();
	}

	public static void toXml(Node node, Writer writer) throws SAXException {
		try {
			Transformer transformer = TransformerFactory.newInstance().newTransformer();
			transformer.transform(new DOMSource(node), new StreamResult(writer));
		} catch (TransformerException e) {
			throw new SAXException("failed to serialize document");
		}		
	}

}
