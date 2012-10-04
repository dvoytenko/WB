package wb.model;

import java.io.StringReader;

import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.xml.sax.InputSource;

import junit.framework.TestCase;

public class SvgParserTest extends TestCase {

	private Element dom(String s) throws Exception {
        SAXReader reader = new SAXReader();
        InputSource source = new InputSource(new StringReader(s));
		return reader.read(source).getRootElement();
	}
	
	public void testPath() throws Exception {
		SvgParser parser = new SvgParser();
		Element svg = dom(
				"<path d='M 48.996171,37.251271" +
				" C 47.958668,32.143973 39.185631,24.658486 33.930828,26.057885" +
				" z'/>");
		Shape shape = parser.parse(svg);
		assertTrue(shape instanceof PathShape);
		
		PathShape path = (PathShape) shape;
		assertEquals(3, path.pathSegment.segments.size());
		
		// M 48.996171,37.251271
		{
			Segment s = path.pathSegment.segments.get(0);
			assertTrue(s instanceof MoveToSegment);
			MoveToSegment m = (MoveToSegment) s;
			assertEquals(48.996171, m.point.x, 1e-3);
			assertEquals(37.251271, m.point.y, 1e-3);
		}

		// C 47.958668,32.143973 39.185631,24.658486 33.930828,26.057885
		{
			Segment s = path.pathSegment.segments.get(1);
			assertTrue(s instanceof CubicSegment);
			CubicSegment c = (CubicSegment) s;
			assertEquals(47.958668, c.cp1.x, 1e-3);
			assertEquals(32.143973, c.cp1.y, 1e-3);
			assertEquals(39.185631, c.cp2.x, 1e-3);
			assertEquals(24.658486, c.cp2.y, 1e-3);
			assertEquals(33.930828, c.endPoint.x, 1e-3);
			assertEquals(26.057885, c.endPoint.y, 1e-3);
		}
		
		// z
		{
			Segment s = path.pathSegment.segments.get(2);
			assertTrue(s instanceof ClosePathSegment);
		}
	}

	public void testTransform() throws Exception {
		
		SvgParser parser = new SvgParser();
		
		assertNull(parser.parseTransform(null));
		assertNull(parser.parseTransform(""));
		
		Transform tr = parser.parseTransform("translate(100, 100) scale(0.5, 2) rotate(90) translate(-20 -20)");
		Transform ex = new Transform().translate(100, 100).scale(0.5, 2).rotate(Geom.rad(90)).translate(-20, -20);
		assertEquals("a in " + tr + " vs " + ex, ex.m[0], tr.m[0], 1e-3);
		assertEquals("b in " + tr + " vs " + ex, ex.m[1], tr.m[1], 1e-3);
		assertEquals("c in " + tr + " vs " + ex, ex.m[2], tr.m[2], 1e-3);
		assertEquals("d in " + tr + " vs " + ex, ex.m[3], tr.m[3], 1e-3);
		assertEquals("e in " + tr + " vs " + ex, ex.m[4], tr.m[4], 1e-3);
		assertEquals("f in " + tr + " vs " + ex, ex.m[5], tr.m[5], 1e-3);
	}
	
}
