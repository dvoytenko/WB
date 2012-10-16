package wb.model;

public class MoveToSegment extends Segment {
	
	public Point point;
	
	public MoveToSegment() {
	}
	
	public MoveToSegment(Point point) {
		this.point = point;
	}
	
	@Override
	public Double getStartAngle(Pane pane) {
		return Geom.angle(pane.getCurrentPoint(), 
				this.point);
	}

	@Override
	public void outline(Pane pane) {
		pane.moveTo(this.point);
	}
	
	@Override
	public Animation createAnimation() {
		return null;
	}
	
}
