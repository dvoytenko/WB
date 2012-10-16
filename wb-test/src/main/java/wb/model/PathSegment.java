package wb.model;

import java.util.ArrayList;
import java.util.List;

public class PathSegment extends Segment {

	public List<Segment> segments = new ArrayList<Segment>();
	
	public PathSegment() {
	}

	public PathSegment(List<Segment> segments) {
		this.segments.addAll(segments);
	}

	@Override
	public void outline(Pane pane) {
		for (Segment segment : this.segments) {
			segment.outline(pane);
		}
	}
	
	@Override
	public Double getStartAngle(Pane pane) {
		if (segments == null || segments.isEmpty()) {
			return null;
		}
		return segments.get(0).getStartAngle(pane);
	}
	
	@Override
	public Animation createAnimation() {
		return null;
	}
	
}
