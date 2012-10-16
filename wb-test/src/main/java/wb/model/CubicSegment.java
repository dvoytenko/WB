package wb.model;

import java.util.ArrayList;
import java.util.List;

import com.antigrain.adaptivebezier.agg.Curve4Div;

public class CubicSegment extends CurveSegment {

	public Point cp1;

	public Point cp2;
	
	public Point endPoint;
	
	public CubicSegment() {
	}

	public CubicSegment(Point cp1, Point cp2, Point endPoint) {
		this.cp1 = cp1;
		this.cp2 = cp2;
		this.endPoint = endPoint;
	}
	
	@Override
	protected List<Segment> expand(Pane pane) {
		Curve4Div curve = new Curve4Div();
		Point cur = pane.getCurrentPoint();
		if (cur == null) {
			cur = new Point(0, 0);
		}
		List<Point> points = curve.exec(cur.x, cur.y, cp1.x, cp1.y, 
				cp2.x, cp2.y, endPoint.x, endPoint.y);
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
