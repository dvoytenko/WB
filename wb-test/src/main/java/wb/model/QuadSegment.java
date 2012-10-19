package wb.model;

import java.util.ArrayList;
import java.util.List;

import com.antigrain.adaptivebezier.agg.Curve3Div;

public class QuadSegment extends CurveSegment {

	public Point cp;
	
	public Point endPoint;
	
	public QuadSegment() {
	}

	public QuadSegment(Point cp, Point endPoint) {
		this.cp = cp;
		this.endPoint = endPoint;
	}
	
	@Override
	protected List<Segment> expand(Pane pane) {
		Curve3Div curve = new Curve3Div();
		Point cur = pane.getCurrentPoint();
		if (cur == null) {
			cur = new Point(0, 0);
		}
		List<Point> points = curve.exec(cur.x, cur.y, cp.x, cp.y, 
				endPoint.x, endPoint.y);
		List<Segment> segments = new ArrayList<Segment>();
		for (int i = 1; i < points.size(); i++) {
			segments.add(new LineToSegment(points.get(i)));
		}
		return segments;
	}

	@Override
	public Animation createAnimation() {
		return null;
	}

}
