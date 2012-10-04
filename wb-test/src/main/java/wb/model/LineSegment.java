package wb.model;

public abstract class LineSegment extends Segment {
	
	protected abstract Point resolvePoint(Pane canvas);
	
	@Override
	public Double getStartAngle(Pane canvas) {
		return Geom.angle(canvas.getCurrentPoint(), 
				resolvePoint(canvas));
	}
	
	@Override
	public void outline(Pane canvas) {
		canvas.lineTo(resolvePoint(canvas));
	}
	
	@Override
	public Animation createAnimation() {
		return new LineAnimation();
	}
	
	private class LineAnimation implements Animation {

		private Board board;
		
		private boolean done;
		
		private Point startPoint;

		private Point endPoint;

		private Double velocity;

		private Pane pane;

		private double dx;

		private double dy;

		private double totalDistance;

		@Override
		public void start(Board board) {
			this.board = board;
			this.velocity = board.getBaseVelocity();
			
			this.pane = this.board.getAnimationPane();
			this.startPoint = this.pane.getCurrentPoint();
			this.endPoint = resolvePoint(this.pane);

	    	this.dx = this.endPoint.x - this.startPoint.x;
	    	this.dy = this.endPoint.y - this.startPoint.y;
	        this.totalDistance = this.pane.distance(this.startPoint, this.endPoint);
	        this.done = this.totalDistance < 1.0;
		}

		@Override
		public boolean isDone() {
			return this.done;
		}

		@Override
		public void end() {
		}
		
		@Override
		public void frame(long time) {
	        
	        double distance = Math.min(time * this.velocity, this.totalDistance);
	        
			double x2 = this.dx * distance/this.totalDistance;
			double y2 = this.dx != 0 ? (this.dy/this.dx) * x2 : 
				this.dy * distance/this.totalDistance;
			
			Point newPoint = this.startPoint.move(x2, y2);
			
			this.pane.lineTo(newPoint);
			
		    this.done = Math.abs(this.totalDistance - distance) < 1;

		    this.board.updateCurrentPosition(newPoint, false);
		    this.board.updateCurrentVelocity(this.velocity);
		    this.board.updateCurrentAngle(Geom.angle(this.startPoint, newPoint));
		    this.board.updateCurrentHeight(0.0);
		}

	}

}
