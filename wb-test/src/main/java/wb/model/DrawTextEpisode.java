package wb.model;

public class DrawTextEpisode extends Episode {
	
	public String text;
	
	public Point position;
	
	public Double fontHeight;
	
	public TextShape shape;
	
	public Double width;
	
	public Double height;
	
	public Double realWidth;
	
	public Double realHeight;
	
	@Override
	public void prepare(PrepareScript preparator) {
		super.prepare(preparator);
		/*
		if (this.shape == null) {
			this.shape = new TextShape();
			this.shape.text = this.text;
			// TODO customize
			Font font = preparator.getFont("nova_thin_extended");
			this.shape.prepare(font);
			
			this.realWidth = this.shape.realWidth;
			this.realHeight = this.shape.realHeight;
		}
		*/
	}

	@Override
	public Animation createAnimation() {
		return null;
	}

}
