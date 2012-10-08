package wb.model;

public class DrawShapeEpisode extends Episode {
	
	public Shape shape;
	
	public String shapeId;

	public Point position;
	
	public Double width;
	
	public Double height;
	
	public Double realWidth;
	
	public Double realHeight;
	
	public Double rotationDegree;
	
	@Override
	public void prepare(PrepareScript preparator) {
		super.prepare(preparator);
		if (this.shape == null && this.shapeId != null) {
			this.shape = preparator.getShape(this.shapeId);
		}
	}

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
