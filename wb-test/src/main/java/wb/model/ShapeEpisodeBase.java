package wb.model;

public abstract class ShapeEpisodeBase extends Episode {
	
	public Shape shape;
	
	public Point position;
	
	public Double width;
	
	public Double height;
	
	// TODO move to the shape?
	public Double realWidth;
	
	public Double realHeight;
	
	public Double rotationDegree;
	
	public Double rate;
	
	public boolean predraw;
	
	public String action;
	
	public Point moveStartPosition;
	
	@Override
	public void prepare(PrepareScript preparator) {
		super.prepare(preparator);
		if (this.shape == null) {
			this.shape = resolveShape(preparator);
		}
		if (this.shape != null) {
			this.shape.prepare(preparator);
		}
		if (this.shape instanceof SizeAwareShape) {
			SizeAwareShape group = (SizeAwareShape) this.shape;
			if (this.realWidth == null) {
				this.realWidth = group.getWidth();
			}
			if (this.realHeight == null) {
				this.realHeight = group.getHeight();
			}
		}
		if ((this.width == null || this.height == null)
				&& (this.realWidth != null && this.realHeight != null)) {
			if (this.width != null) {
				this.height = this.realHeight * this.width / this.realWidth;
			} else {
				this.width = this.realWidth * this.height / this.realHeight;
			}
		}
	}

	protected abstract Shape resolveShape(PrepareScript preparator);

	@Override
	public Animation createAnimation() {
		return null;
	}
	
}
