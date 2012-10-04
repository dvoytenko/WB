package com.antigrain.adaptivebezier.agg;

import java.util.ArrayList;
import java.util.List;

public class Curve3Div {

	public static void main(String[] args) {
		Curve3Div curve = new Curve3Div();

		curve.init(100, 100, 80, 20, 200, 200);

		System.out.println("Points: " + curve.points.size() + ": ");
		int index = 0;
		for (Point p : curve.points) {
			if (index == 0) {
				System.out.println("context.moveTo(" + p.x + ", " + p.y + ");");
			} else {
				System.out.println("context.lineTo(" + p.x + ", " + p.y + ");");
			}
			index++;
		}
	}

	private int curveRecursionLimit = 32;

	private double curveCollinearityEpsilon = 1e-30;

	private double curveAngleToleranceEpsilon = 0.01;

	private double approximationScale = 1.0;

	private double angleTolerance = 0.0;
	
	private double distanceToleranceSquare;

	private List<Point> points = new ArrayList<Point>();

	public void init(double x1, double y1, 
            double x2, double y2, 
            double x3, double y3) {
		this.points.clear();
		this.distanceToleranceSquare = 0.5 / this.approximationScale;
		this.distanceToleranceSquare *= this.distanceToleranceSquare;
		this.bezier(x1, y1, x2, y2, x3, y3);
	}
	
	public void reset() {
		this.points.clear();
	}

	public void bezier(double x1, double y1, double x2, double y2, double x3,
			double y3) {
        this.points.add(new Point(x1, y1));
        this.recursiveBezier(x1, y1, x2, y2, x3, y3, 0);
        this.points.add(new Point(x3, y3));
	}

	public void recursiveBezier(double x1, double y1, double x2, double y2,
			double x3, double y3, int level) {
		if (level > this.curveRecursionLimit) {
			return;
		}

        // Calculate all the mid-points of the line segments
        //----------------------
		double x12 = (x1 + x2) / 2;
		double y12 = (y1 + y2) / 2;
		double x23 = (x2 + x3) / 2;
		double y23 = (y2 + y3) / 2;
		double x123 = (x12 + x23) / 2;
		double y123 = (y12 + y23) / 2;

		double dx = x3 - x1;
		double dy = y3 - y1;
		double d = Math.abs(((x2 - x3) * dy - (y2 - y3) * dx));
		double da;

		if (d > this.curveCollinearityEpsilon) {
            // Regular case
            //-----------------
			if (d * d <= this.distanceToleranceSquare * (dx * dx + dy * dy)) {
                // If the curvature doesn't exceed the distance_tolerance value
                // we tend to finish subdivisions.
                //----------------------
				if (this.angleTolerance < this.curveAngleToleranceEpsilon) {
					this.points.add(new Point(x123, y123));
					return;
				}

                // Angle & Cusp Condition
                //----------------------
				da = Math.abs(Math.atan2(y3 - y2, x3 - x2)
						- Math.atan2(y2 - y1, x2 - x1));
				if (da >= Math.PI) {
					da = 2 * Math.PI - da;
				}

				if (da < this.angleTolerance) {
                    // Finally we can stop the recursion
                    //----------------------
                    this.points.add(new Point(x123, y123));
                    return;                 
                }
            }
		} else {
            // Collinear case
            //------------------
			da = dx * dx + dy * dy;
			if (da == 0) {
				d = this.calcSqDistance(x1, y1, x2, y2);
			} else {
				d = ((x2 - x1) * dx + (y2 - y1) * dy) / da;
				if (d > 0 && d < 1) {
					// Simple collinear case, 1---2---3
					// We can leave just two endpoints
					return;
				}
				if (d <= 0) {
					d = this.calcSqDistance(x2, y2, x1, y1);
				} else if (d >= 1) {
					d = this.calcSqDistance(x2, y2, x3, y3);
				} else {
					d = this.calcSqDistance(x2, y2, x1 + d * dx, y1 + d * dy);
				}
            }
			if (d < this.distanceToleranceSquare) {
				this.points.add(new Point(x2, y2));
				return;
			}
        }

        // Continue subdivision
        //----------------------
        this.recursiveBezier(x1, y1, x12, y12, x123, y123, level + 1); 
        this.recursiveBezier(x123, y123, x23, y23, x3, y3, level + 1); 
	}

	private double calcSqDistance(double x1, double y1, double x2, double y2) {
		double dx = x2 - x1;
		double dy = y2 - y1;
		return dx * dx + dy * dy;
	}

}
