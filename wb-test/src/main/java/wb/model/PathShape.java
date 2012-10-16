package wb.model;

import java.util.List;

public class PathShape extends Shape {
	
	public PathSegment pathSegment = new PathSegment();
	
	public PathShape() {
	}
	
	public PathShape(List<Segment> segments) {
		this.pathSegment.segments.addAll(segments);
	}

	public Double getStartAngle(Pane pane) {
		return this.pathSegment.getStartAngle(pane);
	}

	public void outline(Pane pane) {
		this.pathSegment.outline(pane);
	}

	@Override
	public void draw(Pane pane) {
		pane.beginPath();
		outline(pane);
		pane.stroke();
	}

	@Override
	public Animation createAnimation() {
		return null;
	}
	
}
