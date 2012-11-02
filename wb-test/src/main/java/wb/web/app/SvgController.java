package wb.web.app;

import java.io.IOException;
import java.io.StringReader;
import java.net.URL;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import wb.model.GroupShape;
import wb.model.PrepareShape;
import wb.model.ShapeMeta;
import wb.model.ShapeSource;
import wb.model.SvgParser;

@Controller
@RequestMapping( { "/svg" })
public class SvgController {
	
	@RequestMapping(method = {RequestMethod.GET, RequestMethod.POST}, value="/gen")
	@ResponseBody
	public ShapeMeta gen(
			@RequestParam(value="url", required=false) String url,
			@RequestParam(value="path", required=false) String path,
			@RequestParam(value="doc", required=false) String doc) 
			throws IOException, SAXException {
		
		ShapeMeta meta = null;
		
		if (url != null) {
			ShapeSource source = PrepareShape.getSource(url);
			if (source != null) {
				meta = source.getShapeMetaByUrl(url);
				try {
					System.out.println("loading: " + meta.svgUrl);
					meta.shape = new SvgParser().parse(new InputSource(meta.svgUrl));
				} catch (Exception e) {
					System.out.println("failed to parse svg: " + meta.svgUrl + ": " + e);
				}
			} else {
				URL theUrl = new URL(url);
				meta = new ShapeMeta();
				meta.source = theUrl.getHost();
				meta.id = theUrl.toString();
				meta.url = theUrl.toString();
				meta.title = theUrl.toString();
				meta.svgUrl = theUrl.toString();
				meta.shape = new SvgParser().parse(new InputSource(theUrl.toString()));
			}
		} else if (path != null) {
			meta = new ShapeMeta();
			meta.source = "svgpath";
			meta.id = "svgpath1";
			meta.title = "SVG Path";
			meta.shape = new SvgParser().parsePath(path);
		} else if (doc != null) {
			meta = new ShapeMeta();
			meta.source = "svgdoc";
			meta.id = "svgdoc1";
			meta.title = "SVG Doc";
			meta.shape = new SvgParser().parse(new StringReader(doc));
		}
		
		if (meta != null) {
			GroupShape top = PrepareShape.prepareShape(meta.shape);
			meta.shape = top;
		}
		
		return meta;
	}
	
}
