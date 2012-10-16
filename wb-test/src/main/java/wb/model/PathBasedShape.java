package wb.model;

import java.util.List;

public abstract class PathBasedShape extends Shape {
	
	private PathShape pathCache;
	
	private PathShape path() {
		if (this.pathCache == null) {
			this.pathCache = new PathShape(resolveSegments());
		}
		return this.pathCache;
	}

	protected abstract List<Segment> resolveSegments();
	
	@Override
	public void draw(Pane pane) {
		this.path().draw(pane);
	}
	
	@Override
	public Animation createAnimation() {
		return null;
	}
	
}
