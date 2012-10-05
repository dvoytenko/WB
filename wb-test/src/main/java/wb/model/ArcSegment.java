package wb.model;

// TODO segment that movers cursor automatically is not a very good idea

public abstract class ArcSegment extends Segment {
	
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
	
	
	protected abstract Arc resolveArc(Pane pane);
	
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
		return new AnimationImpl();
	}

	private class AnimationImpl implements Animation {

		private Board board;
		
		private boolean done;

		private Arc arc;

		private double velocity;

		private Pane pane;

		private Point lastPoint;
		
		@Override
		public void start(Board board) {
			this.board = board;
			this.pane = board.getAnimationPane();
			this.velocity = board.getBaseVelocity();
			
			this.arc = resolveArc(this.pane);
	        double da = Math.abs(this.arc.endAngle - this.arc.startAngle);
			this.done = da < 1e-2;
		}

		@Override
		public void frame(long time) {
			
			final boolean isCircle = Math.abs(this.arc.radiusX - this.arc.radiusY) < 1e-2;
			final boolean isRotated = Math.abs(this.arc.xAxisRotation) >= 1e-2;
	        final double da = this.arc.endAngle - this.arc.startAngle;
			
			final double eap;
			if (isCircle && !isRotated) {
				// simple arc
		        double globalRadius = this.pane.toGlobalPoint(new Point(this.arc.radiusX, 0)).x;
		        double totalDistance = da * globalRadius;
		        double distance = this.velocity * time;
			    if (distance > totalDistance) {
			    	distance = totalDistance;
			    }
				eap = this.arc.startAngle + da * distance/totalDistance;
				this.pane.arc(this.arc.center, this.arc.radiusX, this.arc.startAngle, 
						eap, this.arc.counterclockwise);
				this.lastPoint = pane.toGlobalPoint(new Point(Math.cos(eap) * this.arc.radiusX, 
			    		Math.sin(eap) * this.arc.radiusX));
			} else {
				
				final double r = Math.max(this.arc.radiusX, this.arc.radiusY); 
	            
	            Transform tr = new Transform();
	            tr.translate(this.arc.center.x, this.arc.center.y);
	            if (isRotated) {
	            	tr.rotate(this.arc.xAxisRotation);
	            }
	            if (!isCircle) {
	            	tr.scale(this.arc.radiusX/r, this.arc.radiusY/r);
	            }

	            // TODO: arc length is not always the same!!!
		        double globalRadius = this.pane.toGlobalPoint(new Point(this.arc.radiusX, 0)).x;
		        double totalDistance = da * globalRadius;
		        double distance = this.velocity * time;
			    if (distance > totalDistance) {
			    	distance = totalDistance;
			    }
				eap = this.arc.startAngle + da * distance/totalDistance;
	            
				final AnimationImpl that = this;
	            this.pane.withTr(tr, new Runnable() {
					@Override
					public void run() {
						that.pane.arc(new Point(0, 0), r, that.arc.startAngle, 
								eap, that.arc.counterclockwise);
						that.lastPoint = that.pane.toGlobalPoint(new Point(Math.cos(eap) * r, 
					    		Math.sin(eap) * r));
					}
				});
			}
			
			this.done = Math.abs(this.arc.endAngle - eap) < 1;
		    
			board.updateCurrentPosition(lastPoint, false);
		    board.updateCurrentVelocity(velocity);
		    board.updateCurrentAngle(da >= 0 ? Geom.PI_HALF + eap : Geom.PI_1_HALF + eap);
		    board.updateCurrentHeight(0.0);
		}

		@Override
		public boolean isDone() {
			return this.done;
		}

		@Override
		public void end() {
		}
		
	}

}
