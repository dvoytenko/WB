package wb.model;

public class ArcToSvgSegment extends ArcSegment {

	/* Form3: (svg path arcTo: http://www.w3.org/TR/SVG/paths.html#InterfaceSVGPathSegArcAbs)
		// (rx ry x-axis-rotation large-arc-flag sweep-flag x y)+
		radiusX
		radiusY
		xAxisRotation(angle)
		largArcFlag: 1 or 0: larger vs smaller arch sweep
		sweepFlag: 1 or 0: positive vs negative direction
		endPoint(x,y)
		
		// a25,25 -30 0,1 50,-25 l 50,-25
		radiusX = 25
		radiusY = 25
		xAxisRotation = -30
		largArcFlag = 0
		sweepFlag = 1
		endPoint.x = 50
		endPoint.y = -25
	*/
	
	public double radiusX;
	
	public double radiusY;
	
	public double xAxisRotation;
	
	public double largeArcFlag;
	
	public double sweepFlag;
	
	public Point endPoint;
	
	public ArcToSvgSegment() {
	}

	public ArcToSvgSegment(double rx, double ry, double xAxisRotation,
			double largeArcFlag, double sweepFlag, Point endPoint) {
		this.radiusX = rx;
		this.radiusY = ry;
		this.xAxisRotation = xAxisRotation;
		this.largeArcFlag = largeArcFlag;
		this.sweepFlag = sweepFlag;
		this.endPoint = endPoint;
	}

	@Override
	protected Arc resolveArc(Pane pane) {
		
		// Derived from: http://www.w3.org/TR/SVG/implnote.html#ArcImplementationNotes
		
		Point cp = pane.getCurrentPoint();

		double psi = this.xAxisRotation;
		double x1 = cp.x;
		double y1 = cp.y;
		double x2 = this.endPoint.x;
		double y2 = this.endPoint.y;
		double rx = this.radiusX;
		double ry = this.radiusY;

		double xp = Math.cos(psi) * (x1 - x2) / 2.0 + Math.sin(psi) * (y1 - y2) / 2.0;
		double yp = -1 * Math.sin(psi) * (x1 - x2) / 2.0 + Math.cos(psi) * (y1 - y2) / 2.0;
		double fa = largeArcFlag;
		double fs = sweepFlag;
		
		double lambda = (xp * xp) / (rx * rx) + (yp * yp) / (ry * ry);
		if (lambda > 1) {
			rx *= Math.sqrt(lambda);
			ry *= Math.sqrt(lambda);
		}

		double f = Math.sqrt((((rx * rx) * (ry * ry)) - ((rx * rx) * (yp * yp)) - ((ry * ry) * (xp * xp))) 
				/ ((rx * rx) * (yp * yp) + (ry * ry) * (xp * xp)));
		if (fa == fs) {
			f *= -1;
		}
		if (Double.isNaN(f)) {
			f = 0;
		}

		double cxp = f * rx * yp / ry;
		double cyp = f * -ry * xp / rx;
		double cx = (x1 + x2) / 2.0 + Math.cos(psi) * cxp - Math.sin(psi) * cyp;
		double cy = (y1 + y2) / 2.0 + Math.sin(psi) * cxp + Math.cos(psi) * cyp;

		double theta = angle(new Point(1, 0), new Point((xp - cxp) / rx, (yp - cyp) / ry));
		Point u = new Point((xp - cxp) / rx, (yp - cyp) / ry);
		Point v = new Point((-1 * xp - cxp) / rx, (-1 * yp - cyp) / ry);
		double dTheta = angle(u, v);

		if(ratio(u, v) <= -1) {
			dTheta = Math.PI;
		}
		if(ratio(u, v) >= 1) {
			dTheta = 0;
		}
		if(fs == 0 && dTheta > 0) {
			dTheta = dTheta - 2 * Math.PI;
		}
		if(fs == 1 && dTheta < 0) {
			dTheta = dTheta + 2 * Math.PI;
		}

		// return [cx, cy, rx, ry, theta, dTheta, psi, fs];
        /*
	            var cx = p[0], cy = p[1], rx = p[2], ry = p[3], theta = p[4], dTheta = p[5], psi = p[6], fs = p[7];
	
	            var r = (rx > ry) ? rx : ry;
	            var scaleX = (rx > ry) ? 1 : rx / ry;
	            var scaleY = (rx > ry) ? ry / rx : 1;
	
	            context.translate(cx, cy);
	            context.rotate(psi);
	            context.scale(scaleX, scaleY);
	            
        		context.arc(0, 0, r, theta, theta + dTheta, 1 - fs);
         */
		return new Arc(new Point(cx, cy), rx, ry, psi, theta, theta + dTheta, fs == 0.0);
	}
	
	private double angle(Point u, Point v) {
		return (u.x * v.y < u.y * v.x ? -1 : 1) * Math.acos(ratio(u, v));
	}

	private double ratio(Point u, Point v) {
		return (u.x * v.x + u.y * v.y) / (mag(u) * mag(v));
	}
	
	private double mag(Point v) {
		return Math.sqrt(v.x * v.x + v.y * v.y);
	}
	
}
