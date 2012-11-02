package wb.model;

public class Geom {

	public static final double PI = Math.PI;
	
	public static final double PI_HALF = PI/2;

	public static final double PI_1_HALF = PI + PI_HALF;

	public static final double PI_2 = PI * 2;
	
	public static double angle(Point p1, Point p2) {
		return Math.atan2(p1.y - p2.y, p1.x - p2.x);
	}

	public static Point vector(Point start, double distance, double angle) {
		double dx = Math.cos(angle) * distance;
		double dy = Math.sin(angle) * distance;
		return start.move(dx, dy);
	}

	public static double rad(double grad) {
		return PI_2 * grad / 360; 
	}

	public static double grad(double rad) {
		return rad * 360 / PI_2; 
	}

	public static double distance(Point p1, Point p2) {
		double dx = p1.x - p2.x;
		double dy = p1.y - p2.y;
		return Math.sqrt(dx * dx + dy * dy);
	}

	public static boolean almostZero(double v, double eps) {
		return Math.abs(v) <= eps;
	}
	
}
