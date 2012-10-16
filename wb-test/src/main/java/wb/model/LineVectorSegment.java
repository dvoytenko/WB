package wb.model;

// NOT USED YET
public class LineVectorSegment extends LineSegment {
	
	public Double distance;
	
	public Double angle;
	
	@Override
	protected Point resolvePoint(Pane canvas) {
		return Geom.vector(canvas.getCurrentPoint(), this.distance, this.angle);
	}
	
}
