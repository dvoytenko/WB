package wb.model;

import java.util.ArrayList;
import java.util.List;

/**
 * http://www.w3.org/TR/SVG/shapes.html#PolygonElement
 */
public class PolygonShape extends PathBasedShape {
	
	public List<Point> points = new ArrayList<Point>();
	
	@Override
	protected List<Segment> resolveSegments() {
		
		/*
		 * A ‘polyline’ element can be mapped to an equivalent ‘path’ element as follows:
		 * 
		 */
		List<Segment> segments = new ArrayList<Segment>();
		
		/*
		 * Perform an absolute moveto operation to the first coordinate 
		 * pair in the list of points 
		 */
		segments.add(new MoveToSegment(this.points.get(0)));
		
		/*
		 * For each subsequent coordinate pair, perform an absolute 
		 * lineto operation to that coordinate pair
		 */
		for (int i = 1; i < this.points.size(); i++) {
			segments.add(new LineToSegment(this.points.get(i)));
		}
		
		segments.add(new ClosePathSegment());
		
		return segments;
	}

}
