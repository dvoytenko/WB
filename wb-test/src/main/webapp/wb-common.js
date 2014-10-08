

/**
 * @return {!Point}
 */
WB.point = function(x, y) {
	return {x: x, y: y};
};


/**
 * @return {!Bounds}
 */
WB.bounds = function(x1, y1, x2, y2) {
	return {
		topleft: WB.point(Math.min(x1, x2), Math.min(y1, y2)),
		bottomright: WB.point(Math.max(x1, x2), Math.max(y1, y2))};
};


/**
 * 
 */
WB.GeomProto = WB.Class.extend('GeomProto', {
	
	PI: Math.PI,
	
	PI_HALF: Math.PI/2,

	PI_1_HALF: Math.PI + Math.PI/2,

	PI_2: Math.PI * 2,
	
	/**
	 * @return {!Point}
	 */
	movePoint: function(p, dx, dy) {
		return {x: p.x + dx, y: p.y + dy};
	},

	/**
	 * @return {!Point}
	 */
	addPoint: function(p1, p2, dir) {
		if (!dir) {
			dir = 1;
		}
		return {x: p1.x + dir * p2.x, y: p1.y + dir * p2.y};
	},

	pointsEqual: function(p1, p2, eps) {
		if (!eps) {
			eps = 0;
		}
		return Math.abs(p1.x - p2.x) <= eps && Math.abs(p1.y - p2.y) <= eps;
	},

	/**
	 * Angle b/w two points
	 */
	angle: function(p1, p2) {
		return Math.atan2(p1.y - p2.y, p1.x - p2.x);
	},
	
	/**
	 * Sign operator
	 */
	sign: function(d) {
		if (d == null || d == undefined) {
			return null;
		}
		return d < 0 ? -1 : d > 0 ? 1 : 0;
	},

	/**
	 * 
	 */
	vectorEndPoint: function(start, distance, angle) {
		var dx = Math.cos(angle) * distance;
		var dy = Math.sin(angle) * distance;
		return this.movePoint(start, dx, dy);
	},
	
	distance: function(p1, p2) {
		var dx = p1.x - p2.x;
		var dy = p1.y - p2.y;
		return Math.sqrt(dx * dx + dy * dy);
	},

	squareDistance: function(p1, p2) {
		var dx = p1.x - p2.x;
		var dy = p1.y - p2.y;
		return dx * dx + dy * dy;
	},
	
	rad: function(degree) {
		return this.PI_2 * degree / 360; 
	},
	
	lineY: function(line, x) {
		// start/end
		var x1 = line.start.x;
		var x2 = line.end.x;
		var y1 = line.start.y;
		var y2 = line.end.y;
		if (x < Math.min(x1, x2) || x > Math.max(x1, x2)) {
			// x outside of the [x1, x2] range
			return null;
		}
		if (x == x1) {
			// touches the start or a vertical line
			return y1;
		}
		if (x == x2) {
			// touches the end or a vertical line
			return y2;
		}
		if (x1 == x2) {
			// vertical line, but not touching
			return null;
		}
		var a = (y2 - y1) / (x2 - x1);
		var b = y1 - a * x1;
		return a * x + b;
	},
	
	boundsOverlap: function(b1, b2) {
		// topleft/bottomright
		return (b1.topleft.x <= b2.bottomright.x && b2.topleft.x <= b1.bottomright.x)
			&& (b1.topleft.y <= b2.bottomright.y && b2.topleft.y <= b1.bottomright.y);
	},
	
	includeBounds: function(b1, b2) {
		// topleft/bottomright
		return {
			topleft: {x: Math.min(b1.topleft.x, b2.topleft.x), y: Math.min(b1.topleft.y, b2.topleft.y)},
			bottomright: {x: Math.max(b1.bottomright.x, b2.bottomright.x), y: Math.max(b1.bottomright.y, b2.bottomright.y)},
		};
	},
	
	intersectBounds: function(b1, b2) {
		if (!this.boundsOverlap(b1, b2)) {
			return null;
		}
		return WB.bounds(
				Math.max(b1.topleft.x, b2.topleft.x),
				Math.max(b1.topleft.y, b2.topleft.y),
				Math.min(b1.bottomright.x, b2.bottomright.x),
				Math.min(b1.bottomright.y, b2.bottomright.y));
	},
	
	moveBounds: function(b, dx, dy) {
		// topleft/bottomright
		return {
			topleft: {x: b.topleft.x + dx, y: b.topleft.y + dy},
			bottomright: {x: b.bottomright.x + dx, y: b.bottomright.y + dy},
		};
	},
	
	growBounds: function(b, d) {
		return {
			topleft: {x: b.topleft.x - d, y: b.topleft.y - d},
			bottomright: {x: b.bottomright.x + d, y: b.bottomright.y + d}
		};
	},
	
	pointWithinBounds: function(b, p) {
		return (p.x >= b.topleft.x && p.x <= b.bottomright.x &&
				p.y >= b.topleft.y && p.y <= b.bottomright.y);
	}
	
});

WB.Geom = new WB.GeomProto();


/**
 * Most methods return 'this'
 */
WB.Transform = WB.Class.extend('Transform', {
	
	m: null,
	
    init: function(opts) {
    	if (opts && opts.m) {
    		this.m = opts.m.slice(0);
    	} else if (opts && opts.transform) {
    		this.m = opts.transform.m.slice(0);
    	} else {
    		this.reset();
    	}
    },
    
    reset: function() {
    	this.m = [1, 0, 0, 1, 0, 0];
    	return this;
    },
    
    multiply: function(matrix) {
		var m11 = this.m[0] * matrix.m[0] + this.m[2] * matrix.m[1];
		var m12 = this.m[1] * matrix.m[0] + this.m[3] * matrix.m[1];
		
		var m21 = this.m[0] * matrix.m[2] + this.m[2] * matrix.m[3];
		var m22 = this.m[1] * matrix.m[2] + this.m[3] * matrix.m[3];
		
		var dx = this.m[0] * matrix.m[4] + this.m[2] * matrix.m[5] + this.m[4];
		var dy = this.m[1] * matrix.m[4] + this.m[3] * matrix.m[5] + this.m[5];
		
		this.m[0] = m11;
		this.m[1] = m12;
		this.m[2] = m21;
		this.m[3] = m22;
		this.m[4] = dx;
		this.m[5] = dy;
    	return this;
    },
    
    invert: function() {
		var d = 1 / (this.m[0] * this.m[3] - this.m[1] * this.m[2]);
		var m0 = this.m[3] * d;
		var m1 = -this.m[1] * d;
		var m2 = -this.m[2] * d;
		var m3 = this.m[0] * d;
		var m4 = d * (this.m[2] * this.m[5] - this.m[3] * this.m[4]);
		var m5 = d * (this.m[1] * this.m[4] - this.m[0] * this.m[5]);
		  
		this.m[0] = m0;
		this.m[1] = m1;
		this.m[2] = m2;
		this.m[3] = m3;
		this.m[4] = m4;
		this.m[5] = m5;
    	return this;
    },
    
    translate: function(x, y) {
    	this.m[4] += this.m[0] * x + this.m[2] * y;
    	this.m[5] += this.m[1] * x + this.m[3] * y;
    	return this;
    },
    
    scale: function(sx, sy) {
		this.m[0] *= sx;
		this.m[1] *= sx;
		this.m[2] *= sy;
		this.m[3] *= sy;
    	return this;
    },

    rotate: function(rad, cx, cy) {
    	if (!cx && !cy) {
    		var c = Math.cos(rad);
    		var s = Math.sin(rad);
    		var m11 = this.m[0] * c + this.m[2] * s;
    		var m12 = this.m[1] * c + this.m[3] * s;
    		var m21 = this.m[0] * -s + this.m[2] * c;
    		var m22 = this.m[1] * -s + this.m[3] * c;
    		  
    		this.m[0] = m11;
    		this.m[1] = m12;
    		this.m[2] = m21;
    		this.m[3] = m22;
    	} else {
    		// translate(<cx>, <cy>) rotate(<rotate-angle>) translate(-<cx>, -<cy>)
    		return this.translate(cx, cy).rotate(rad).translate(-cx, -cy);
    	}
    	return this;
    },

	skew: function(radX, radY) {
		return this.shear(Math.tan(radX), Math.tan(radY));
	},

	shear: function(shx, shy) {
		
	    var a = this.m[0];
	    var b = this.m[1];
	    var c = this.m[2];
	    var d = this.m[3];
	    
	    this.m[0] = a + c * shy;
	    this.m[2] = a * shx + c;
	    this.m[1] = b + d * shy;
	    this.m[3] = b * shx + d;
		return this;
	},
	
    transformPoint: function(px, py) {
		var x = px;
		var y = py;
		px = x * this.m[0] + y * this.m[2] + this.m[4];
		py = x * this.m[1] + y * this.m[3] + this.m[5];
		return {x: px, y: py};
    }
	
});


/**
 * 
 */
WB.Pane = WB.Class.extend('Pane', {
	
	canvas: null,
	
	context: null,
	
	zoomFactor: 1,
	
    init: function(opts) {
    	this.trace = opts && opts.trace || false;
    	
    	if (opts && opts._desc) {
    		this._desc = opts._desc;
    	}
    	
    	if (opts && opts.canvas) {
    		this.canvas = opts.canvas;
    	}
    	if (opts && opts.context) {
    		this.context = opts.context;
    	}
    	if (!this.context) {
    		this.context = this.canvas.getContext('2d');
    	}
    	
    	if (opts && opts.strokeStyle) {
    		this.context.strokeStyle = opts.strokeStyle;
    	}
    	
    	this._trStack = [];
    	
    	var tr;
    	if (opts && opts.defaultTransform) {
    		tr = opts.defaultTransform;
    	} else {
    		tr = new WB.Transform();
    	}
    	this.defaultTransform = tr;
    	this.defaultTransformInv = new WB.Transform(tr).invert();
		this._setCanvasTransform(null);
		
		this.rendering = true;
    },
    
    setZoomFactor: function(zoomFactor) {
    	this.zoomFactor = zoomFactor;
    },
    
    updateDefaultTransform: function(defaultTransform) {
    	this.defaultTransform = defaultTransform;
    	this.defaultTransformInv = new WB.Transform(defaultTransform).invert();
    	
		if (this._trStack.length == 0) {
			this._setCanvasTransform(null);
		} else {
			var tr = this._trStack[this._trStack.length - 1];
			this._setCanvasTransform(tr);
		}
    },
    
	withTr: function(transform, runnable) {

		if (!transform) {
			runnable();
			return;
		}
		
		var parent = null;
		if (this._trStack.length) {
			parent = this._trStack[this._trStack.length - 1];
		}

		var tr;
		if (parent) {
			tr = new WB.Transform({transform: parent});
			tr.multiply(transform);
		} else {
			tr = transform;
		}

		this._trStack.push(tr);
		this._setCanvasTransform(tr);
		
		runnable(this);
		
		this._setCanvasTransform(parent);
		this._trStack.pop();
	},
	
	_setCanvasTransform: function(tr) {
		if (!tr) {
			tr = new WB.Transform();
		}
		
		if (!tr.m[0] && tr.m[0] != 0) {
			throw "invalid matrix: " + JSON.stringify(tr.m);
		}
		
		this.currentTransform = tr;
		this.currentTransformInv = new WB.Transform(tr).invert();
		
		var pixelTr = new WB.Transform({transform: this.defaultTransform});
		pixelTr.multiply(tr);
		
		if (this.trace) {
			console.log('context.setTransform(' 
					+ pixelTr.m[0] + ', ' 
					+ pixelTr.m[1] + ', ' 
					+ pixelTr.m[2] + ', ' 
					+ pixelTr.m[3] + ', ' 
					+ pixelTr.m[4] + ', ' 
					+ pixelTr.m[5] + ')');
		}
		this.context.setTransform(pixelTr.m[0], pixelTr.m[1], pixelTr.m[2], 
				pixelTr.m[3], pixelTr.m[4], pixelTr.m[5]);
		
		// default line width: 2.5 mm
		var w0 = this.currentTransformInv.transformPoint(0, 0);
		var w2 = this.currentTransformInv.transformPoint(2, 2);
		this.lineWidth = WB.Geom.distance(w0, w2)/this.zoomFactor;
		// this.lineWidth = Math.sqrt(w*w*2);
		// this.lineWidth = this.currentTransform.m[0]/this.defaultTransform.m[0]*2.5;
		// this.context.lineWidth = this.lineWidth;
	},
	
	_clearCanvas: function() {
		if (this.trace) {
			console.log('context.clearRect(0, 0, ' 
					+ this.canvas.width + ', ' 
					+ this.canvas.height + ')');
		}
		this.context.save();
		this.context.setTransform(1, 0, 0, 1, 0, 0);
		if (this.rendering) {
			this.context.clearRect(0, 0, this.canvas.width, this.canvas.height);
		}
		this.context.restore();
	},
	
	captureBounds: function(allowRendering) {
		this.rendering = allowRendering;
		this.capturer = {};
	},
	
	endCaptureBounds: function() {
		this.rendering = true;
		var bounds = this.capturer;
		// console.log('captured end: ' + JSON.stringify(bounds));
		if (bounds && !bounds.topleft) {
			bounds = null;
		}
		this.capturer = null;
		return bounds;
	},
	
	_capture: function(ax1, ay1, ax2, ay2) {
		if (!this.capturer) {
			return;
		}
		
//		console.log('capture: ' + JSON.stringify([ax1, ay1, ax2, ay2]));
		
		var p1 = this.toGlobalPointXY(ax1, ay1);
		var p2 = this.toGlobalPointXY(ax2, ay2);

		var x1 = Math.min(p1.x, p2.x);
		var x2 = Math.max(p1.x, p2.x);
		var y1 = Math.min(p1.y, p2.y);
		var y2 = Math.max(p1.y, p2.y);
		
		if (!this.capturer.topleft) {
			this.capturer.topleft = {x: x1, y: y1};
		} else {
			this.capturer.topleft.x = Math.min(this.capturer.topleft.x, x1);
			this.capturer.topleft.y = Math.min(this.capturer.topleft.y, y1);
		}

		if (!this.capturer.bottomright) {
			this.capturer.bottomright = {x: x2, y: y2};
		} else {
			this.capturer.bottomright.x = Math.max(this.capturer.bottomright.x, x2);
			this.capturer.bottomright.y = Math.max(this.capturer.bottomright.y, y2);
		}

//		console.log('-> captured: ' + JSON.stringify([
//		    this.capturer.topleft.x, 
//		    this.capturer.topleft.y, 
//		    this.capturer.bottomright.x, 
//		    this.capturer.bottomright.y]));
	},
	
	globalBounds: function() {
		return {
			topleft: this.defaultTransformInv.transformPoint(0, 0),
			bottomright: this.defaultTransformInv.transformPoint(this.canvas.width, this.canvas.height)
		};
	},
	
	setCursorPositionGlobal: function(p) {
		this.cursorPosition = p;
	},
	
	toGlobalPointXY: function(x, y) {
		return this.currentTransform.transformPoint(x, y);
	},
	
	toGlobalPoint: function(p) {
		if (!p) {
			return null;
		}
		return this.currentTransform.transformPoint(p.x, p.y);
	},
	
	toGlobalBounds: function(b) {
		if (!b) {
			return null;
		}
		var p1 = this.toGlobalPoint(b.topleft);
		var p2 = this.toGlobalPoint(b.bottomright);
		var x1 = Math.min(p1.x, p2.x);
		var x2 = Math.max(p1.x, p2.x);
		var y1 = Math.min(p1.y, p2.y);
		var y2 = Math.max(p1.y, p2.y);
		return {
			topleft: {x: x1, y: y1},
			bottomright: {x: x2, y: y2}
		};
	},
	
	toLocalPoint: function(p) {
		if (!p) {
			return null;
		}
		return this.currentTransformInv.transformPoint(p.x, p.y);
	},

	toLocalBounds: function(b) {
		if (!b) {
			return null;
		}
		var p1 = this.toLocalPoint(b.topleft);
		var p2 = this.toLocalPoint(b.bottomright);
		var x1 = Math.min(p1.x, p2.x);
		var x2 = Math.max(p1.x, p2.x);
		var y1 = Math.min(p1.y, p2.y);
		var y2 = Math.max(p1.y, p2.y);
		return {
			topleft: {x: x1, y: y1},
			bottomright: {x: x2, y: y2}
		};
	},
	
	_update: function(p, global, pathStart) {
		// console.log('update to ' + JSON.stringify(p) + ' ' + global);
		
		if (!p) {
			this._currentPoint = null;
		} else {
			if (global) {
				this._currentPoint = p;
			} else {
				this._currentPoint = this.toGlobalPoint(p);
			}
		}
		
		if (pathStart) {
			this.pathStartPoint = this._currentPoint;
		}
			
		if (this.trace) {
			console.log('cp: ' + JSON.stringify(this._currentPoint));
		}
	},
	
	getCurrentPoint: function(global) {
		var p = this._currentPoint;
		if (!p) {
			p = this.cursorPosition;
		}
		if (!p) {
			p = {x: 0, y: 0};
		}
		
		if (global) {
			return p;
		}
		
		return this.toLocalPoint(p);
	},
	
	/**
	 * Points are specified in the local system. 
	 * They will be converted to the global system and distance calculated
	 * usng Geom.distance.
	 */
	distanceGlobal: function(p1, p2) {
		var gp1 = this.toGlobalPoint(p1);
		var gp2 = this.toGlobalPoint(p2);
		return WB.Geom.distance(gp1, gp2);
	},
	
	globalLength: function(length) {
		var x1 = this.toGlobalPoint({x:0,y:0}).x;
		var x2 = this.toGlobalPoint({x:length,y:0}).x;
		return Math.abs(x2 - x1);
		
	},
	
	getPathStartPoint: function() {
		if (!this.pathStartPoint) {
			return null;
		}
		return this.toLocalPoint(this.pathStartPoint);
	},
	
    beginPath: function() {
		if (this.trace) {
			console.log('context.beginPath()');
		}
    	this.context.beginPath();
    	// TODO can enable this once other cursor operations are supported
		// this._update(null, false, false);
    },
	
	/**
	 * moveTo(Point p)
	 */
	moveTo: function(p) {
		if (this.trace) {
			console.log('context.moveTo(' + p.x + ', ' + p.y + ')');
		}
		this.context.moveTo(p.x, p.y);
		this._update(p, false, true);
	},
	
	/**
	 * lineTo(Point p)
	 */
	lineTo: function(p) {
		if (this.trace) {
			console.log('context.lineTo(' + p.x + ', ' + p.y + ')');
		}
		this.context.lineTo(p.x, p.y);
		this._update(p, false, false);
		if (this.capturer) {
			var sp = this.getPathStartPoint();
			if (sp) {
				this._capture(sp.x, sp.y, p.x, p.y);
			} else {
				this._capture(p.x, p.y, p.x, p.y);
			}
		}
	},
	
	/**
	 * arc(Point c, double r, double sAngle, double eAngle, boolean counterclockwise)
	 */
	arc: function(c, r, sAngle, eAngle, counterclockwise) {
		if (this.trace) {
			console.log('context.arc(' + c.x 
					+ ', ' + c.y 
					+ ', ' + r 
					+ ', ' + sAngle 
					+ ', ' + eAngle 
					+ ', ' + counterclockwise 
					+ ')');
		}
		this.context.arc(c.x, c.y, r, sAngle, eAngle, counterclockwise);
		// last point
		var p = {x: c.x + Math.cos(eAngle) * r, y: c.y + Math.sin(eAngle) * r};
		this._update(p, false, false);
		if (this.capturer) {
			// TODO: doesn't always work!
			// TRICK: r is always maximum of (rx,ry) thus this should be correct
			this._capture(c.x - r, c.y - r, c.x + r, c.y + r);
		}
	},
	
	stroke: function() {
		if (this.trace) {
			console.log('context.lineWidth = ' + this.lineWidth);
			console.log('context.stroke()');
		}
		if (this.rendering) {
			this.context.lineWidth = this.lineWidth;
			this.context.stroke();
		}
	},
	
	drawImage: function(img, x, y, width, height) {
		if (this.rendering) {
			this.context.drawImage(img, x, y, width, height);
		}
		if (this.capturer) {
			this._capture(x, y, x + width, y + height);
		}
	},
	
	drawImageClipped: function(img, sx, sy, swidth, sheight, x, y, width, height) {
		if (this.rendering) {
			this.context.drawImage(img, sx, sy, swidth, sheight, x, y, width, height);
		}
		if (this.capturer) {
			this._capture(x, y, x + width, y + height);
		}
	}
	
});


/**
 * 
 */
WB.Animation = WB.Class.extend('Animation', {
	
	start: function(board) {
	},
	
	frame: function(time) {
	},
	
	isDone: function() {
		return true;
	},
	
	end: function() {
	},
	
	getTimeLeft: function() {
		return 0;
	}
	
});


WB.PauseAnimation = WB.Animation.extend('PauseAnimation', {
	
	pauseTime: null,
	
	init: function(time) {
		this.pauseTime = time;
	},
	
	createAnimation: function() {
		return this;
	},
	
	start: function(board) {
		this.board = board;
		this.done = this.pauseTime < 1;
		console.log('pause started: ' + this.pauseTime);
	},
	
	frame: function(time) {
		if (!this.startTime) {
			this.startTime = time;
		}
		this.done = (time - this.startTime) > this.pauseTime;
		if (this.done) {
			console.log('pause finished: ' + this.pauseTime + ' @ ' + time);			
		}
	},
	
	isDone: function() {
		return this.done;
	},
	
	end: function() {
		console.log('pause finished');
	}
	
});


WB.WaitForAnimation = WB.Animation.extend('WaitForAnimation', {
	
	happenedCheck: null,
	
	maxTime: null,
	
	init: function(happenedCheck, maxTime) {
		this.happenedCheck = happenedCheck;
		this.maxTime = maxTime ? maxTime: Number.MAX_VALUE;
	},
	
	createAnimation: function() {
		return this;
	},
	
	start: function(board) {
		this.board = board;
		this.done = this.maxTime < 1 || this.happenedCheck();
		console.log('waitFor started');
	},
	
	frame: function(time) {
		if (!this.startTime) {
			this.startTime = time;
		}
		this.done = (time - this.startTime) > this.maxTime || this.happenedCheck();
//		if (!this.done) {
//			this.board.resetPosition();
//		}
	},
	
	isDone: function() {
		return this.done;
	},
	
	end: function() {
		console.log('waitFor finished');
	}
	
});


/**
 * 
 */
WB.SubAnimation = WB.Animation.extend('SubAnimation', {
	
	source: null,
	
	startTime: null,
	
	init: function(source, startTime) {
		this.source = source;
		this.startTime = startTime;
		this.animation = source.createAnimation();
	},
	
	start: function(board) {
		this.animation.start(board);
	},
	
	frame: function(time) {
		this.animation.frame(time - this.startTime);
	},
	
	isDone: function() {
		return this.animation.isDone();
	},
	
	end: function() {
		this.animation.end();
	},
	
	getTimeLeft: function() {
		return this.animation.getTimeLeft();
	}
	
});


/**
 *
 */
WB.AnimationDelegate = WB.Animation.extend('AnimationDelegate', {
	
	animation: null,
	
	board: null,
	
	init: function(animation) {
		this.animation = animation;
	},
	
	start: function(board) {
		this.board = board;
		this.pane = board.animationPane;
		this.animation.start(board);
	},
	
	frame: function(time) {
		this.animation.frame(time);
	},
	
	isDone: function() {
		return this.animation.isDone();
	},
	
	end: function() {
		this.animation.end();
	},
	
	getTimeLeft: function() {
		return this.animation.getTimeLeft();
	}
	
});


/**
 */
WB.ListAnimation = WB.Animation.extend('ListAnimation', {
	
	items: null,
	
	board: null,
	
	init: function(items) {
		this.items = items;
	},
	
	start: function(board) {
		this._start(board);
	},
	
	isDone: function() {
		return this.done;
	},
	
	end: function() {
	},

	frame: function(time) {
		this._frame(time);
	},

	_start: function(board) {
		this.board = board;
		this.pane = board.animationPane;
		
		this.completeList = [];
		this.pendingList = this.items.slice(0);
		this.timeLeft = 0;
		this.done = !this.pendingList.length;
		this.prevTime = 0;
	},
	
	_frame: function(time) {
		
		var currentTime = this.prevTime;
		do {

			if (!this.wip && this.pendingList.length) {
				do {
					if (this.wip) {
						this.wip.end();
						this.completeList.push(this.wip.source);
						this.wip = null;
					}
					if (this.pendingList.length) {
						var part = this.pendingList.splice(0, 1)[0];
						this.wip = new WB.SubAnimation(part, currentTime);
						this.wip.start(this.board);
					}
				} while (!!this.wip && this.wip.isDone());
			}
			
			if (this.wip) {

				// do a frame if not over
				if (!this.wip.isDone()) {
					this.wip.frame(time);
				}
				
				// complete frame if finished in the last frame
				if (this.wip.isDone()) {
					var timeLeft = this.wip.getTimeLeft();
					currentTime = time - (timeLeft ? timeLeft : 0);
					this.wip.end();
			    	this.completeList.push(this.wip.source);
			    	this.wip = null;
				} else {
					currentTime = time;
				}
			}
		} while (currentTime < time && this.pendingList.length);

		this.done = !this.pendingList.length && !this.wip;
		this.timeLeft = time > currentTime + 1 ? time - currentTime : 0;
		this.prevTime = currentTime;
	},
	
	getTimeLeft: function() {
		return this.timeLeft;
	}
	
});


/**
 */
WB.NumInterpolator = WB.Animation.extend('NumInterpolator', {
	
	init: function(from, to, velocity, maxTime) {
		this.from = from;
		this.to = to;
		this.velocity = velocity;
		this.maxTime = maxTime;
	},
	
	start: function(board) {
		this.board = board;
		
        this.totalDistance = Math.abs(this.to - this.from);
        this.dir = this.to >= this.from ? 1 : -1;
        this.totalTime = this.velocity ? this.totalDistance/this.velocity : this.maxTime;
        if (!this.totalTime && this.totalTime != 0) {
        	throw "unknown time";
        }
        if (this.maxTime && this.totalTime > this.maxTime) {
        	this.totalTime = this.maxTime;
        	this.velocity = null;
        }
        if (!this.velocity && this.totalTime > 0) {
        	this.velocity = this.totalDistance / this.totalTime;
        }
        
        this.timeLeft = 0;
        this.done = this.totalTime < 1;
        this.value = this.from;
	},
	
	isDone: function() {
		return this.done;
	},
	
	getTimeLeft: function() {
		return this.timeLeft;
	},
	
	frame: function(time) {
		if (time < this.totalTime) {
			var distance = time * this.velocity;
			if (distance > this.totalDistance) {
				distance = this.totalDistance;
			}
			this.value = this.from + this.dir * distance;
		} else {
	        this.timeLeft = time - this.totalTime;
			this.value = this.to;
			this.done = true;
		}
	},
	
	end: function() {
	},
	
	getValue: function() {
		return this.value;
	}
	
});


/**
 */
WB.PointInterpolator = WB.Animation.extend('PointInterpolator', {
	
	init: function(from, to, velocity, maxTime) {
		this.from = from;
		this.to = to;
		this.velocity = velocity;
		this.maxTime = maxTime;
	},
	
	start: function(board) {
		this.board = board;
		this.pane = board.animationPane;
		
        this.totalDistance = this.pane.distanceGlobal(this.from, this.to, false, true);
        this.totalTime = this.velocity ? this.totalDistance/this.velocity : this.maxTime;
        if (!this.totalTime && this.totalTime != 0) {
        	throw "unknown time";
        }
        if (this.maxTime && this.totalTime > this.maxTime) {
        	this.totalTime = this.maxTime;
        	this.velocity = null;
        }
        if (!this.velocity && this.totalTime > 0) {
        	this.velocity = this.totalDistance / this.totalTime;
        }
        
    	this.dx = this.to.x - this.from.x;
    	this.dy = this.to.y - this.from.y;
        this.timeLeft = 0;
        this.done = this.totalTime < 1;
        this.value = this.from;
	},
	
	isDone: function() {
		return this.done;
	},
	
	getTimeLeft: function() {
		return this.timeLeft;
	},
	
	frame: function(time) {
		if (time < this.totalTime) {
			var distance = time * this.velocity;
			if (distance > this.totalDistance) {
				distance = this.totalDistance;
			}
			
	        var x2 = this.dx * distance/this.totalDistance;
	        var y2 = this.dx != 0 ? (this.dy/this.dx) * x2 : 
				this.dy * distance/this.totalDistance;
	        this.value = WB.Geom.movePoint(this.from, x2, y2);
		} else {
	        this.timeLeft = time - this.totalTime;
			this.value = this.to;
			this.done = true;
		}
	},
	
	end: function() {
	},
	
	getValue: function() {
		return this.value;
	}
	
});

