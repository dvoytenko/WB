package wb.eraser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class Eraser {
	
	public static void main(String[] args) {
		
		double[] shape = {
				10, 100,
				40, 50,
				80, 60,
				100, 110,
				100, 130,
				70, 150,
				40, 150,
				10, 100 // z
		};
		
		Eraser eraser = new Eraser(shape);
		
		System.out.println(eraser.lines);
		
		System.out.println("bounding box: " + eraser.bounds);
		
		System.out.println("horiz(40) " + eraser.scanHoriz(40.0)); // null
		System.out.println("horiz(151) " + eraser.scanHoriz(40.0)); // null
		System.out.println("horiz(50) " + eraser.scanHoriz(50.0)); // 1 point
		System.out.println("horiz(55) " + eraser.scanHoriz(55.0)); // honest line
		System.out.println("horiz(60) " + eraser.scanHoriz(60.0)); // honest line
		System.out.println("horiz(100) " + eraser.scanHoriz(100.0)); // honest line
		System.out.println("horiz(110) " + eraser.scanHoriz(110.0)); // honest line
		System.out.println("horiz(120) " + eraser.scanHoriz(120.0)); // honest line
		System.out.println("horiz(130) " + eraser.scanHoriz(130.0)); // honest line
		System.out.println("horiz(140) " + eraser.scanHoriz(140.0)); // honest line
		System.out.println("horiz(150) " + eraser.scanHoriz(150.0)); // honest line
	}
	
	
	private List<Line> lines = new ArrayList<Line>();
	
	private Bounds bounds;

	public Eraser(double[] poly) {
		Double top = null;
		Double left = null;
		Double right = null;
		Double bottom = null;
		
		Point prev = null;
		for (int i = 0; i < poly.length; i+=2) {
			Point next = new Point(poly[i], poly[i + 1]);
			if (prev != null) {
				lines.add(new Line(prev, next));
			}
			
			if (left == null || next.x < left) {
				left = next.x;
			}
			if (top == null || next.y < top) {
				top = next.y;
			}
			if (right == null || next.x > right) {
				right = next.x;
			}
			if (bottom == null || next.y > bottom) {
				bottom = next.y;
			}
			
			prev = next;
		}
		
		this.bounds = new Bounds(
				new Point(left, top), 
				new Point(right, bottom));
	}

	public List<Line> scanHoriz(double y) {
		List<Line> result = new ArrayList<Line>();
		
		if (y < this.bounds.topleft.y ||
				y > this.bounds.bottomright.y) {
			return result;
		}
		
		Line input = new Line(new Point(this.bounds.topleft.x, y), 
				new Point(this.bounds.bottomright.x, y));
		
		List<Point> points = new ArrayList<Point>();
		
		for (Line line : this.lines) {
			Point p = line.intersect(input);
			if (p != null) {
				points.add(p);
			}
		}
		
		Collections.sort(points, new Comparator<Point>() {
			@Override
			public int compare(Point p1, Point p2) {
				return Double.compare(p1.x, p2.x);
			}
		});
		
		Point prev = null;
		for (int i = 0; i < points.size(); i++) {
			Point point = points.get(i);
			if (prev != null) {
				if (Math.abs(prev.x - point.x) > 1e-3
						|| Math.abs(prev.y - point.y) > 1e-3) {
					result.add(new Line(prev, point));
					prev = null;
				} else {
					// two points equal, i.e. two lines intersect in the same point
					prev = point;
				}
			} else {
				prev = point;
			}
		}
		
		return result;
	}
	
	public static class Line {
		
		public Point p1;
		
		public Point p2;

		private double a;

		private double b;

		public Line(Point p1, Point p2) {
			this.p1 = p1;
			this.p2 = p2;
			
			this.b = p1.x == p2.x ? Double.NaN:
				(p2.y * p1.x - p1.y * p2.x)/(p1.x - p2.x);
			this.a = (p1.y - this.b)/p1.x;
		}
	
		public Point intersect(Line l) {
			
			if (l.minX() > this.maxX() || this.minX() > l.maxX()) {
				return null;
			}
			if (l.minY() > this.maxY() || this.minY() > l.maxY()) {
				return null;
			}
			
			if (l.isHoriz() && this.isHoriz()) {
				return null;
			}
			if (l.isVert() && this.isVert()) {
				return null;
			}
			
			if (this.a == l.a) {
				return null;
			}
			
			Point point;
			
			if (l.isHoriz() && this.isVert()) {
				point = new Point(this.p1.x, l.p1.y);
			} else if (this.isHoriz() && l.isVert()) {
				point = new Point(l.p1.x, this.p1.y);
			} else if (l.isVert()) {
				point = new Point(l.p1.x, this.y(l.p1.x));
			} else if (this.isVert()) {
				point = new Point(this.p1.x, l.y(this.p1.x));
			} else if (this.isHoriz()) {
				point = new Point((this.b - l.b)/l.a,
						this.b);
			} else {
				double y = (l.b * this.a - this.b * l.a) /
						(this.a - l.a);
				double x = (y - this.b) / this.a;
				point = new Point(x, y);
			}
			
			// check the point belongs to both parts of lines
			if (point.x < this.minX() || point.x > this.maxX()
					|| point.y < this.minY() || point.y > this.maxY()) {
				return null;
			}
			if (point.x < l.minX() || point.x > l.maxX()
					|| point.y < l.minY() || point.y > l.maxY()) {
				return null;
			}

			return point;
		}

		private double y(double x) {
			return a * x + b;
		}

		private double minX() {
			return Math.min(this.p1.x, this.p2.x);
		}

		private double maxX() {
			return Math.max(this.p1.x, this.p2.x);
		}

		private double minY() {
			return Math.min(this.p1.y, this.p2.y);
		}

		private double maxY() {
			return Math.max(this.p1.y, this.p2.y);
		}
		
		private boolean isVert() {
			return this.p1.x == this.p2.x;
		}

		private boolean isHoriz() {
			return this.p1.y == this.p2.y;
		}
		
		@Override
		public String toString() {
			return "Line: " + p1 + " -> " + p2;
		}
	}
	
}
