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
	}

	@Override
	public Animation createAnimation() {
		return null;
	}
	
}
