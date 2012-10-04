

/**
 * 
 */
WB.GeomProto = WB.Class.extend({
	
	PI: Math.PI,
	
	PI_HALF: Math.PI/2,

	PI_1_HALF: Math.PI + Math.PI/2,

	PI_2: Math.PI * 2,
	
	movePoint: function(p, dx, dy) {
		return {x: p.x + dx, y: p.y + dy};
	},

	/**
	 * Angle b/w two points
	 */
	angle: function(p1, p2) {
		return Math.atan2(p1.y - p2.y, p1.x - p2.x);
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
	}
	
});

WB.Geom = new WB.GeomProto();


/**
 * Most methods return 'this'
 */
WB.Transform = WB.Class.extend({
	
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
WB.Pane = WB.Class.extend({
	
	canvas: null,
	
	context: null,
	
    init: function(opts) {
    	this.trace = opts && opts.trace || false;
    	
    	if (opts && opts.canvas) {
    		this.canvas = opts.canvas;
    	}
    	if (opts && opts.context) {
    		this.context = opts.context;
    	}
    	if (!this.context) {
    		this.context = this.canvas.getContext('2d');
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
		this.lineWidth = WB.Geom.distance(w0, w2);
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
		this.context.clearRect(0, 0, this.canvas.width, this.canvas.height);
	},
	
	setCursorPositionGlobal: function(p) {
		this.cursorPosition = p;
	},
	
	toGlobalPoint: function(p) {
		return this.currentTransform.transformPoint(p.x, p.y);
	},
	
	toLocalPoint: function(p) {
		return this.currentTransformInv.transformPoint(p.x, p.y);
	},
	
	_update: function(p, global) {
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
		// console.log('-> ' + JSON.stringify(this._currentPoint));
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
	
    beginPath: function() {
		if (this.trace) {
			console.log('context.beginPath()');
		}
    	this.context.beginPath();
    	// TODO can enable this once other cursor operations are supported
		// this._update(null, false);
    },
	
	/**
	 * moveTo(Point p)
	 */
	moveTo: function(p) {
		if (this.trace) {
			console.log('context.moveTo(' + p.x + ', ' + p.y + ')');
		}
		this.context.moveTo(p.x, p.y);
		this._update(p, false);
	},
	
	/**
	 * lineTo(Point p)
	 */
	lineTo: function(p) {
		if (this.trace) {
			console.log('context.lineTo(' + p.x + ', ' + p.y + ')');
		}
		this.context.lineTo(p.x, p.y);
		this._update(p, false);
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
		var p = {x: Math.cos(eAngle) * r, y: Math.sin(eAngle) * r};
		this._update(p, false);
	},
	
	stroke: function() {
		if (this.trace) {
			console.log('context.lineWidth = ' + this.lineWidth);
			console.log('context.stroke()');
		}
		this.context.lineWidth = this.lineWidth;
		this.context.stroke();
	}
	
});


/**
 * 
 */
WB.Animation = WB.Class.extend({
	
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


WB.PauseAnimation = WB.Animation.extend({
	
	pauseTime: null,
	
	init: function(time) {
		this.pauseTime = time;
		this.done = this.pauseTime < 1;
		console.log('pause started');
	},
	
	createAnimation: function() {
		return this;
	},
	
	frame: function(time) {
		if (!this.startTime) {
			this.startTime = time;
		}
		this.done = (time - this.startTime) > this.pauseTime;
	},
	
	isDone: function() {
		return this.done;
	},
	
	end: function() {
		console.log('pause finished');
	}
	
});


/**
 * 
 */
WB.SubAnimation = WB.Animation.extend({
	
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
WB.AnimationDelegate = WB.Animation.extend({
	
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
WB.ListAnimation = WB.Animation.extend({
	
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
	},
	
	_frame: function(time) {
		
		var timeLeft = 0;
		do {

			if (!this.wip && this.pendingList.length) {
				do {
					if (!!this.wip) {
						this.wip.end();
						this.completeList.push(this.wip.source);
						this.wip = null;
					}
					if (this.pendingList.length) {
						var part = this.pendingList.splice(0, 1)[0];
						this.wip = new WB.SubAnimation(part, time - timeLeft);
						this.wip.start(this.board);
					}
				} while (!!this.wip && this.wip.isDone());
			}
			
			if (!!this.wip && !this.wip.isDone()) {
				timeLeft = 0;
				this.wip.frame(time);
			}
	
			if (!!this.wip && this.wip.isDone()) {
				this.wip.end();
		    	this.completeList.push(this.wip.source);
				timeLeft = this.wip.getTimeLeft();
		    	this.wip = null;
			}

		} while (timeLeft > 1 && this.pendingList.length);

		this.timeLeft = timeLeft > 1 ? timeLeft : 0;
		this.done = !this.pendingList.length && !this.wip;
	},
	
	getTimeLeft: function() {
		return this.timeLeft;
	}
	
});

