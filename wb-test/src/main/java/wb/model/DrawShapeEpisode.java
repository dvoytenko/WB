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
	
	public Double rate;
	
	@Override
	public void prepare(PrepareScript preparator) {
		super.prepare(preparator);
		if (this.shape == null && this.shapeId != null) {
			this.shape = preparator.getShape(this.shapeId);
		}
		if (this.shape instanceof GroupShape) {
			GroupShape group = (GroupShape) this.shape;
			if (this.realWidth == null) {
				this.realWidth = group.width;
				this.realHeight = group.height;
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

	@Override
	public Animation createAnimation() {
		return null;
	}
	
}
