package wb.model;

public class DrawShapeEpisode extends ShapeEpisodeBase {
	
	public String shapeId;
	
	@Override
	protected Shape resolveShape(PrepareScript preparator) {
		if (this.shapeId == null) {
			return null;
		}
		return preparator.getShape(this.shapeId);
	}

	@Override
	public String toText() {
		return "Draw shape: " + this.shapeId;
	}

}
