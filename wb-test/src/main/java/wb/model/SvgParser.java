package wb.model;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.StringTokenizer;

import org.json.JSONObject;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import wb.util.XmlHelper;

public class SvgParser {
	
	public static void main(String[] args) throws Exception {
		
		final String[] files = {
				"karderio_computer_screen.svg",
				"fortran_minimalist_monitor_and_computer.svg",
				"pc.svg",
				"server.svg",
				"lmproulx_Iphone.svg",
				"BenBois_iPhone_SVG.svg",
				"hand_phone.svg",
				};
		for (String fileName : files) {
			System.out.println(fileName);
			
			File file = new File("src/main/webapp/shapes/" + fileName);
			
			Shape shape = new SvgParser().parse(file);
			
			JSONObject js = (JSONObject) new Serializer().toJson(shape);
			System.out.println(js.toString(2));

			File targetFile = new File("src/main/webapp/shapes/" + file.getName().replace(".svg", ".json"));
			Writer writer = new OutputStreamWriter(new FileOutputStream(targetFile), "UTF-8");
			writer.write(js.toString(2));
			writer.close();
		}

		/*
		File file = new File("C:/Work/WB/fonts/2/Znikomit2.svg");
		
		InputSource source = new InputSource(new FileInputStream(file));
		
		SAXReader reader = new SAXReader();
		Document doc = reader.read(source);
		
		Font font = new SvgParser().parseFont(doc.getRootElement().
				element("defs").element("font"));
		
		JSONObject js = (JSONObject) new Serializer().toJson(font);
		System.out.println(js.toString(2));
		
		File targetFile = new File("src/main/webapp/fonts/" + file.getName().replace(".svg", ".json"));
		Writer writer = new OutputStreamWriter(new FileOutputStream(targetFile), "UTF-8");
		writer.write(js.toString(2));
		writer.close();
		*/
	}

	private boolean skipFirstWhiteGroup;
	
	private int groupCount = 0;
	
	public void setSkipFirstWhiteGroup(boolean skipFirstWhiteGroup) {
		this.skipFirstWhiteGroup = skipFirstWhiteGroup;
	}

	public Shape parse(File file) throws IOException, SAXException {
		return parse(XmlHelper.parse(file).getDocumentElement());
	}
	
	public Shape parse(InputStream stream) throws IOException, SAXException {
		return parse(new InputSource(stream));
	}
	
	public Shape parse(Reader reader) throws IOException, SAXException {
		return parse(new InputSource(reader));
	}
	
	public Shape parse(InputSource source) throws SAXException, IOException {
		return parse(XmlHelper.parse(source).getDocumentElement());
	}

	public Shape parse(Element root) {
		
		if (root.getTagName().equalsIgnoreCase("svg")) {
			return parseSvg(root);
		}
		if (root.getTagName().equalsIgnoreCase("g")) {
			// <g fill="white" stroke="white">
			if (groupCount == 0 
					&& skipFirstWhiteGroup
					&& "white".equals(root.getAttribute("fill"))
					&& "white".equals(root.getAttribute("stroke"))) {
				groupCount++;
				return null;
			}
			return parseGroup(root);
		}
		if (root.getTagName().equalsIgnoreCase("path")
				&& !noStroke(root)) {
			return parsePath(root);
		}
		if (root.getTagName().equalsIgnoreCase("polyline")
				&& !noStroke(root)) {
			return parsePolyline(root);
		}
		if (root.getTagName().equalsIgnoreCase("polygon")
				&& !noStroke(root)) {
			return parsePolygon(root);
		}
		if (root.getTagName().equalsIgnoreCase("rect")
				&& !noStroke(root)) {
			return parseRect(root);
		}
//		if (root.getName().equalsIgnoreCase("xxx") 
//				&& !noStroke(root)) {
//			return parseXXX(root);
//		}
		
		return null;
	}

	private boolean noStroke(Element root) {
		// return "none".equalsIgnoreCase(root.attributeValue("stroke"));
		return false;
	}

	public Font parseFont(Element root) {
		Font font = new Font();
		
		String horizAdvX = root.getAttribute("horiz-adv-x");
		if (horizAdvX != null && !horizAdvX.isEmpty()) {
			font.baseAdvX = Double.parseDouble(horizAdvX);
		}
		
		Element fontFaceElem = XmlHelper.element(root, "font-face", false);
		if (fontFaceElem != null) {
			String unitsPerEm = fontFaceElem.getAttribute("units-per-em");
			if (unitsPerEm != null) {
				font.baseHeight = Double.parseDouble(unitsPerEm);
			}
		}
		
		Element missingGlyphElem = XmlHelper.element(root, "missing-glyph", false);
		if (missingGlyphElem != null) {
			font.missingGlyph = parseGlyph(missingGlyphElem);
		}
		
		List<Element> glyphElems = XmlHelper.elements(root, "glyph", false);
		for (Element glyphElem : glyphElems) {
			String unicode = glyphElem.getAttribute("unicode");
			if (unicode != null && unicode.length() == 1) {
				font.glyphMap.put(unicode, parseGlyph(glyphElem));
			}
		}
		
		return font;
	}
	
	public Glyph parseGlyph(Element elem) {
		
		Glyph glyph = new Glyph();
		
		String horizAdvX = elem.getAttribute("horiz-adv-x");
		if (horizAdvX != null && !horizAdvX.isEmpty()) {
			glyph.advX = Double.parseDouble(horizAdvX);
		}
		
		String d = elem.getAttribute("d");
		if (d != null) {
			List<PathAction> actions = parsePathActions(d);
			PathConsumer consumer = new PathConsumer();
			for (PathAction a : actions) {
				a.interpret(consumer);
			}
			glyph.pathSegment = new PathSegment(consumer.segments);
		}
		
		return glyph;
	}

	private void parseChildren(Element parentElem, GroupShape parentGroup) {
		List<Element> children = XmlHelper.elements(parentElem);
		for (Element child : children) {
			Shape shape = parse(child);
			if (shape != null) {
				parentGroup.shapes.add(shape);
			}
		}
	}

	private Shape parseSvg(Element root) {
		GroupShape top = new GroupShape();
		top.transform = parseTransform(root);
		parseChildren(root, top);
		return top;
	}

	private Shape parseGroup(Element root) {
		GroupShape top = new GroupShape();
		top.transform = parseTransform(root);
		parseChildren(root, top);
		return top;
	}

	/**
	 * http://www.w3.org/TR/SVG/shapes.html#PolylineElement
	 */
	private Shape parsePolyline(Element root) {
		
		String pointsAttr = root.getAttribute("points");
		if (pointsAttr == null || pointsAttr.isEmpty()) {
			return null;
		}
		
		// points
		PolylineShape shape = new PolylineShape();
		/*
		 * The points that make up the polyline. 
		 * All coordinate values are in the user coordinate system
		 */
		shape.points.addAll(parseListOfPoints(pointsAttr));
		
		Shape top;
		Transform tr = parseTransform(root);
		if (tr != null) {
			GroupShape group = new GroupShape();
			group.transform = tr;
			group.shapes.add(shape);
			top = group;
		} else {
			top = shape;
		}
		return top;
	}

	/**
	 * http://www.w3.org/TR/SVG/shapes.html#PolygonElement
	 */
	private Shape parsePolygon(Element root) {
		
		String pointsAttr = root.getAttribute("points");
		if (pointsAttr == null || pointsAttr.isEmpty()) {
			return null;
		}
		
		// points
		PolygonShape shape = new PolygonShape();
		/*
		 * The points that make up the polyline. 
		 * All coordinate values are in the user coordinate system
		 */
		shape.points.addAll(parseListOfPoints(pointsAttr));
		
		Shape top;
		Transform tr = parseTransform(root);
		if (tr != null) {
			GroupShape group = new GroupShape();
			group.transform = tr;
			group.shapes.add(shape);
			top = group;
		} else {
			top = shape;
		}
		return top;
	}

	/**
	 * http://www.w3.org/TR/SVG/shapes.html#RectElement
	 */
	private Shape parseRect(Element root) {
		
		/*
			‘width’
			‘height’
			‘rx’
			‘ry’
		 */
		
		/* x:
		 * The x-axis coordinate of the side of the rectangle which has the smaller x-axis 
		 * coordinate value in the current user coordinate system. If the attribute is not 
		 * specified, the effect is as if a value of "0" were specified. 
		 */
		double x;
		try {
			x = Double.parseDouble(root.getAttribute("x").trim());
		} catch (Exception e) {
			x = 0.0;
		}
		
		/* y:
		 * The y-axis coordinate of the side of the rectangle which has the smaller y-axis 
		 * coordinate value in the current user coordinate system. If the attribute is not 
		 * specified, the effect is as if a value of "0" were specified.
		 */
		double y;
		try {
			y = Double.parseDouble(root.getAttribute("y").trim());
		} catch (Exception e) {
			y = 0.0;
		}
		
		/* width:
		 * The width of the rectangle.
		 * A negative value is an error (see Error processing). 
		 * A value of zero disables rendering of the element.
		 */
		double width = Double.parseDouble(root.getAttribute("width").trim());
		
		/* height:
		 * The height of the rectangle.
		 * A negative value is an error (see Error processing). 
		 * A value of zero disables rendering of the element.
		 */
		double height = Double.parseDouble(root.getAttribute("height").trim());
		
		/* rx, ry
		 * ignore for now
		 */
		
		RectangleShape shape = new RectangleShape();
		shape.topleft = new Point(x, y);
		shape.width = width;
		shape.height = height;
		
		Shape top;
		Transform tr = parseTransform(root);
		if (tr != null) {
			GroupShape group = new GroupShape();
			group.transform = tr;
			group.shapes.add(shape);
			top = group;
		} else {
			top = shape;
		}
		return top;
	}

	private Shape parsePath(Element root) {
		
		String d = root.getAttribute("d");
		if (d == null || d.isEmpty()) {
			return null;
		}
		
		PathShape pathShape = new PathShape();
		// TODO: style="stroke-linejoin:round"
		
		List<PathAction> actions = parsePathActions(d);
		
		PathConsumer consumer = new PathConsumer();
		
		for (PathAction a : actions) {
			try {
				a.interpret(consumer);
			} catch (Exception e) {
				throw new RuntimeException("failed to interpret [" + d + "]" +
						", action [" + a.code + ": " + Arrays.toString(a.values) + "]" +
						": " + e, e);
			}
		}
		
		pathShape.pathSegment.segments.addAll(consumer.segments);

		Shape top;
		Transform tr = parseTransform(root);
		if (tr != null) {
			GroupShape group = new GroupShape();
			group.transform = tr;
			group.shapes.add(pathShape);
			top = group;
		} else {
			top = pathShape;
		}
		return top;
	}

	public PathShape parsePath(String d) {
		if (d == null || d.isEmpty()) {
			return null;
		}
		
		PathShape pathShape = new PathShape();
		
		List<PathAction> actions = parsePathActions(d);
		
		PathConsumer consumer = new PathConsumer();
		
		for (PathAction a : actions) {
			try {
				a.interpret(consumer);
			} catch (Exception e) {
				throw new RuntimeException("failed to interpret [" + d + "]" +
						", action [" + a.code + ": " + Arrays.toString(a.values) + "]" +
						": " + e, e);
			}
		}
		
		pathShape.pathSegment.segments.addAll(consumer.segments);
		
		return pathShape;
	}
	
	private Transform parseTransform(Element elem) {
		return parseTransform(elem.getAttribute("transform"));
	}

	public Transform parseTransform(String s) {
		
		if (s == null || s.isEmpty()) {
			return null;
		}

		List<TransformAction> actions = new ArrayList<TransformAction>();
		
		String delims = "() ,\t\r\n\f";
		StringTokenizer tokenizer = new StringTokenizer(s, delims, true);
		final int NONE = 0;
		final int START_FUNC = 1;
		final int ARGS = 2;
		int state = NONE;
		String funcName = null;
		List<Double> values = new ArrayList<Double>();
		while (tokenizer.hasMoreTokens()) {
			String tok = tokenizer.nextToken().trim();
			switch (state) {
			case NONE:
				if (tok.equals("(") || tok.equals(")") || tok.equals(",")) {
					throw new IllegalArgumentException("invalid form of transform in [" + s + "]");
				} else if (!tok.isEmpty() && !delims.contains(tok)) {
					funcName = tok;
					state = START_FUNC;
				}
				break;
			case START_FUNC:
				if (tok.equals("(")) {
					state = ARGS;
				} else if (tok.equals(")") || tok.equals(",")
						|| (!tok.isEmpty() && !delims.contains(tok))) {
					throw new IllegalArgumentException("invalid form of transform in [" + s + "]");
				}
				break;
			case ARGS:
				if (tok.equals(")")) {
					actions.add(new TransformAction(funcName, values));
					values.clear();
					state = NONE;
				} else if (tok.equals("(")) {
					throw new IllegalArgumentException("invalid form of transform in [" + s + "]");
				} else if (!tok.isEmpty() && !delims.contains(tok)) {
					try {
						values.add(Double.parseDouble(tok));
					} catch (Exception e) {
						throw new IllegalArgumentException("invalid form of transform in [" + s + "]" +
								": invalid number [" + tok + "]");
					}
				}
				break;
			}
		}

		if (actions.isEmpty()) {
			return null;
		}
		
		Transform tr = new Transform();
		for (TransformAction action : actions) {
			action.apply(tr);
		}
		return tr;
	}

	/**
	 * http://www.w3.org/TR/SVG/shapes.html#PointsBNF
	 */
	private List<Point> parseListOfPoints(String ps) {

		// read all numbers
		int beg = 0;
		List<Double> values = new ArrayList<Double>();
		for (int i = 0; i <= ps.length(); i++) {
			char c = i < ps.length() ? ps.charAt(i) : 0;
			
			// end of number?
			if ((Character.isLetter(c) && Character.toLowerCase(c) != 'e')
					|| Character.isWhitespace(c)
					|| c == ',' || c == ';' || c == 0
					|| (i > beg && c == '-' && 
						Character.toLowerCase(ps.charAt(i-1)) != 'e')) {
				if (i > beg) {
					String s = ps.substring(beg, i).trim();
					if (s.length() > 0) {
						values.add(Double.parseDouble(s));
					}
				}
				if (c != '-') {
					beg = i + 1;
				} else {
					beg = i;
				}
			}
		}
		
		/*
		 * If an odd number of coordinates is provided, then the element 
		 * is in error, with the same user agent behavior as occurs with 
		 * an incorrectly specified ‘path’ element.
		 */
		if (values.size() % 2 == 1) {
			throw new IllegalArgumentException("odd number of coordinates!");
		}
		
		List<Point> list = new ArrayList<Point>();
		for (int i = 0; i < values.size(); i += 2) {
			list.add(new Point(values.get(i), values.get(i + 1)));
		}
		return list;
	}

	private List<PathAction> parsePathActions(String d) {
		List<PathAction> actions = new ArrayList<PathAction>();
		int beg = 0;
		char action = 0;
		List<Double> values = new ArrayList<Double>();
		for (int i = 0; i <= d.length(); i++) {
			char c = i < d.length() ? d.charAt(i) : 0;
			
			// end of number?
			if ((Character.isLetter(c) && Character.toLowerCase(c) != 'e')
					|| Character.isWhitespace(c)
					|| c == ',' || c == ';' || c == 0
					|| (i > beg && c == '-' && 
						Character.toLowerCase(d.charAt(i-1)) != 'e')) {
				if (i > beg) {
					String s = d.substring(beg, i).trim();
					if (s.length() > 0) {
						values.add(Double.parseDouble(s));
					}
				}
				if (c != '-') {
					beg = i + 1;
				} else {
					beg = i;
				}
			}
			
			// end of segment/start of a new one
			if ((Character.isLetter(c) && Character.toLowerCase(c) != 'e')
					|| c == 0) {
				
				// end segment
				if (action != 0) {
					actions.add(new PathAction(action, values));
				}
				
				// start new segment
				action = c;
				values.clear();
			}
		}
		return actions;
	}

	private static class TransformAction {
		
		String name;
		
		double[] values;
		
		public TransformAction(String name, List<Double> values) {
			this.name = name;
			this.values = new double[values.size()];
			for (int i = 0; i < values.size(); i++) {
				this.values[i] = values.get(i);
			}
		}
		
		/**
		 * http://www.w3.org/TR/SVG/coords.html#TransformAttribute
		 */
		public void apply(Transform tr) {
			
			if ("matrix".equalsIgnoreCase(this.name)) {
				// matrix(<a> <b> <c> <d> <e> <f>)
				tr.set(values[0], values[1], values[2], 
						values[3], values[4], values[5]);
				return;
			}

			if ("translate".equalsIgnoreCase(this.name)) {
				// translate(<tx> [<ty>])
				// If <ty> is not provided, it is assumed to be zero
				if (values.length < 2) {
					tr.translate(values[0], 0);
				} else {
					tr.translate(values[0], values[1]);
				}
				return;
			}

			if ("scale".equalsIgnoreCase(this.name)) {
				// scale(<sx> [<sy>])
				if (values.length < 2) {
					tr.scale(values[0], values[0]);
				} else {
					tr.scale(values[0], values[1]);
				}
				return;
			}

			if ("rotate".equalsIgnoreCase(this.name)) {
				// rotate(<rotate-angle> [<cx> <cy>])
				if (values.length < 2) {
					tr.rotate(Geom.rad(values[0]));
				} else {
					tr.rotate(Geom.rad(values[0]), values[1], values[2]);
				}
				return;
			}

			if ("skewX".equalsIgnoreCase(this.name)) {
				// skewX(<skew-angle>)
				tr.skew(Geom.rad(values[0]), 0);
				return;
			}

			if ("skewY".equalsIgnoreCase(this.name)) {
				// skewY(<skew-angle>)
				tr.skew(0, Geom.rad(values[0]));
				return;
			}
			
			System.out.println("unsupported transform '" + this.name + "'");
		}
		
	}

	private static class PathAction {
		
		char code;
		
		double[] values;

		public PathAction(char code, List<Double> values) {
			this.code = code;
			this.values = new double[values.size()];
			for (int i = 0; i < values.size(); i++) {
				this.values[i] = values.get(i);
			}
		}

		/**
		 * http://www.w3.org/TR/SVG/paths.html#PathData
		 */
		public void interpret(PathConsumer consumer) {
			switch (this.code) {
			case 'M':
			case 'm':
				move(consumer);
				break;

			case 'Z':
			case 'z':
				closepath(consumer);
				break;
				
			case 'L':
			case 'l':
				lineto(consumer);
				break;
				
			case 'H':
			case 'h':
				horiz(consumer);
				break;

			case 'V':
			case 'v':
				vert(consumer);
				break;

			case 'C':
			case 'c':
				cubic(consumer);
				break;

			case 'S':
			case 's':
				cubicShort(consumer);
				break;

			case 'Q':
			case 'q':
				quad(consumer);
				break;

			case 'T':
			case 't':
				quadShort(consumer);
				break;

			case 'A':
			case 'a':
				arc(consumer);
				break;
				
			default:
				System.out.println("unsupported path segment '" + this.code + "'");
			}
		}

		/**
		 * http://www.w3.org/TR/SVG/paths.html#PathDataMovetoCommands
		 */
		private void move(PathConsumer consumer) {
			// Params: (x y)+
			
			final boolean abs = Character.isUpperCase(code);
			
			/*
			 * M (uppercase) indicates that absolute coordinates will follow;
			 * m (lowercase) indicates that relative coordinates will follow.
			 */
			Point point = consumer.resolvePoint(abs, values[0], values[1]);
			
			/*
			 * Start a new sub-path at the given (x,y) coordinate.
			 */
			consumer.addSegment(new MoveToSegment(point));
			consumer.startNewSubpath(point);
			
			/*
			 * If a moveto is followed by multiple pairs of coordinates, the 
			 * subsequent pairs are treated as implicit lineto commands.
			 * Hence, implicit lineto commands will be relative if the moveto 
			 * is relative, and absolute if the moveto is absolute.
			 */
			if (values.length > 2) {
				for (int i = 2; i < values.length; i += 2) {
					Point nextPoint = consumer.resolvePoint(abs, values[i], 
							values[i + 1]);
					consumer.addSegment(new LineToSegment(nextPoint));
					consumer.setCurrentPoint(nextPoint);
				}
			}
		}
		
		private void closepath(PathConsumer consumer) {
			/*
			 * If a "closepath" is followed immediately by a "moveto", 
			 * then the "moveto" identifies the start point of the next 
			 * subpath. If a "closepath" is followed immediately by any 
			 * other command, then the next subpath starts at the same 
			 * initial point as the current subpath.
			 * ==>
			 * This all means that closepath doesn't change the path 
			 * start-point.
			 */
			consumer.addSegment(new ClosePathSegment());
			consumer.closePath();
		}

		private void lineto(PathConsumer consumer) {
			// Params: (x y)+
			
			/*
			 * L (uppercase) indicates that absolute coordinates will follow; 
			 * l (lowercase) indicates that relative coordinates will follow.
			 */
			final boolean abs = Character.isUpperCase(code);
			
			/*
			 * Draw a line from the current point to the given (x,y) 
			 * coordinate which becomes the new current point.
			 * 
			 * A number of coordinates pairs may be specified to draw a 
			 * polyline. At the end of the command, the new current point is 
			 * set to the final set of coordinates provided.
			 */
			for (int i = 0; i < values.length; i += 2) {
				Point nextPoint = consumer.resolvePoint(abs, values[i], 
						values[i + 1]);
				consumer.addSegment(new LineToSegment(nextPoint));
				consumer.setCurrentPoint(nextPoint);
			}
		}

		private void horiz(PathConsumer consumer) {
			// Params: x+
			
			/*
			 * H (uppercase) indicates that absolute coordinates will follow; 
			 * h (lowercase) indicates that relative coordinates will follow.
			 */
			final boolean abs = Character.isUpperCase(code);
			
			/*
			 * Draws a horizontal line from the current point (cpx, cpy) to (x, cpy).
			 * At the end of the command, the new current point becomes (x, cpy) for 
			 * the final value of x.
			 */
			for (int i = 0; i < values.length; i++) {
				Point nextPoint = new Point(consumer.resolvePoint(abs, values[i], 0).x, 
						consumer.getCurrentPoint().y);
				consumer.addSegment(new LineToSegment(nextPoint));
				consumer.setCurrentPoint(nextPoint);
			}
		}
		
		private void vert(PathConsumer consumer) {
			// Params: y+
			
			/*
			 * V (uppercase) indicates that absolute coordinates will follow; 
			 * v (lowercase) indicates that relative coordinates will follow.
			 */
			final boolean abs = Character.isUpperCase(code);
			
			/*
			 * Draws a vertical line from the current point (cpx, cpy) to (cpx, y).
			 * At the end of the command, the new current point becomes (cpx, y) 
			 * for the final value of y.
			 */
			for (int i = 0; i < values.length; i++) {
				Point nextPoint = new Point(consumer.getCurrentPoint().x, 
						consumer.resolvePoint(abs, 0, values[i]).y);
				consumer.addSegment(new LineToSegment(nextPoint));
				consumer.setCurrentPoint(nextPoint);
			}
		}
		
		private void cubic(PathConsumer consumer) {
			// Params: (x1 y1 x2 y2 x y)+
			
			/*
			 * C (uppercase) indicates that absolute coordinates will follow; 
			 * c (lowercase) indicates that relative coordinates will follow.
			 */
			final boolean abs = Character.isUpperCase(code);

			/*
			 * Draws a cubic Bezier curve from the current point to (x,y) 
			 * using (x1,y1) as the control point at the beginning of the 
			 * curve and (x2,y2) as the control point at the end of the 
			 * curve.
			 *
			 * Multiple sets of coordinates may be specified to draw a polybezier. 
			 * At the end of the command, the new current point becomes the 
			 * final (x,y) coordinate pair used in the polybezier. 
			 */
			for (int i = 0; i < values.length; i += 6) {
				double x1 = values[i]; 
				double y1 = values[i + 1]; 
				double x2 = values[i + 2]; 
				double y2 = values[i + 3];
				double x = values[i + 4];
				double y = values[i + 5];
				Point cp1 = consumer.resolvePoint(abs, x1, y1);
				Point cp2 = consumer.resolvePoint(cp1, abs, x2, y2);
				Point ep = consumer.resolvePoint(cp2, abs, x, y);
				consumer.addSegment(new CubicSegment(cp1, cp2, ep));
				consumer.setCurrentPoint(ep);
			}
		}

		private void cubicShort(PathConsumer consumer) {
			// Params: (x2 y2 x y)+
			
			/*
			 * S (uppercase) indicates that absolute coordinates will follow; 
			 * s (lowercase) indicates that relative coordinates will follow.
			 */
			final boolean abs = Character.isUpperCase(code);

			/*
			 * Draws a cubic Bezier curve from the current point to (x,y).
			 */
			for (int i = 0; i < values.length; i += 4) {
				double x2 = values[i]; 
				double y2 = values[i + 1]; 
				double x = values[i + 2];
				double y = values[i + 3];
				
				Point cp2 = consumer.resolvePoint(abs, x2, y2);
				Point ep = consumer.resolvePoint(cp2, abs, x, y);
				
				/*
				 * The first control point is assumed to be the reflection of 
				 * the second control point on the previous command relative 
				 * to the current point.
				 * 
				 * If there is no previous command or if the previous command 
				 * was not an C, c, S or s, assume the first control point is 
				 * coincident with the current point.
				 */
				Point cp1;
				Segment prev = consumer.getLastSegment();
				if (prev instanceof CubicSegment) {
					Point prevCp2 = ((CubicSegment) prev).cp2;
					Point cur = consumer.getCurrentPoint();
					// reflect prevCp2 over cur
					cp1 = new Point(cur.x*2 - prevCp2.x, cur.y*2 - prevCp2.y);
				} else {
					cp1 = consumer.getCurrentPoint();
				}
				
				consumer.addSegment(new CubicSegment(cp1, cp2, ep));
				consumer.setCurrentPoint(ep);
			}
		}
		
		private void quad(PathConsumer consumer) {
			// Params: (x1 y1 x y)+
			
			/*
			 * Q (uppercase) indicates that absolute coordinates will follow; 
			 * q (lowercase) indicates that relative coordinates will follow. 
			 */
			final boolean abs = Character.isUpperCase(code);

			/*
			 * Draws a quadratic Bezier curve from the current point to (x,y) 
			 * using (x1,y1) as the control point.
			 * 
			 * Multiple sets of coordinates may be specified to draw a polybezier. 
			 * At the end of the command, the new current point becomes the 
			 * final (x,y) coordinate pair used in the polybezier.
			 */
			for (int i = 0; i < values.length; i += 6) {
				double x1 = values[i]; 
				double y1 = values[i + 1]; 
				double x = values[i + 2];
				double y = values[i + 3];
				Point cp1 = consumer.resolvePoint(abs, x1, y1);
				Point ep = consumer.resolvePoint(cp1, abs, x, y);
				consumer.addSegment(new QuadSegment(cp1, ep));
				consumer.setCurrentPoint(ep);
			}
		}
		
		private void quadShort(PathConsumer consumer) {
			// Params: (x y)+
			
			/*
			 * T (uppercase) indicates that absolute coordinates will follow; 
			 * t (lowercase) indicates that relative coordinates will follow.
			 */
			final boolean abs = Character.isUpperCase(code);

			/*
			 * Draws a quadratic Bezier curve from the current point to (x,y). 
			 */
			for (int i = 0; i < values.length; i += 4) {
				double x = values[i];
				double y = values[i + 1];
				
				Point ep = consumer.resolvePoint(abs, x, y);
				
				/*
				 * The control point is assumed to be the reflection of the 
				 * control point on the previous command relative to the current 
				 * point.
				 * 
				 * If there is no previous command or if the previous command was 
				 * not a Q, q, T or t, assume the control point is coincident with 
				 * the current point.
				 */
				Point cp;
				Segment prev = consumer.getLastSegment();
				if (prev instanceof QuadSegment) {
					Point prevCp = ((QuadSegment) prev).cp;
					Point cur = consumer.getCurrentPoint();
					// reflect prevCp over cur
					cp = new Point(cur.x*2 - prevCp.x, cur.y*2 - prevCp.y);
				} else {
					cp = consumer.getCurrentPoint();
				}
				
				consumer.addSegment(new QuadSegment(cp, ep));
				consumer.setCurrentPoint(ep);
			}
		}
		
		private void arc(PathConsumer consumer) {
			// Params: (rx ry x-axis-rotation large-arc-flag sweep-flag x y)+
			
			final boolean abs = Character.isUpperCase(code);
			
			/*
			 * Draws an elliptical arc from the current point to (x, y).
			 * 
			 * The size and orientation of the ellipse are defined by two 
			 * radii (rx, ry) and an x-axis-rotation, which indicates how 
			 * the ellipse as a whole is rotated relative to the current 
			 * coordinate system.
			 * 
			 * The center (cx, cy) of the ellipse is calculated automatically 
			 * to satisfy the constraints imposed by the other parameters.
			 * 
			 * large-arc-flag and sweep-flag contribute to the automatic 
			 * calculations and help determine how the arc is drawn.
			 */

			for (int i = 0; i < values.length; i += 7) {
				double rx = values[i];
				double ry = values[i + 1];
				double xAxisRotation = values[i + 2];
				double largeArcFlag = values[i + 3];
				double sweepFlag = values[i + 4];
				double x = values[i + 5];
				double y = values[i + 6];
				
				Point ep = consumer.resolvePoint(abs, x, y);
				
				consumer.addSegment(new ArcToSvgSegment(rx, ry, 
						Geom.rad(xAxisRotation), 
						largeArcFlag, sweepFlag, ep));
				consumer.setCurrentPoint(ep);
			}

		}
		
	}
	
	private static class PathConsumer {

		private List<Segment> segments = new ArrayList<Segment>();

		private Point currentPoint;

		private Point subpathPoint;
		
		public void addSegment(Segment segment) {
			this.segments.add(segment);
		}

		public Segment getLastSegment() {
			if (this.segments.isEmpty()) {
				return null;
			}
			return this.segments.get(this.segments.size() - 1);
		}

		public void startNewSubpath(Point point) {
			this.subpathPoint = point;
			setCurrentPoint(point);
		}

		public void closePath() {
			if (this.subpathPoint != null) {
				setCurrentPoint(this.subpathPoint);
			}
		}

		public Point getCurrentPoint() {
			return this.currentPoint;
		}

		public void setCurrentPoint(Point point) {
			this.currentPoint = point;
		}

		public Point resolvePoint(boolean abs, double x, double y) {
			return resolvePoint(this.currentPoint, abs, x, y);
		}
		
		public Point resolvePoint(Point anchor, boolean abs, double x, double y) {
			if (abs || anchor == null) {
				return new Point(x, y);
			}
			return anchor.move(x, y);
		}

	}

}
