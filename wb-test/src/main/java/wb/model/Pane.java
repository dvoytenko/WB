package wb.model;

import java.util.Stack;

public abstract class Pane {
	
	private Stack<Transform> trStack = new Stack<Transform>();
	
	private Transform currentTransform;
	
	private Transform currentInvertTransform;
	
	public Pane(Transform defaultTransform) {
		this.trStack.push(defaultTransform);
		setCanvasTransform(defaultTransform);
	}

	public abstract void moveTo(Point p);

	public abstract void lineTo(Point p);

	public abstract double distance(Point startPoint, Point endPoint);

	public abstract void beginPath();

	public abstract void stroke();

	public abstract void arcTo(Point p, Point e, double r);
	
	public abstract void arc(Point c, double r, double sAngle, double eAngle, boolean counterclockwise);

	public abstract Point getCurrentPoint();

	public abstract Double getCurrentAngle();

	public abstract Canvas canvas();

	public void withTr(Transform transform, Runnable runnable) {
		
		final Transform parent = this.trStack.peek();
		
		final Transform appliedTr = new Transform(parent);
		appliedTr.multiply(transform);
		
		trStack.push(appliedTr);
		setCanvasTransform(appliedTr);
		
		runnable.run();
		
		trStack.pop();
		setCanvasTransform(parent);
	}

	private void setCanvasTransform(Transform tr) {
		this.currentTransform = tr;
		this.currentInvertTransform = new Transform(tr).invert();
		canvas().setTransform(tr.m[0], tr.m[1], tr.m[2], tr.m[3], tr.m[4], tr.m[5]);
	}

	public Point transformGlobalPointToLocal(Point p) {
		return this.currentTransform.transformPoint(p);
	}
	
	public Point transformLocalPointToGlobal(Point p) {
		return this.currentInvertTransform.transformPoint(p);
	}

	public Point toGlobalPoint(Point point) {
		// TODO Auto-generated method stub
		return null;
	}

}
