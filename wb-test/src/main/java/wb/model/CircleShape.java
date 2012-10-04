package wb.model;

public class CircleShape extends Shape {
	
	public ArcAngleSegment arcSegment;

	@Override
	public Point getStartPoint() {
		return arcSegment.getStartPoint();
	}
	
	@Override
	public void draw(Pane canvas) {
		canvas.beginPath();
		
		this.arcSegment.outline(canvas);
		
		canvas.stroke();
	}

	@Override
	public Animation createAnimation() {
		return new AnimationImpl(this.arcSegment.createAnimation());
	}

	private class AnimationImpl extends AnimationDelegate {

		public AnimationImpl(Animation animation) {
			super(animation);
		}
		
		@Override
		public void frame(long time) {
			
			Pane canvas = getBoard().getAnimationPane();
			canvas.beginPath();
			
			super.frame(time);
			
			canvas.stroke();
		}
		
	}

}
