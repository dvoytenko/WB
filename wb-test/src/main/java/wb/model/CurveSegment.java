package wb.model;

import java.util.List;

public abstract class CurveSegment extends Segment {

	private PathSegment pathSegment;

	protected abstract List<Segment> expand(Pane pane);
	
	public PathSegment pathSegment(Pane pane) {
		// TODO: find a way to optimize. Resolve segments against the context? shape+pane+place
		// TODO: is this a good idea? can't reuse shapes with different start points
		if (this.pathSegment == null) {
			List<Segment> segments = this.expand(pane);
			this.pathSegment = new PathSegment(segments);
		}
		return this.pathSegment;
	}

	public void outline(Pane pane) {
		this.pathSegment(pane).outline(pane);
	}
	
	@Override
	public Double getStartAngle(Pane pane) {
		return null;
	}
	
}
