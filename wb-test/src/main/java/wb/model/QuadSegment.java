package wb.model;

public class QuadSegment extends CurveSegment {

	public Point cp;
	
	public Point endPoint;

	public QuadSegment(Point cp, Point endPoint) {
		this.cp = cp;
		this.endPoint = endPoint;
	}

	@Override
	public Animation createAnimation() {
		return null;
	}

	@Override
	public Double getStartAngle(Pane pane) {
		return null;
	}

	@Override
	public void outline(Pane pane) {
	}

}
