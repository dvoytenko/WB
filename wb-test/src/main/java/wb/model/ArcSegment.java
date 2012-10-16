package wb.model;

public class ArcSegment extends Segment {
	
	public static class Arc {
		
		public Point center;
		
		public double radiusX;

		public double radiusY;
		
		public double xAxisRotation;

		public double startAngle;
		
		public double endAngle;
		
		public boolean counterclockwise;
		
		public Arc() {
		}

		public Arc(Point center, double radiusX, double radiusY, double xAxisRotation, 
				double startAngle, double endAngle, boolean counterclockwise) {
			this.center = center;
			this.radiusX = radiusX;
			this.radiusY = radiusY;
			this.xAxisRotation = xAxisRotation;
			this.startAngle = startAngle;
			this.endAngle = endAngle;
			this.counterclockwise = counterclockwise;
		}
		
	}
	
	/* +Form1: (http://www.w3schools.com/tags/canvas_arc.asp)
	     centerPoint
	     radius
	     startAngle
	     endAngle
	     counterclockwise
	 */
	
	/* Form2:
		 (startPoint)
		 centerPoint
		 endPoint or angle
	*/
	
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
	
	/* +Form4: (canvas arcTo: http://www.w3schools.com/tags/canvas_arcto.asp)
		tangentPoint
		endPoint
		radius
	*/
	
	public Arc arc;
	
	public ArcSegment() {
	}

	public ArcSegment(Arc arc) {
		this.arc = arc;
	}
	
	protected Arc resolveArc(Pane pane) {
		return this.arc;
	}
	
	@Override
	public Double getStartAngle(Pane canvas) {
		Arc arc = resolveArc(canvas);
        double da = arc.endAngle - arc.startAngle;
		return da >= 0 ? Geom.PI_HALF + arc.startAngle : Geom.PI_1_HALF + arc.startAngle;
	}
	
	@Override
	public void outline(final Pane pane) {
		final Arc arc = resolveArc(pane);
		
		boolean isCircle = Math.abs(arc.radiusX - arc.radiusY) < 1e-2;
		boolean isRotated = Math.abs(arc.xAxisRotation) >= 1e-2;

		// TODO connect current point with the first arc point with a straight line
		
		if (isCircle && !isRotated) {
			// simple arc
			pane.arc(arc.center, arc.radiusX, arc.startAngle, arc.endAngle, 
					arc.counterclockwise);
		} else {
			
			final double r = Math.max(arc.radiusX, arc.radiusY); 
            
            Transform tr = new Transform();
            tr.translate(arc.center.x, arc.center.y);
            if (isRotated) {
            	tr.rotate(arc.xAxisRotation);
            }
            if (!isCircle) {
            	tr.scale(arc.radiusX/r, arc.radiusY/r);
            }
            
            pane.withTr(tr, new Runnable() {
				@Override
				public void run() {
					pane.arc(new Point(0, 0), r, arc.startAngle, 
							arc.endAngle, arc.counterclockwise);
				}
			});
		}
	}

	@Override
	public Animation createAnimation() {
		return null;
	}

}
