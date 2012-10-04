package wb.model;

public class LineToSegment extends LineSegment {
	
	public Point point;
	
	public LineToSegment() {
	}

	public LineToSegment(Point point) {
		this.point = point;
	}

	@Override
	protected Point resolvePoint(Pane pane) {
		return this.point;
	}

}
