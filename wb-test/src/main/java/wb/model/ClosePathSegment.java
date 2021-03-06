package wb.model;

public class ClosePathSegment extends Segment {

	@Override
	public void outline(Pane pane) {
		Point p = pane.getPathStartPoint();
		if (p != null) {
			pane.lineTo(p);
		}
		// http://dev.w3.org/html5/2dcontext/#dom-context-2d-moveto
		// he closePath() method must do nothing if the object's path has no subpaths. 
		// Otherwise, it must mark the last subpath as closed, create a new subpath 
		// whose first point is the same as the previous subpath's first point, and 
		// finally add this new subpath to the path.
	}

	@Override
	public Double getStartAngle(Pane pane) {
		return null;
	}

	@Override
	public Animation createAnimation() {
		return null;
	}

}
