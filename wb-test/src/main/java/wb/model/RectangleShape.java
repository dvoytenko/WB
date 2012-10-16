package wb.model;

import java.util.ArrayList;
import java.util.List;

public class RectangleShape extends PathBasedShape {
	
	public Point topleft;
	
	public Double width;
	
	public Double height;
	
	@Override
	protected List<Segment> resolveSegments() {
		List<Segment> segments = new ArrayList<Segment>();
		segments.add(new MoveToSegment(this.topleft));
		segments.add(new LineToSegment(new Point(this.topleft.x + this.width, this.topleft.y)));
		segments.add(new LineToSegment(new Point(this.topleft.x + this.width, this.topleft.y + this.height))); 
		segments.add(new LineToSegment(new Point(this.topleft.x, this.topleft.y + this.height)));
		segments.add(new ClosePathSegment());
		return segments;
	}

}
