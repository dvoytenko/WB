package wb.model;

/**
 * http://dev.w3.org/html5/2dcontext/#the-canvas-state
 */
class CanvasState {

	/*
	 * Drawing states consist of:
	 * - The current transformation matrix.
	 * - The current clipping region.
	 * - The current values of the following attributes: strokeStyle, 
	 * 		fillStyle, globalAlpha, lineWidth, lineCap, lineJoin, 
	 * 		miterLimit, shadowOffsetX, shadowOffsetY, shadowBlur, 
	 * 		shadowColor, globalCompositeOperation, font, 
	 * 		textAlign, textBaseline
	 */
	
	private Transform transform;

	private Transform transformInv;
	
	private double lineWidth;
	
	public CanvasState() {
		setTransform(new Transform());
	}

	public CanvasState(CanvasState other) {
		setTransform(new Transform(other.transform));
		this.lineWidth = other.lineWidth;
	}

	public void setTransform(Transform transform) {
		this.transform = transform;
		this.transformInv = new Transform(transform).invert();
	}
	
	public Transform getTransform() {
		return transform;
	}
	
	public Transform getTransformInv() {
		return transformInv;
	}
	
	public void setLineWidth(double lineWidth) {
		this.lineWidth = lineWidth;
	}
	
	public double getLineWidth() {
		return lineWidth;
	}

	public Point toGlobalPoint(Point p) {
		return this.transform.transformPoint(p);
	}

	public Point toLocalPoint(Point p) {
		return this.transformInv.transformPoint(p);
	}
	
}
