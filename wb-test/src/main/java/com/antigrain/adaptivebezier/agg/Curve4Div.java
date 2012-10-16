package com.antigrain.adaptivebezier.agg;

import java.util.ArrayList;
import java.util.List;

import wb.model.Point;

public class Curve4Div {

	public static void main(String[] args) {
		Curve4Div curve = new Curve4Div();

		curve.exec(48.996171, 37.251271, 47.958668, 32.143973, 39.185631,
				24.658486, 33.930828, 26.057885);

		System.out.println("Points: " + curve.points.size() + ": ");
		for (Point p : curve.points) {
			System.out.println("context.lineTo(" + p.x + ", " + p.y + ");");
		}
	}

	private int curveRecursionLimit = 32;

	private double curveCollinearityEpsilon = 1e-30;

	private double curveAngleToleranceEpsilon = 0.01;

	private double approximationScale = 1.0;

	private double angleTolerance = 0.0;

	private double cuspLimit = 0.0;

	private double distanceToleranceSquare;

	private List<Point> points = new ArrayList<Point>();
	
	public List<Point> exec(double x1, double y1, double x2, double y2, double x3,
			double y3, double x4, double y4) {
		this.points.clear();
		this.distanceToleranceSquare = 0.5 / this.approximationScale;
		this.distanceToleranceSquare *= this.distanceToleranceSquare;
		bezier(x1, y1, x2, y2, x3, y3, x4, y4);
		return this.points;
	}

	public void bezier(double x1, double y1, double x2, double y2, double x3,
			double y3, double x4, double y4) {
		this.points.add(new Point(x1, y1));
		recursiveBezier(x1, y1, x2, y2, x3, y3, x4, y4, 0);
		this.points.add(new Point(x4, y4));
	}

	public void recursiveBezier(double x1, double y1, double x2, double y2,
			double x3, double y3, double x4, double y4, int level) {
		if (level > this.curveRecursionLimit) {
			return;
		}

		// Calculate all the mid-points of the line segments
		// ----------------------
		double x12 = (x1 + x2) / 2;
		double y12 = (y1 + y2) / 2;
		double x23 = (x2 + x3) / 2;
		double y23 = (y2 + y3) / 2;
		double x34 = (x3 + x4) / 2;
		double y34 = (y3 + y4) / 2;
		double x123 = (x12 + x23) / 2;
		double y123 = (y12 + y23) / 2;
		double x234 = (x23 + x34) / 2;
		double y234 = (y23 + y34) / 2;
		double x1234 = (x123 + x234) / 2;
		double y1234 = (y123 + y234) / 2;

		// Try to approximate the full cubic curve by a single straight line
		// ------------------
		double dx = x4 - x1;
		double dy = y4 - y1;

		double d2 = Math.abs(((x2 - x4) * dy - (y2 - y4) * dx));
		double d3 = Math.abs(((x3 - x4) * dy - (y3 - y4) * dx));
		double da1, da2, k;

		// DANGER
		// switch((int(d2 > curveCollinearityEpsilon ? 1 : 0) << 1) +
		// int(d3 > curveCollinearityEpsilon))
		switch (((d2 > this.curveCollinearityEpsilon ? 1 : 0) << 1)
				+ (d3 > this.curveCollinearityEpsilon ? 1 : 0)) {
		case 0:
			// All collinear OR p1==p4
			// ----------------------
			k = dx * dx + dy * dy;
			if (k == 0) {
				d2 = calcSqDistance(x1, y1, x2, y2);
				d3 = calcSqDistance(x4, y4, x3, y3);
			} else {
				k = 1 / k;
				da1 = x2 - x1;
				da2 = y2 - y1;
				d2 = k * (da1 * dx + da2 * dy);
				da1 = x3 - x1;
				da2 = y3 - y1;
				d3 = k * (da1 * dx + da2 * dy);
				if (d2 > 0 && d2 < 1 && d3 > 0 && d3 < 1) {
					// Simple collinear case, 1---2---3---4
					// We can leave just two endpoints
					return;
				}
				if (d2 <= 0)
					d2 = calcSqDistance(x2, y2, x1, y1);
				else if (d2 >= 1)
					d2 = calcSqDistance(x2, y2, x4, y4);
				else
					d2 = calcSqDistance(x2, y2, x1 + d2 * dx, y1 + d2 * dy);

				if (d3 <= 0)
					d3 = calcSqDistance(x3, y3, x1, y1);
				else if (d3 >= 1)
					d3 = calcSqDistance(x3, y3, x4, y4);
				else
					d3 = calcSqDistance(x3, y3, x1 + d3 * dx, y1 + d3 * dy);
			}
			if (d2 > d3) {
				if (d2 < this.distanceToleranceSquare) {
					this.points.add(new Point(x2, y2));
					return;
				}
			} else {
				if (d3 < this.distanceToleranceSquare) {
					this.points.add(new Point(x3, y3));
					return;
				}
			}
			break;

		case 1:
			// p1,p2,p4 are collinear, p3 is significant
			// ----------------------
			if (d3 * d3 <= this.distanceToleranceSquare * (dx * dx + dy * dy)) {
				if (this.angleTolerance < this.curveAngleToleranceEpsilon) {
					this.points.add(new Point(x23, y23));
					return;
				}

				// Angle Condition
				// ----------------------
				da1 = Math.abs(Math.atan2(y4 - y3, x4 - x3)
						- Math.atan2(y3 - y2, x3 - x2));
				if (da1 >= Math.PI)
					da1 = 2 * Math.PI - da1;

				if (da1 < this.angleTolerance) {
					this.points.add(new Point(x2, y2));
					this.points.add(new Point(x3, y3));
					return;
				}

				if (this.cuspLimit != 0.0) {
					if (da1 > this.cuspLimit) {
						this.points.add(new Point(x3, y3));
						return;
					}
				}
			}
			break;

		case 2:
			// p1,p3,p4 are collinear, p2 is significant
			// ----------------------
			if (d2 * d2 <= this.distanceToleranceSquare * (dx * dx + dy * dy)) {
				if (this.angleTolerance < this.curveAngleToleranceEpsilon) {
					this.points.add(new Point(x23, y23));
					return;
				}

				// Angle Condition
				// ----------------------
				da1 = Math.abs(Math.atan2(y3 - y2, x3 - x2)
						- Math.atan2(y2 - y1, x2 - x1));
				if (da1 >= Math.PI)
					da1 = 2 * Math.PI - da1;

				if (da1 < this.angleTolerance) {
					this.points.add(new Point(x2, y2));
					this.points.add(new Point(x3, y3));
					return;
				}

				if (this.cuspLimit != 0.0) {
					if (da1 > this.cuspLimit) {
						this.points.add(new Point(x2, y2));
						return;
					}
				}
			}
			break;

		case 3:
			// Regular case
			// -----------------
			if ((d2 + d3) * (d2 + d3) <= this.distanceToleranceSquare
					* (dx * dx + dy * dy)) {
				// If the curvature doesn't exceed the distance_tolerance value
				// we tend to finish subdivisions.
				// ----------------------
				if (this.angleTolerance < this.curveAngleToleranceEpsilon) {
					this.points.add(new Point(x23, y23));
					return;
				}

				// Angle & Cusp Condition
				// ----------------------
				k = Math.atan2(y3 - y2, x3 - x2);
				da1 = Math.abs(k - Math.atan2(y2 - y1, x2 - x1));
				da2 = Math.abs(Math.atan2(y4 - y3, x4 - x3) - k);
				if (da1 >= Math.PI)
					da1 = 2 * Math.PI - da1;
				if (da2 >= Math.PI)
					da2 = 2 * Math.PI - da2;

				if (da1 + da2 < this.angleTolerance) {
					// Finally we can stop the recursion
					// ----------------------
					this.points.add(new Point(x23, y23));
					return;
				}

				if (this.cuspLimit != 0.0) {
					if (da1 > this.cuspLimit) {
						this.points.add(new Point(x2, y2));
						return;
					}

					if (da2 > this.cuspLimit) {
						this.points.add(new Point(x3, y3));
						return;
					}
				}
			}
			break;
		}

		// Continue subdivision
		// ----------------------
		recursiveBezier(x1, y1, x12, y12, x123, y123, x1234, y1234, level + 1);
		recursiveBezier(x1234, y1234, x234, y234, x34, y34, x4, y4, level + 1);
	}

	private double calcSqDistance(double x1, double y1, double x2, double y2) {
		double dx = x2 - x1;
		double dy = y2 - y1;
		return dx * dx + dy * dy;
	}

}
