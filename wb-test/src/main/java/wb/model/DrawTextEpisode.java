package wb.model;

public class DrawTextEpisode extends ShapeEpisodeBase {
	
	public String text;
	
	@Override
	protected Shape resolveShape(PrepareScript preparator) {
		TextShape shape = new TextShape();
		shape.text = this.text;
		return shape;
	}
	
}
