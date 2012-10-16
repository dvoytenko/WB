package wb.model;

// NOT USED YET
public class ArcToSegment extends ArcSegment {
	
	/* Form4: (canvas arcTo: http://www.w3schools.com/tags/canvas_arcto.asp)
		pinaclePoint
		endPoint
		radius
	*/
	
	public Point endPoint;
	
	public Double radius;

	public Point tangentPoint;
	
	@Override
	protected Arc resolveArc(Pane canvas) {
		final Point startPoint = canvas.getCurrentPoint();
		
		double r = this.radius;
		Point t = this.tangentPoint;
		
		double sa = Geom.angle(t, startPoint) + Math.PI/2;
		Point center = Geom.vector(startPoint, radius, sa);
		double ea = Geom.angle(center, t);
		return new Arc(center, r, r, 0, sa, ea, false);
	}

}
