package wb.web.app;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.xpath.XPathAPI;
import org.cyberneko.html.parsers.DOMParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import wb.model.GroupShape;
import wb.model.PrepareScript;
import wb.model.PrepareShape;
import wb.model.ShapeMeta;
import wb.model.ShapeSource;
import wb.model.SvgParser;
import wb.util.IoHelper;

@Controller
@RequestMapping( { "/shapedb" })
public class ShapeDbController {
	
	@Autowired
	private ShapeDb shapeDb;
	
	@RequestMapping(method = RequestMethod.GET, value={"", "/"})
	@ResponseBody
	public List<ShapeMeta> list(HttpServletRequest req) {
		return shapeDb.getTopShapeMeta();
	}
	
	@RequestMapping(method = RequestMethod.GET, value={"/extract"})
	@ResponseBody
	public List<ShapeMeta> extract(@RequestParam("u") String url) {
		
		List<ShapeMeta> shapes = new ArrayList<ShapeMeta>();
		
		Set<String> seen = new HashSet<String>();
		
		ShapeSource source = PrepareShape.getSource(url);
		if (source != null) {
			ShapeMeta meta = source.getShapeMetaByUrl(url);
			try {
				System.out.println("loading: " + meta.svgUrl);
				meta.shape = new SvgParser().parse(new InputSource(meta.svgUrl));
				shapes.add(meta);
			} catch (Exception e) {
				System.out.println("failed to parse svg: " + meta.svgUrl + ": " + e);
			}
		} else {
			
			try {
				URL theUrl = new URL(url);
				seen.add(theUrl.toString());
				
				HttpURLConnection con = (HttpURLConnection) theUrl.openConnection();
				
				if (con.getContentType() != null 
						&& con.getContentType().contains("svg")) {
					throw new RuntimeException("not supported yet!");
				} else {

					// HTML
					DOMParser parser = new DOMParser();
					parser.setFeature("http://xml.org/sax/features/namespaces", false);
					parser.parse(new InputSource(IoHelper.reader(con)));
					Document doc = parser.getDocument();

					// 1) look for embedded SVG
					NodeList svgNodes = XPathAPI.selectNodeList(doc, "//SVG");
					for (int i = 0; i < svgNodes.getLength(); i++) {
						Element svgNode = (Element) svgNodes.item(i);
						ShapeMeta meta = new ShapeMeta();
						System.out.println("loading embedded");
						meta.svgUrl = theUrl.toString() + "#1";
						meta.shape = new SvgParser().parse(svgNode);
						if (meta.shape != null) {
							shapes.add(meta);
						}
					}
					
					// 2) look for a link with SVG
					NodeList aNodes = XPathAPI.selectNodeList(doc, "//A[@href]");
					for (int i = 0; i < aNodes.getLength(); i++) {
						Element aNode = (Element) aNodes.item(i);
						String href = aNode.getAttribute("href");
						if (href != null && href.endsWith(".svg")) {
							ShapeMeta meta = new ShapeMeta();
							URL subUrl = new URL(theUrl, href);
							if (!seen.contains(subUrl.toString())) {
								System.out.println("loading: " + subUrl);
								meta.url = subUrl.toString();
								meta.svgUrl = subUrl.toString();
								meta.shape = new SvgParser().parse(new InputSource(subUrl.toString()));
								if (meta.shape != null) {
									shapes.add(meta);
								}
								seen.add(subUrl.toString());
							}
						}
					}
					
				}
				
			} catch (Exception e) {
				System.out.println("failed to parse doc: " + url + ": " + e);
			}
		}
		
		// thumbnails
		final File tempDir = new File(System.getProperty("java.io.tmpdir"));
		for (ShapeMeta meta : shapes) {
			GroupShape shape = PrepareShape.prepareShape(meta.shape);
			meta.shape = shape;
			File file;
			try {
				file = File.createTempFile("shape", ".png", tempDir);
				PrepareScript prepareScript = new PrepareScript();
				PrepareShape.saveShapeImage(prepareScript, shape, 
						file, 300, 300);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
			meta.thumbUrl = "/wb/service/shapedb/temp/" + file.getName();
		}
		
		return shapes;
	}
	
	@RequestMapping(method = RequestMethod.POST, value="/save")
	@ResponseBody
	public ShapeMeta save(@RequestBody ShapeMeta meta) {
		this.shapeDb.saveShape(meta);
		return meta;
	}
	
	@RequestMapping(method = RequestMethod.GET, value={"/temp/{path:.*}"})
	@ResponseBody
	public void temp(@PathVariable("path") String path,
			HttpServletResponse resp) throws IOException {
		
		if (path.endsWith(".png")) {
			resp.setContentType("image/png");
			ServletOutputStream out = resp.getOutputStream();
			final File tempDir = new File(System.getProperty("java.io.tmpdir"));
			InputStream in = new BufferedInputStream(new FileInputStream(
					new File(tempDir, path)));
			IoHelper.copy(in, out);
			in.close();
			return;
		}
		
		throw new RuntimeException("unknown temp file: " + path);
	}
	
}
