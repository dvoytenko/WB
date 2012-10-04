package wb.model;

import java.util.Arrays;

/**
 * See http://www.w3.org/TR/SVG/coords.html#TransformAttribute
 * See https://github.com/simonsarris/Canvas-tutorials/blob/master/transform.js
 */
public class Transform {
	
	public final double[] m = new double[6];

	public Transform() {
		reset();
	}
	
	public Transform(Transform other) {
		set(other.m[0], other.m[1], other.m[2], other.m[3], other.m[4], other.m[5]);
	}

	public Transform set(double m0, double m1, double m2, double m3, double m4, double m5) {
		this.m[0] = m0;
		this.m[1] = m1;
		this.m[2] = m2;
		this.m[3] = m3;
		this.m[4] = m4;
		this.m[5] = m5;
		return this;
	}

	public Transform reset() {
		return set(1, 0, 0, 1, 0, 0);
	}

	public Transform multiply(Transform matrix) {

		double m11 = this.m[0] * matrix.m[0] + this.m[2] * matrix.m[1];
		double m12 = this.m[1] * matrix.m[0] + this.m[3] * matrix.m[1];

		double m21 = this.m[0] * matrix.m[2] + this.m[2] * matrix.m[3];
		double m22 = this.m[1] * matrix.m[2] + this.m[3] * matrix.m[3];

		double dx = this.m[0] * matrix.m[4] + this.m[2] * matrix.m[5] + this.m[4];
		double dy = this.m[1] * matrix.m[4] + this.m[3] * matrix.m[5] + this.m[5];

		return set(m11, m12, m21, m22, dx, dy);
	}

	public Transform invert() {
		double d = 1 / (this.m[0] * this.m[3] - this.m[1] * this.m[2]);
		double m0 = this.m[3] * d;
		double m1 = -this.m[1] * d;
		double m2 = -this.m[2] * d;
		double m3 = this.m[0] * d;
		double m4 = d * (this.m[2] * this.m[5] - this.m[3] * this.m[4]);
		double m5 = d * (this.m[1] * this.m[4] - this.m[0] * this.m[5]);
		return set(m0, m1, m2, m3, m4, m5);
	}

	public Transform translate(double x, double y) {
		/*
		 	/a c e\
		 	|b d f|
		 	\0 0 1/
		 */
		this.m[4] += this.m[0] * x + this.m[2] * y;
		this.m[5] += this.m[1] * x + this.m[3] * y;
		return this;
	}

	public Transform scale(double sx, double sy) {
		this.m[0] *= sx;
		this.m[1] *= sx;
		this.m[2] *= sy;
		this.m[3] *= sy;
		return this;
	}

	public Transform rotate(double rad) {
		double c = Math.cos(rad);
		double s = Math.sin(rad);
		double m11 = this.m[0] * c + this.m[2] * s;
		double m12 = this.m[1] * c + this.m[3] * s;
		double m21 = this.m[0] * -s + this.m[2] * c;
		double m22 = this.m[1] * -s + this.m[3] * c;
		this.m[0] = m11;
		this.m[1] = m12;
		this.m[2] = m21;
		this.m[3] = m22;
		return this;
	}

	public Transform rotate(double rad, double cx, double cy) {
		// translate(<cx>, <cy>) rotate(<rotate-angle>) translate(-<cx>, -<cy>)
		return translate(cx, cy).rotate(rad).translate(-cx, -cy);
	}

	/**
	 * http://www.w3.org/TR/SVG/coords.html#SkewXDefined
	 * http://www.w3.org/TR/SVG/coords.html#SkewYDefined
	 */
	public Transform skew(double radX, double radY) {
		return shear(Math.tan(radX), Math.tan(radY));
	}

	public Transform shear(double shx, double shy) {
		
	    double a = this.m[0];
	    double b = this.m[1];
	    double c = this.m[2];
	    double d = this.m[3];
	    
	    this.m[0] = a + c * shy;
	    this.m[2] = a * shx + c;
	    this.m[1] = b + d * shy;
	    this.m[3] = b * shx + d;
		return this;
	}

	public Point transformPoint(Point p) {
		double tx = p.x * this.m[0] + p.y * this.m[2] + this.m[4];
		double ty = p.x * this.m[1] + p.y * this.m[3] + this.m[5];
		return new Point(tx, ty);
	}

	@Override
	public String toString() {
		return "Transform [m=" + Arrays.toString(m) + "]";
	}

}
