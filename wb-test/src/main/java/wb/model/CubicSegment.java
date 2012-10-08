package wb.model;

public class CubicSegment extends CurveSegment {

	public Point cp1;

	public Point cp2;
	
	public Point endPoint;
	
	public CubicSegment() {
	}

	public CubicSegment(Point cp1, Point cp2, Point endPoint) {
		this.cp1 = cp1;
		this.cp2 = cp2;
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
