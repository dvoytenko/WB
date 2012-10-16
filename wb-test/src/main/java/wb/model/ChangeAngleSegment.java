package wb.model;

// NOT USED YET
public class ChangeAngleSegment extends Segment {

	private Segment nextSegment;
	
	public ChangeAngleSegment(Segment nextSegment) {
		this.nextSegment = nextSegment;
	}

	@Override
	public Double getStartAngle(Pane canvas) {
		return null;
	}

	@Override
	public void outline(Pane canvas) {
	}

	@Override
	public Animation createAnimation() {
		return new AnimationImpl();
	}

	private class AnimationImpl implements Animation {
		
		private boolean done;
		
		private Double startAngle;

		private Double endAngle;

		private double da;

		private Pane canvas;

//		private Board board;

		private double threshold;

		private double velocity;

		@Override
		public void start(Board board) {
//			this.board = board;
//			this.velocity = board.getBaseChangeAngleVelocity();
			this.canvas = board.getAnimationPane();
			
			// FIXME
//			this.startAngle = this.canvas.getCurrentAngle();
			this.endAngle = nextSegment.getStartAngle(canvas);
			
			double da = Math.abs(this.endAngle - this.startAngle);
			if (da > Geom.PI) {
				da = - (Geom.PI_2 - da);
			}
			da *= Math.signum(this.endAngle - this.startAngle);
			
			this.da = da;
			this.threshold = Geom.PI / 15;
			this.done = Math.abs(da) < this.threshold;
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
			
			double totalTimeNeeded = Math.abs(this.da)/velocity;
			double angleComplete = this.da * Math.min(time, totalTimeNeeded) / totalTimeNeeded;
			
//			this.board.updateCurrentAngle(this.startAngle + angleComplete);
//			this.board.updateCurrentVelocity(0.0);
			
			this.done = Math.abs(this.da - angleComplete) < this.threshold;
		}

	}
	
}
