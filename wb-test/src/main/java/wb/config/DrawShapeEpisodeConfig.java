package wb.config;

import wb.model.DrawShapeEpisode;
import wb.model.GroupShape;
import wb.model.Point;
import wb.model.Shape;
import wb.model.Transform;

public class DrawShapeEpisodeConfig {
	
	public String shapeId;
	
	private Shape resolvedShape;
	
	public Point position;
	
	public Double width;
	
	public Double height;
	
	public Double realWidth;
	
	public Double realHeight;
	
	public Double rotation;
	
	public DrawShapeEpisode toEpisode() {
		
		DrawShapeEpisode episode = new DrawShapeEpisode();
		
		GroupShape top = new GroupShape();
		episode.shape = top;
		
		Shape shape = this.resolvedShape;
		top.shapes.add(shape);
		
		top.transform = new Transform();
		top.transform.translate(this.position.x, this.position.y);
		
		top.transform.scale(this.width/this.realWidth, 
				this.height/this.realHeight);
		
		if (this.rotation != null && this.rotation != 0) {
			top.transform.rotate(this.rotation);
		}
		
		return episode;
	}

}
