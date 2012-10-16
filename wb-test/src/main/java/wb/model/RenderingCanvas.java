package wb.model;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class RenderingCanvas implements Canvas {

	private Graphics2D graphics;
	
	private Point current;
	
	private List<Op> path = new ArrayList<Op>();

	private CanvasState state = new CanvasState();
	
	private Stack<CanvasState> stateStack = new Stack<CanvasState>();

	public RenderingCanvas(Graphics2D graphics) {
		this.graphics = graphics;
		
		this.graphics.setPaint(Color.black);
		
		// Anti-alias!
		this.graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, 
		        RenderingHints.VALUE_ANTIALIAS_ON);
	}

	private void applyTransform(Transform tr) {
		this.graphics.setTransform(new AffineTransform(tr.m.clone()));
	}

	@Override
	public void setTransform(double a, double b, double c, double d, double e,
			double f) {
		Transform tr = new Transform(a, b, c, d, e, f);
		this.state.setTransform(tr);
		applyTransform(tr);
	}

	private Point toGlobalPoint(Point p) {
		return this.state.toGlobalPoint(p);
	}

	private Point toLocalPoint(Point p) {
		return this.state.toLocalPoint(p);
	}

	private void current(Point point) {
		this.current = toGlobalPoint(point);
	}

	private Point current() {
		return toLocalPoint(this.current);
	}

	@Override
	public void beginPath() {
		this.path.clear();
	}

	@Override
	public void moveTo(double x, double y) {
		current(new Point(x, y));
	}
	
	private int di(double x) {
		return (int) Math.round(x);
	}

	@Override
	public void lineWidth(double lineWidth) {
		this.state.setLineWidth(lineWidth);
	}

	@Override
	public void lineTo(double x, double y) {
		Point cp = current();
		if (cp == null) {
			cp = new Point(0, 0);
		}
		int x1 = di(cp.x);
		int y1 = di(cp.y);
		int x2 = di(x);
		int y2 = di(y);
		this.path.add(new Line(x1, y1, x2, y2));
		current(new Point(x, y));
	}

	@Override
	public void arc(double cx, double cy, double r, double sAngle,
			double eAngle, boolean counterclockwise) {
		Point cp = current();
		if (cp == null) {
			cp = new Point(0, 0);
		}
		
		// c.x + Math.cos(eAngle) * r, c.y + Math.sin(eAngle) * r
		int x = di(cx - r);
		int y = di(cy - r);
		int width = di(r * 2);
		int height = di(r * 2);
		int startAngle = di(sAngle);
		double da = eAngle - sAngle;
		if (da > 0 && counterclockwise) {
			da = da - Geom.PI_2;
		} else if (da < 0 && !counterclockwise) {
			da = Geom.PI_2 + da;
		}
		int arcAngle = di(da);
		
		this.graphics.drawArc(x, y, width, height, startAngle, arcAngle);
	}

	private Stroke createStroke() {
		// di(this.state.getLineWidth())
		return new BasicStroke(2);
	}

	@Override
	public void stroke() {
		this.graphics.setStroke(createStroke());
		for (Op op : this.path) {
			op.stroke();
		}
		this.path.clear();
	}

	@Override
	public void save() {
		this.stateStack.push(this.state);
		this.state = new CanvasState(this.state);
	}

	@Override
	public void restore() {
		this.state = this.stateStack.pop();
		applyTransform(this.state.getTransform());
	}

	@Override
	public void drawImage(Object img, double x, double y, double width,
			double height) {
		throw new RuntimeException("not implemented");
	}
	
	private abstract class Op {
		
		public abstract void stroke();
		
	}

	private class Line extends Op {
		
		private int x1;
		
		private int y1;
		
		private int x2;
		
		private int y2;
		
		public Line(int x1, int y1, int x2, int y2) {
			this.x1 = x1;
			this.y1 = y1;
			this.x2 = x2;
			this.y2 = y2;
		}

		@Override
		public void stroke() {
			graphics.drawLine(x1, y1, x2, y2);
		}
		
	}

}
