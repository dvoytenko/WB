

WB.EraserEpisode = WB.Episode.extend('EraserEpisode', {
	
	pathSegment: null,
	
	init: function(opts) {
		if (opts) {
			for (var k in opts) {
				this[k] = opts[k];
			}
		}
	},
	
	_eraser: function() {
		return new WB.EraserShape({
			pathSegment: this.pathSegment
		});
	},
	
	prepare: function(board) {
		if (this.predraw || this.action == 'predraw') {
			board.commitShape(this._eraser(), true);
		}
	},
	
	createAnimation: function() {
		return this._eraser().createAnimation();
	}
	
});


WB.EraserShape = WB.Shape.extend('EraserShape', {
	
	pathSegment: null,
	
	_points: null,
	_lines: null,
	_rects: null,
	_bounds: null,
	
	init: function(opts) {
		if (opts) {
			for (var k in opts) {
				this[k] = opts[k];
			}
		}
	},
	
	draw: function(pane) {
		this.prepare(pane);
		var that = this;
		pane.withTr(pane.defaultTransform, function() {
			that.drawPartial(pane, that._bounds);
		});
	},
	
	prepare: function(pane) {

		// 1. Get polygon
		var points = [];
		this.pathSegment.polygon(pane, points);
		
		// 2. Turn polygon to lines and get bounds
		var xMax = points[0].x, xMin = points[0].x, yMax = points[0].y, yMin = points[0].y;
		var lines = [];
		for (var i = 1; i < points.length; i++) {
			var start = points[i - 1];
			var end = points[i];
			if (!WB.Geom.pointsEqual(start, end)) {
				lines.push({start: start, end: end});
			}
			xMax = Math.max(xMax, end.x);
			xMin = Math.min(xMin, end.x);
			yMax = Math.max(yMax, end.y);
			yMin = Math.min(yMin, end.y);
		}
		var bounds = {topleft: {x: xMin, y: yMin},
				bottomright: {x: xMax, y: yMax}};

		// 4. Scan horizontally and get all the vertical lines
		var rects = [];
		for (var x = bounds.topleft.x; x <= bounds.bottomright.x; x += 1) {
			// 4.1. Find intercepting points
			var yList = [];
			for (var i = 0; i < lines.length; i++) {
				var line = lines[i];
				if (line.start.x == line.end.x) {
					// vertical line: start.x == end.x
					if (x == line.start.x) {
						// scan line coincides with the line line
						yList.push(line.end.y);
					}
				} else {
					var y = WB.Geom.lineY(line, x);
					if (y) {
						var p = {x: x, y: y};
						if (WB.Geom.pointsEqual(p, line.end)) {
							// scan line goes through the end vertex
							var next = lines[(i + 1) % lines.length];
							// we can add a point if the adjacent lines are on the
							// different sides of this point
							if (WB.Geom.sign(x - line.start.x) != WB.Geom.sign(x - next.end.x)) {
								yList.push(y);
							}
						} else if (!WB.Geom.pointsEqual(p, line.start)) {
							// scan line intersects the line
							yList.push(y);
						}
					}
				}
			}
			yList.sort();
//			console.log(yList);
			
			for (var i = 1; i < yList.length; i += 2) {
				rects.push(WB.bounds(x, yList[i - 1], x, yList[i]));
			}
		}

//		console.log('rects:');
//		console.log(rects);
		
		this._points = points;
		this._lines = lines;
		this._bounds = bounds;
		this._rects = rects;
	},
	
	boundsPartial: function(maxBounds) {
		var count = 0;
		var xMax, xMin, yMax, yMin;
		for (var i = 0; i < this._rects.length; i++) {
			var rect = this._rects[i];
			var x = rect.x;
			var y = rect.y;
			var height = rect.height;
			var bounds = WB.Geom.intersectBounds(maxBounds, rect);
			//console.log('intersect: ' + JSON.stringify(rect) + ' -> ' + JSON.stringify(bounds));
			if (bounds) {
				count++;
				if (count == 1) {
					xMin = bounds.topleft.x;
					xMax = bounds.bottomright.x;
					yMin = bounds.topleft.y;
					yMax = bounds.bottomright.y;
				} else {
					xMin = Math.min(xMin, bounds.topleft.x);
					xMax = Math.max(xMax, bounds.bottomright.x);
					yMin = Math.min(yMin, bounds.topleft.y);
					yMax = Math.max(yMax, bounds.bottomright.y);
				}
			}
		}
		if (count == 0) {
			return null;
		}
		return {topleft: {x: xMin, y: yMin},
			bottomright: {x: xMax, y: yMax}};
	},
	
	drawPartial: function(pane, bounds) {
		
		var context = pane.context;
		
		if (this._rects.length < 1) {
			// nothing to do here - can't build a shape from two lines
			return;
		}

		context.save();
		
		function bleach(bounds) {
			bounds = pane.toGlobalBounds(bounds);
			var x = bounds.topleft.x;
			var y = bounds.topleft.y;
			var width = bounds.bottomright.x - bounds.topleft.x + 1;
			var height = bounds.bottomright.y - bounds.topleft.y + 1;
			//console.log('bleach: (' + x + ', ' + y + '), (' + bounds.bottomright.x + ', ' + bounds.bottomright.y + ')');
			var pixels = context.getImageData(x, y, width, height);
			var d = pixels.data;
			for (var i = 0; i < d.length; i += 4) {
				d[i+3] = 255 * 0.01;
			}
			context.putImageData(pixels, x, y);
		}
		
		for (var i = 0; i < this._rects.length; i++) {
			var rect = this._rects[i];
			var inter = WB.Geom.intersectBounds(rect, bounds);
			if (inter) {
				bleach(inter);
			}
		}

		// TEST DRAW
		if (false) {
			context.beginPath();
			for (var i = 0; i < this._points.length; i++) {
				var p = this._points[i];
				if (i == 0) {
					context.moveTo(p.x, p.y);
				} else {
					context.lineTo(p.x, p.y);
				}
			}
			context.stroke();
		}
		
		// source-atop: works, but makes black lines white
		// source-in: makes destination transparent!
		// destination-in: the source image is transparent
		// destination-out: ??? could work with 0 color
		
//		context.globalCompositeOperation = 'destination-out';
//		context.fillStyle = 'rgb(0,0,0,0.5)';
		
//		context.beginPath();
//		this.pathSegment.outline(pane);
//		context.clip();
		
//		context.fill();
		
		/* alpha: 1 - not transparent, 0 - completely transparent
		var grad = context.createRadialGradient(190, 240, 20, 
				170, 250, 100);
		grad.addColorStop(0, 'rgba(255,255,255,0.99)');
		grad.addColorStop(1, 'rgba(255,255,255,0.21)');
		context.fillStyle = grad;
		*/

//		context.fillRect(90, 190, 170, 120);
		
		/*
			var grad = context.createLinearGradient(200, 90, 200, 170);
			grad.addColorStop(0, 'rgba(255,255,255,0.98)');
			grad.addColorStop(0.7, 'rgba(255,255,255,0.85)');
			grad.addColorStop(1, 'rgba(255,255,255,0.21)'); // white
			context.fillStyle = grad;
			context.fillRect(200, 90, 80, 80);
			context.strokeRect(200, 90, 80, 80);
		 */
		
		context.restore();
	},
	
	createAnimation: function() {
		return new WB.EraserAnimation(this);
	}
	
});


WB.EraserAnimation = WB.Animation.extend('EraserAnimation', {
	
	/** @type {!EraserShape} */
	eraser: null,
	
	_anim: null,
	
	init: function(eraser) {
		this.eraser = eraser;
	},
	
	start: function(board) {
		this.board = board;
		this.pane = board.commitPane;
		this.eraser.prepare(this.pane);
		
		// get all the blocks we want to interpolate on
		var animations = [];
		var brushSize = 20;
		var bounds = this.eraser._bounds;
//		console.log('bounds: ' + JSON.stringify(bounds));
		var height = bounds.bottomright.y - bounds.topleft.y;
		var vertSteps = Math.max(Math.ceil(height/brushSize), 1);
		for (var v = 0; v < vertSteps; v++) {
			var y1 = bounds.topleft.y + v * brushSize;
			var y2 = y1 + brushSize;
			
			var maxBounds = WB.bounds(bounds.topleft.x, y1, bounds.bottomright.x, y2);
//			console.log('maxbounds: ' + JSON.stringify(maxBounds));
			var subBounds = this.eraser.boundsPartial(maxBounds);
//			console.log('-> subbounds: ' + JSON.stringify(subBounds));
			
			if (subBounds) {
				animations.push(new WB.EraseLineAnimation(this.eraser, subBounds));
			}
		}

		this._anim = new WB.ListAnimation(animations);
		this._anim.start(board);
	},
	
	isDone: function() {
		return this._anim.isDone();
	},
	
	end: function() {
		this._anim.end();
		this.board.commitShape(this.eraser, false);
	},
	
	frame: function(time) {
		this._anim.frame(time);
	},
	
	getTimeLeft: function() {
		return this._anim.getTimeLeft();
	}
	
});


WB.EraseLineAnimation = WB.Animation.extend('EraseLineAnimation', {
	/** @type {!EraserShape} */
	eraser: null,

	init: function(eraser, bounds) {
		this.eraser = eraser;
		this.bounds = bounds;
	},
	
	createAnimation: function() {
		return this;
	},

	start: function(board) {
		this.board = board;
		this.pane = board.commitPane;
		
		// move twice (or more) as fast as you draw
		this.velocity = board.getBaseVelocity() * 2;

		this.prevPoint = this.bounds.topleft;

		// width interpolation
		this._inter = new WB.NumInterpolator(this.bounds.topleft.x, this.bounds.bottomright.x,
				this.velocity/1000 / 5);
		this._inter.start(board);
	},
	
	isDone: function() {
		return this._inter.isDone();
	},
	
	end: function() {
		this._inter.end();
		this.board.commitShape(this.eraser, false);
	},
	
	frame: function(time) {
		this._inter.frame(time);

		var prevPoint = this.prevPoint;
        var newPoint = WB.point(this._inter.getValue(), this.bounds.topleft.y);

		this.eraser.drawPartial(this.pane, WB.bounds(
				prevPoint.x, this.bounds.topleft.y,
				newPoint.x + 1, this.bounds.bottomright.y));

        this.pane.moveTo(newPoint);

		this.board.state({
			pointer: 'eraser',
			position: this.pane.toGlobalPoint(newPoint),
	    	velocity: this._inter.velocity,
	    	angle: WB.Geom.angle(prevPoint, newPoint),
			pressure: 1.5,
			height: 0.0
		});
		
		this.prevPoint = newPoint;
	},
	
	getTimeLeft: function() {
		return this._inter.getTimeLeft();
	}
	
});
