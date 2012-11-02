package wb.model;

import java.util.Stack;

public class MeasuringCanvas implements Canvas {

	private Point current;

	private CanvasState state = new CanvasState();
	
	private Stack<CanvasState> stateStack = new Stack<CanvasState>();
	
	private Bounds bounds = new Bounds(null, null);
	
	public MeasuringCanvas() {
	}

	public Bounds getBounds() {
		if (this.bounds.topleft == null) {
			return null;
		}
		return new Bounds(
				new Point(Math.floor(this.bounds.topleft.x),
						Math.floor(this.bounds.topleft.y)),
				new Point(Math.ceil(this.bounds.bottomright.x),
						Math.ceil(this.bounds.bottomright.y))
				);
	}

	private void capture(Point p) {
		
		Point gp = toGlobalPoint(p);
		
		if (this.bounds.topleft == null) {
			this.bounds.topleft = new Point(gp.x, gp.y);
		} else {
			this.bounds.topleft.x = Math.min(this.bounds.topleft.x, gp.x);
			this.bounds.topleft.y = Math.min(this.bounds.topleft.y, gp.y);
		}

		if (this.bounds.bottomright == null) {
			this.bounds.bottomright = new Point(gp.x, gp.y);
		} else {
			this.bounds.bottomright.x = Math.max(this.bounds.bottomright.x, gp.x);
			this.bounds.bottomright.y = Math.max(this.bounds.bottomright.y, gp.y);
		}
	}

	@Override
	public void setTransform(double a, double b, double c, double d, double e,
			double f) {
		this.state.setTransform(new Transform(a, b, c, d, e, f));
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
		if (this.current == null) {
			return null;
		}
		return toLocalPoint(this.current);
	}
	
	@Override
	public void lineWidth(double lineWidth) {
		this.state.setLineWidth(lineWidth);
	}

	@Override
	public void beginPath() {
	}

	@Override
	public void moveTo(double x, double y) {
		Point p = new Point(x, y);
		current(p);
		capture(p);
	}
	
	@Override
	public void lineTo(double x, double y) {
		Point cp = current();
		if (cp == null) {
			cp = new Point(0, 0);
		}
		capture(cp);
		Point ep = new Point(x, y);
		current(ep);
		capture(ep);
	}

	@Override
	public void arc(double cx, double cy, double r, double sAngle,
			double eAngle, boolean counterclockwise) {
		Point cp = current();
		if (cp == null) {
			cp = new Point(0, 0);
		}
		capture(cp);

		// TODO: doesn't always work!
		// TRICK: r is always maximum of (rx,ry) thus this should be correct
		capture(new Point(cx - r, cy - r));
		capture(new Point(cx + r, cy + r));
	}

	@Override
	public void drawImage(Object img, double x, double y, double width,
			double height) {
		capture(new Point(x, y));
		capture(new Point(x + width, y + height));
	}

	@Override
	public void stroke() {
	}

	@Override
	public void save() {
		this.stateStack.push(this.state);
		this.state = new CanvasState(this.state);
	}

	@Override
	public void restore() {
		this.state = this.stateStack.pop();
	}

}
