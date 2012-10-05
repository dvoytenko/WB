package wb.model;

public class ArcAngleSegment extends ArcSegment {

	/* Form1: (http://www.w3schools.com/tags/canvas_arc.asp)
	    centerPoint
	    radius
	    startAngle
	    endAngle
	    counterclockwise
	*/
	
	public Point center;
	
	public double radiusX;

	public double radiusY;
	
	public double xAxisRotation;

	public double startAngle;
	
	public double endAngle;
	
	public boolean counterclockwise;
	
	public ArcAngleSegment() {
	}

	public ArcAngleSegment(Point center, double radiusX, double radiusY,
			double xAxisRotation, double startAngle, double endAngle,
			boolean counterclockwise) {
		this.center = center;
		this.radiusX = radiusX;
		this.radiusY = radiusY;
		this.xAxisRotation = xAxisRotation;
		this.startAngle = startAngle;
		this.endAngle = endAngle;
		this.counterclockwise = counterclockwise;
	}

	@Override
	protected Arc resolveArc(Pane canvas) {
		return new Arc(this.center, this.radiusX, this.radiusY, 
				this.xAxisRotation, this.startAngle, this.endAngle, 
				this.counterclockwise);
	}

}
