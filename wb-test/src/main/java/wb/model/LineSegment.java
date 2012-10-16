package wb.model;

public abstract class LineSegment extends Segment {
	
	protected abstract Point resolvePoint(Pane canvas);
	
	@Override
	public Double getStartAngle(Pane canvas) {
		return Geom.angle(canvas.getCurrentPoint(), 
				resolvePoint(canvas));
	}
	
	@Override
	public void outline(Pane pane) {
		pane.lineTo(resolvePoint(pane));
	}
	
	@Override
	public Animation createAnimation() {
		return null;
	}
	
}
