package wb.web.app;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Insets;
import java.io.IOException;

import javax.swing.JLabel;

import org.apache.batik.dom.GenericDOMImplementation;
import org.apache.batik.svggen.SVGGeneratorContext;
import org.apache.batik.svggen.SVGGraphics2D;
import org.scilab.forge.jlatexmath.TeXConstants;
import org.scilab.forge.jlatexmath.TeXFormula;
import org.scilab.forge.jlatexmath.TeXIcon;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import wb.model.ShapeMeta;
import wb.model.SvgParser;

@Controller
@RequestMapping( { "/latex" })
public class LaTeXController {
	
	@RequestMapping(method = {RequestMethod.GET, RequestMethod.POST}, value="/gen")
	@ResponseBody
	public ShapeMeta gen(@RequestParam("formula") String formulaStr) 
			throws IOException, SAXException {
		
		TeXFormula formula = new TeXFormula(formulaStr);
		
		TeXIcon icon = formula.createTeXIcon(TeXConstants.STYLE_DISPLAY, 20);
		icon.setInsets(new Insets(20, 20, 20, 20));
		
		DOMImplementation domImpl = GenericDOMImplementation
				.getDOMImplementation();
		String svgNS = "http://www.w3.org/2000/svg";
		Document document = domImpl.createDocument(svgNS, "svg", null);
		SVGGeneratorContext ctx = SVGGeneratorContext.createDefault(document);

		SVGGraphics2D g2 = new SVGGraphics2D(ctx, true);
		g2.setSVGCanvasSize(new Dimension(icon.getIconWidth(), icon
				.getIconHeight()));
		g2.setColor(Color.white);
		g2.fillRect(0, 0, icon.getIconWidth(), icon.getIconHeight());

		JLabel jl = new JLabel();
		jl.setForeground(new Color(0, 0, 0));
		icon.paintIcon(jl, g2, 0, 0);
		
//		Writer out = new StringWriter();
//		g2.stream(out, false);
//		out.close();
//		
//		final String svg = out.toString();
//		System.out.println(svg);
		
		ShapeMeta meta = new ShapeMeta();
		SvgParser parser = new SvgParser();
		parser.setSkipFirstWhiteGroup(true);
		meta.shape = parser.parse(g2.getRoot());
		System.out.println("done");
		return meta;
	}
	
}
