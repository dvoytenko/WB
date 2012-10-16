package wb.openclipart;

import org.apache.xpath.XPathAPI;
import org.cyberneko.html.parsers.DOMParser;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

import wb.model.ShapeMeta;
import wb.model.ShapeSource;

public class OpenClipArtSource implements ShapeSource {
	
	public static void main(String[] args) throws Exception {
		
		final String link = "http://openclipart.org/detail/13886/car-outline-modified-by-molumen";
		
		ShapeMeta meta = new OpenClipArtSource().getShapeMetaByUrl(link);
		
		System.out.println("source: " + meta.source);
		System.out.println("id: " + meta.id);
		System.out.println("link: " + meta.url);
		System.out.println("title: " + meta.title);
		System.out.println("author: " + meta.author);
		System.out.println("tags: " + meta.tags);
		System.out.println("svg: " + meta.svgUrl);
		System.out.println("thumb: " + meta.thumbUrl);
	}

	@Override
	public ShapeMeta getShapeMetaByUrl(String url) {
		try {
			return load(url);
		} catch (Exception e) {
			throw new RuntimeException("failed to load OpenClipArt [" + 
					url + "]: " + e, e);
		}
	}

	private ShapeMeta load(String url) throws Exception {
		
		ShapeMeta meta = new ShapeMeta();
		meta.source = "openclipart.org";
		meta.id = url;
		meta.url = rel(url);
		
		DOMParser parser = new DOMParser();
		parser.setFeature("http://xml.org/sax/features/namespaces", false);
		parser.parse(meta.url);
		Document doc = parser.getDocument();

		meta.title = text(XPathAPI.selectSingleNode(doc.getElementById("view"), "H2"), true);
		
		meta.author = text(XPathAPI.selectSingleNode(doc.getElementById("viewauthor"), "A"), true);
		
		NodeList tagNodes = XPathAPI.selectNodeList(doc.getElementById("viewtags"), ".//A");
		for (int i = 0; i < tagNodes.getLength(); i++) {
			meta.tags.add(text(tagNodes.item(i), true));
		}
		
		meta.svgUrl = rel(text(XPathAPI.selectSingleNode(doc.getElementById("viewimg"), ".//A/@href"), true));
		meta.thumbUrl = rel(text(XPathAPI.selectSingleNode(doc.getElementById("viewimg"), ".//IMG/@src"), true));
		
		return meta;
	}

	private String rel(String path) {
		if (path == null || path.isEmpty()) {
			return null;
		}
		if (path.startsWith("http:") || path.startsWith("https:")) {
			return path;
		}
		if (!path.startsWith("/")) {
			path = "/" + path;
		}
		return "http://openclipart.org" + path;
	}

	private static String text(Node node, boolean trim) {
		String value = null;
		if (node == null) {
			System.out.println("no node!");
		} else if (node instanceof Text) {
			value = ((Text) node).getNodeValue();
		} else if (node instanceof Element) {
			Element elem = (Element) node;
			StringBuilder sb = new StringBuilder();
			Node n = elem.getFirstChild();
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

}
