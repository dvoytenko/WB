package wb.model;

public class DrawShapeEpisode extends Episode {
	
	public Shape shape;

	@Override
	public Animation createAnimation() {
		return new AnimationImpl();
	}
	
	private class AnimationImpl extends AnimationDelegate {

		public AnimationImpl() {
			super(shape.createAnimation());
		}
		
		@Override
		public void end() {
			super.end();
			getBoard().commit(shape, true);
			getBoard().getAnimationPane().canvas().clear();
		}
		
		@Override
		public void frame(long time) {
			getBoard().getAnimationPane().canvas().clear();
			super.frame(time);
		}
		
	}

}
