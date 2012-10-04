package wb.model;

import java.util.List;

public class PathShape extends Shape {
	
	public PathSegment pathSegment = new PathSegment();
	
	public PathShape() {
	}
	
	public PathShape(List<Segment> segments) {
		this.pathSegment.segments.addAll(segments);
	}

	@Override
	public Point getStartPoint() {
		return this.pathSegment.getStartPoint();
	}

	public Double getStartAngle(Pane pane) {
		return this.pathSegment.getStartAngle(pane);
	}

	public void outline(Pane pane) {
		this.pathSegment.outline(pane);
	}

	@Override
	public void draw(Pane canvas) {
		
		canvas.beginPath();
		
		outline(canvas);
		
		canvas.stroke();
	}

	@Override
	public Animation createAnimation() {
		return new AnimationImpl();
	}
	
	private class AnimationImpl extends AnimationDelegate {

		public AnimationImpl() {
			super(pathSegment.createAnimation());
		}
		
		@Override
		public void frame(long time) {
			super.frame(time);
			getBoard().getAnimationPane().stroke();
		}

	}

}
