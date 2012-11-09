

/**
 */
WB.ShapeEpisodeBase = WB.Episode.extend('ShapeEpisodeBase', {
	
	shape: null,
	
	position: null,
	
	width: null,
	
	height: null,
	
	realWidth: null,
	
	realHeight: null,
	
	rotationDegree: null,
	
	rate: 1,
	
	init: function(opts) {
		if (opts) {
			for (var k in opts) {
				this[k] = opts[k];
			}
		}
	},
	
	_shape: function() {
		if (!this._shapeCache) {
			
			var tr = new WB.Transform();
			tr.translate(this.position.x, this.position.y);
			// TODO calculate real width/height if not given
			tr.scale(this.width/this.realWidth, 
					this.height/this.realHeight);
			if (this.rotationDegree) {
				tr.rotate(WB.Geom.rad(this.rotationDegree));
			}
			
			this._shapeCache = new WB.GroupShape({
				shapes: [this.shape],
				transform: tr
				});
		}
		return this._shapeCache;
	},
	
	createAnimation: function() {
		return new WB.ShapeEpisodeAnimation(this, this._shape().createAnimation(),
				this.rate);
	}
	
});


/**
 */
WB.DrawShapeEpisode = WB.ShapeEpisodeBase.extend('DrawShapeEpisode', {
});


/**
 */
WB.ShapeEpisodeAnimation = WB.Animation.extend('ShapeEpisodeAnimation', {
	
	episode: null,
	
	animation: null,
	
	board: null,
	
	init: function(episode, animation, rate) {
		this.episode = episode;
		this.animation = animation;
		this.rate = rate;
	},
	
	start: function(board) {
		this.board = board;
		this.pane = board.animationPane;
		this.oldVelocity = this.board.baseVelocity;
		
		console.log('rate: ' + this.rate);
		this.board.baseVelocity = this.oldVelocity * this.rate;
		
		this.animation.start(board);

		console.log('Episode started ' + this.episode._type);
	},
	
	frame: function(time) {
		this.animation.frame(time);
	},
	
	isDone: function() {
		return this.animation.isDone();
	},
	
	end: function() {
		this.animation.end();
		this.board.state({velocity: 0, height: 1});
		this.board.baseVelocity = this.oldVelocity;
	},
	
	getTimeLeft: function() {
		return this.animation.getTimeLeft();
	}
	
});


/**
 */
WB.Shape = WB.Class.extend('Shape', {
	
	draw: function(pane) {
	},
	
	createAnimation: function() {
		return null;
	}
	
});


/**
 */
WB.ShapeMeta = WB.Class.extend('ShapeMeta', {
	
	init: function(opts) {
		if (opts) {
			for (var k in opts) {
				this[k] = opts[k];
			}
		}
	}
	
});


/**
 */
WB.GroupShape = WB.Shape.extend('GroupShape', {
	
	transform: null,
	
	shapes: null,
	
	init: function(opts) {
		if (opts && opts.transform) {
			this.transform = opts.transform;
		}
		if (opts && opts.transformMatrix) {
			this.transform = new WB.Transform(opts.transformMatrix);
		}
		if (opts && opts.shapes) {
			this.shapes = opts.shapes;
		}
		if (opts && opts.width) {
			this.width = opts.width;
		}
		if (opts && opts.height) {
			this.height = opts.height;
		}
	},
	
	draw: function(pane) {
		var that = this;
		pane.withTr(this.transform, function() {
			that._draw(pane);
		});
	},
	
	_draw: function(pane) {
		for (var i = 0; i < this.shapes.length; i++) {
			this.shapes[i].draw(pane);
		}
	},
	
	createAnimation: function() {
		return new WB.GroupShapeAnimation(this);
	}
	
});


/**
 */
WB.GroupShapeAnimation = WB.ListAnimation.extend('GroupShapeAnimation', {
	
	init: function(group) {
		this.group = group;
		this.items = [];
		if (group.shapes.length) {
			for (var i = 0; i < group.shapes.length; i++) {
				var shape = group.shapes[i];
				this.items.push(shape);
			}
		}
	},
	
	start: function(board) {
		if (!this.group.transform) {
			this._start(board);
		} else {
			var that = this;
			board.withTr(this.group.transform, function() {
				that._start(board);
			});
		}
	},
	
	frame: function(time) {
		if (!this.group.transform) {
			this._frame(time);
		} else {
			var that = this;
			this.board.withTr(this.group.transform, function() {
				that._frame(time);
			});
		}
	}
	
});


/**
 */
WB.MoveShapeAnimation = WB.Animation.extend('MoveShapeAnimation', {
	
	init: function(shape, fromPoint, toPoint) {
		this.shape = shape;
		this.fromPoint = fromPoint;
		this.toPoint = toPoint;
	},
	
	start: function(board) {
		this.board = board;
		this.velocity = board.getBaseVelocity();
		this.pane = board.animationPane;
		
    	this.dx = this.toPoint.x - this.fromPoint.x;
    	this.dy = this.toPoint.y - this.fromPoint.y;
        this.totalDistance = this.pane.distanceGlobal(this.fromPoint, 
        		this.toPoint, false, true);
        this.timeLeft = 0;
        this.done = this.totalDistance < 1.0;

        this.movedShape = new WB.GroupShape({
			transform: new WB.Transform().translate(this.fromPoint.x, this.fromPoint.y),
			shapes: [this.shape]
		});	
	},
	
	isDone: function() {
		return this.done;
	},
	
	frame: function(time) {
		var distance = time * this.velocity / 1000;
		if (distance > this.totalDistance) {
	        this.timeLeft = time - this.totalDistance / this.velocity * 1000;
			distance = this.totalDistance;
		}
        
        var x2 = this.dx * distance/this.totalDistance;
        var y2 = this.dx != 0 ? (this.dy/this.dx) * x2 : 
			this.dy * distance/this.totalDistance;
		
        var newPoint = WB.Geom.movePoint(this.fromPoint, x2, y2);
        
        this.movedShape.transform = new WB.Transform().translate(newPoint.x, newPoint.y);
        this.movedShape.draw(this.pane);
		
	    this.done = Math.abs(this.totalDistance - distance) < 1.0;
	    
	    this.board.state({
	    	pointer: 'move',
	    	//position: this.pane.toGlobalPoint(newPoint),
	    	velocity: this.velocity,
	    	angle: WB.Geom.angle(this.fromPoint, newPoint),
	    	height: 1.0
	    });
	},
	
	end: function() {
		var p = this.toPoint;
        this.movedShape.transform = new WB.Transform().translate(p.x, p.y);
		this.board.commitShape(this.movedShape, true);
	},
	
	getTimeLeft: function() {
		return this.timeLeft;
	}
	
});

