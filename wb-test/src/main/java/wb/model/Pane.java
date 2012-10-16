package wb.model;

import java.util.Stack;

public class Pane {
	
	private final boolean trace = false;
	
	private Canvas canvas;
	
	private Stack<Transform> trStack = new Stack<Transform>();

	private Transform defaultTransform;

	@SuppressWarnings("unused")
	private Transform defaultTransformInv;

	private Transform currentTransform;

	private Transform currentTransformInv;

	private double lineWidth;

	private Point currentPoint;

	private Point pathStartPoint;

	private Point cursorPosition;	
	
	public Pane(Canvas canvas, Transform defaultTransform) {
		this.canvas = canvas;
		
    	Transform tr;
    	if (defaultTransform != null) {
    		tr = defaultTransform;
    	} else {
    		tr = new Transform();
    	}
    	this.defaultTransform = tr;
    	this.defaultTransformInv = new Transform(tr).invert();
		setCanvasTransform(null);
		
		
//		this.trStack.push(defaultTransform);
//		setCanvasTransform(defaultTransform);
	}

    public void updateDefaultTransform(Transform defaultTransform) {
    	this.defaultTransform = defaultTransform;
    	this.defaultTransformInv = new Transform(defaultTransform).invert();
    	
		if (this.trStack.size() == 0) {
			setCanvasTransform(null);
		} else {
			Transform tr = this.trStack.get(this.trStack.size() - 1);
			setCanvasTransform(tr);
		}
    }
	
	private void setCanvasTransform(Transform tr) {
		if (tr == null) {
			tr = new Transform();
		}
		
		this.currentTransform = tr;
		this.currentTransformInv = new Transform(tr).invert();
		
		Transform pixelTr = new Transform(this.defaultTransform);
		pixelTr.multiply(tr);
		
		if (this.trace) {
			log("context.setTransform(" 
					+ pixelTr.m[0] + ", " 
					+ pixelTr.m[1] + ", " 
					+ pixelTr.m[2] + ", " 
					+ pixelTr.m[3] + ", " 
					+ pixelTr.m[4] + ", " 
					+ pixelTr.m[5] + ")");
		}
		this.canvas.setTransform(pixelTr.m[0], pixelTr.m[1], pixelTr.m[2], 
				pixelTr.m[3], pixelTr.m[4], pixelTr.m[5]);
		
		// default line width: 2.5 mm
		Point w0 = this.currentTransformInv.transformPoint(0, 0);
		Point w2 = this.currentTransformInv.transformPoint(2, 2);
		this.lineWidth = Geom.distance(w0, w2);
		// this.lineWidth = Math.sqrt(w*w*2);
		// this.lineWidth = this.currentTransform.m[0]/this.defaultTransform.m[0]*2.5;
		// this.context.lineWidth = this.lineWidth;
	}

	private void log(String s) {
		System.out.println("pane: " + s);
	}

	public void withTr(Transform transform, Runnable runnable) {
		
		if (transform == null) {
			runnable.run();
			return;
		}
		
		Transform parent = null;
		if (this.trStack.size() > 0) {
			parent = this.trStack.get(this.trStack.size() - 1);
		}

		Transform tr;
		if (parent != null) {
			tr = new Transform(parent);
			tr.multiply(transform);
		} else {
			tr = transform;
		}

		this.trStack.push(tr);
		setCanvasTransform(tr);
		
		runnable.run();
		
		setCanvasTransform(parent);
		this.trStack.pop();
	}

	public Point toGlobalPoint(double x, double y) {
		return this.currentTransform.transformPoint(x, y);
	}
	
	public Point toGlobalPoint(Point p) {
		return this.currentTransform.transformPoint(p);
	}
	
	public Point toLocalPoint(Point p) {
		return this.currentTransformInv.transformPoint(p.x, p.y);
	}
	
	private void update(Point p, boolean global, boolean pathStart) {
		// console.log('update to ' + JSON.stringify(p) + ' ' + global);
		
		if (p == null) {
			this.currentPoint = null;
		} else {
			if (global) {
				this.currentPoint = p;
			} else {
				this.currentPoint = toGlobalPoint(p);
			}
		}
		
		if (pathStart) {
			this.pathStartPoint = this.currentPoint;
		}
	}

	public Point getCurrentPoint() {
		return getCurrentPoint(false);
	}
	
	public Point getCurrentPoint(boolean global) {
		Point p = this.currentPoint;
		if (p == null) {
			p = this.cursorPosition;
		}
		if (p == null) {
			p = new Point(0, 0);
		}
		
		if (global) {
			return p;
		}
		
		return toLocalPoint(p);
	}

	public double distanceGlobal(Point p1, Point p2) {
		Point gp1 = this.toGlobalPoint(p1);
		Point gp2 = this.toGlobalPoint(p2);
		return Geom.distance(gp1, gp2);
	}
	
	public double globalLength(double length) {
		double x1 = this.toGlobalPoint(new Point(0, 0)).x;
		double x2 = this.toGlobalPoint(new Point(length,0)).x;
		return Math.abs(x2 - x1);
	}
	
	public Point getPathStartPoint() {
		if (this.pathStartPoint == null) {
			return null;
		}
		return this.toLocalPoint(this.pathStartPoint);
	}
	
    public void beginPath() {
		if (this.trace) {
			log("context.beginPath()");
		}
    	this.canvas.beginPath();
    	// TODO can enable this once other cursor operations are supported
		// this._update(null, false, false);
    }
    
	public void moveTo(Point p) {
		if (this.trace) {
			log("context.moveTo(" + p.x + ", " + p.y + ")");
		}
		this.canvas.moveTo(p.x, p.y);
		this.update(p, false, true);
	}
    
	public void lineTo(Point p) {
		if (this.trace) {
			log("context.lineTo(" + p.x + ", " + p.y + ")");
		}
		this.canvas.lineTo(p.x, p.y);
		this.update(p, false, false);
	}
	
	public void arc(Point c, double r, double sAngle, double eAngle, boolean counterclockwise) {
		if (this.trace) {
			log("context.arc(" + c.x 
					+ ", " + c.y 
					+ ", " + r 
					+ ", " + sAngle 
					+ ", " + eAngle 
					+ ", " + counterclockwise 
					+ ")");
		}
		this.canvas.arc(c.x, c.y, r, sAngle, eAngle, counterclockwise);
		// last point
		Point p = new Point(c.x + Math.cos(eAngle) * r, c.y + Math.sin(eAngle) * r);
		this.update(p, false, false);
	}
	
	public void stroke() {
		if (this.trace) {
			log("context.lineWidth = " + this.lineWidth);
			log("context.stroke()");
		}
		this.canvas.lineWidth(this.lineWidth);
		this.canvas.stroke();
	}
	
	public void drawImage(Object img, double x, double y, double width, double height) {
		this.canvas.drawImage(img, x, y, width, height);
	}
	
}
