package wb.model;

public class MoveToSegment extends Segment {
	
	public Point point;
	
	public MoveToSegment() {
	}
	
	public MoveToSegment(Point point) {
		this.point = point;
	}
	
	public Point point(Pane pane) {
		return this.point;
	}

	@Override
	public Double getStartAngle(Pane pane) {
		return Geom.angle(pane.getCurrentPoint(), 
				this.point(pane));
	}

	@Override
	public void outline(Pane pane) {
		pane.moveTo(this.point(pane));
	}
	
	@Override
	public Animation createAnimation() {
		return new MoveAnimation();
	}
	
	private class MoveAnimation implements Animation {

		private Board board;
		
		private boolean done;
		
		private Point startPoint;

		private Point endPoint;
		
		private double velocity;

		public MoveAnimation() {
		}

		@Override
		public void start(Board board) {
			this.board = board;
			this.velocity = board.getBaseMoveVelocity();
			this.startPoint = board.getCurrentPosition(false);
			this.endPoint = point(board.getAnimationPane());
			this.done = board.getAnimationPane()
					.distance(startPoint, endPoint) < 1.0;
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
			if (this.done) {
				return;
			}
			
			// TODO optimize
			
			Pane pane = this.board.getAnimationPane();
			
	    	double dx = endPoint.x - startPoint.x;
	    	double dy = endPoint.y - startPoint.y;
	        double a = dy/dx;
	        
	        double actualDistance = pane.distance(startPoint, endPoint);
	        double distance = time * velocity;
	        if (distance > actualDistance) {
	        	distance = actualDistance;
	        }
	        
			double x2 = dx * distance/actualDistance;
			double y2 = dx != 0 ? a * x2 : dy * distance/actualDistance;
			Point newPoint = startPoint.move(x2, y2);
			
			pane.moveTo(newPoint);
			
		    this.done = Math.abs(actualDistance - distance) < 1;

		    board.updateCurrentPosition(newPoint, false);
		    board.updateCurrentVelocity(velocity);
		    board.updateCurrentAngle(Math.atan2(dy, dx));
		    board.updateCurrentPressure(0.0);
		    board.updateCurrentHeight(0.0);
		}

	}

}
