package wb.model;

import java.util.ArrayList;
import java.util.List;

import wb.model.ArcSegment.Arc;

public class EllipseShape extends PathBasedShape {
	
	public Point center;
	
	public Double radiusX;
	
	public Double radiusY;
	
	@Override
	protected List<Segment> resolveSegments() {
		List<Segment> segments = new ArrayList<Segment>();
//		segments.add(new MoveToSegment(new Point(this.center.x + radiusX, this.center.y)));
		segments.add(new ArcSegment(new Arc(this.center, this.radiusX, this.radiusY, 
				0, Geom.PI, Geom.PI * 3, false)));
		return segments;
	}

}
