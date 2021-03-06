package wb.model;

import java.util.ArrayList;
import java.util.List;

public class GroupShape extends Shape implements SizeAwareShape {
	
	public String id;
	
	public String title;
	
	public String source;
	
	public String url;
	
	public String author;
	
	public Double width;

	public Double height;
	
	public Transform transform;
	
	public List<String> tags;
	
	public List<Shape> shapes = new ArrayList<Shape>();
	
	public GroupShape() {
	}

	public GroupShape(Shape shape) {
		this.shapes.add(shape);
		if (shape instanceof SizeAwareShape) {
			this.width = ((SizeAwareShape) shape).getWidth();
			this.height = ((SizeAwareShape) shape).getHeight();
		}
	}

	@Override
	public Double getWidth() {
		return this.width;
	}
	
	@Override
	public Double getHeight() {
		return this.height;
	}
	
	public void prepare(PrepareScript prepareScript) {
		super.prepare(prepareScript);
		for (Shape shape : this.shapes) {
			shape.prepare(prepareScript);
		}
	}

	@Override
	public void draw(final Pane pane) {
		pane.withTr(this.transform, new Runnable() {
			@Override
			public void run() {
				_draw(pane);
			}
		});
	}
	
	private void _draw(Pane pane) {
		for (Shape shape : this.shapes) {
			shape.draw(pane);
		}
	}
	
	@Override
	public Animation createAnimation() {
		return null;
	}

}
