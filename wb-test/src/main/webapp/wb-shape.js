

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
	
	predraw: false,
	
	action: null,
	
	moveStartPosition: null,
	
	init: function(opts) {
		if (opts) {
			for (var k in opts) {
				this[k] = opts[k];
			}
		}
	},
	
	_shape: function(includePosition) {
		
		var tr = new WB.Transform();
		
		if (includePosition) {
			tr.translate(this.position.x, this.position.y);
		}
		
		// TODO calculate real width/height if not given?
		tr.scale(this.width/this.realWidth, 
				this.height/this.realHeight);
		if (this.rotationDegree) {
			tr.rotate(WB.Geom.rad(this.rotationDegree));
		}
		
		return new WB.GroupShape({
			shapes: [this.shape],
			transform: tr
			});
	},
	
	prepare: function(board) {
		if (this.predraw || this.action == 'predraw') {
			board.commitShape(this._shape(true), true);
		}
	},

	getPointer: function() {
		if (this.predraw || this.action == 'predraw') {
			return null;
		}
		if (this.action == 'move') {
			return 'move';
		}
		return 'draw';
	},
	
	createAnimation: function() {
		if (this.predraw || this.action == 'predraw') {
			return new WB.Animation();
		}
		if (this.action == 'move') {
			// TODO default moveStartPosition to something?
			return new WB.MoveShapeAnimation(this._shape(false),
					this.moveStartPosition, this.position,
					this.width, this.height,
					this.rate);
		}
		return new WB.ShapeEpisodeAnimation(this, this._shape(true).createAnimation(),
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
		this.board.state({pointer: 'draw'});

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
	
	isReady: function() {
		return true;
	},
	
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
	
	isReady: function() {
		for (var i = 0; i < this.shapes.length; i++) {
			if (!this.shapes[i].isReady()) {
				return false;
			}
		}
		return true;
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
	
	init: function(shape, fromPoint, toPoint, width, height, rate) {
		this.shape = shape;
		this.fromPoint = fromPoint;
		this.toPoint = toPoint;
		
		this.width = width;
		this.height = height;
		
		// TODO process rate
		this.rate = rate;
	
		console.log('MoveShapeAnimation from ' + JSON.stringify(fromPoint) + ' to ' + JSON.stringify(toPoint));
	},
	
	start: function(board) {
		this.board = board;
		this.velocity = board.getBaseVelocity();
		this.pane = board.animationPane;
		
		this.inter = new WB.PointInterpolator(this.fromPoint, this.toPoint, 
				this.velocity/1000);
		this.inter.start(board);
		
        this.movedShape = new WB.GroupShape({
			transform: new WB.Transform().translate(this.fromPoint.x, this.fromPoint.y),
			shapes: [this.shape]
		});	
	},
	
	isDone: function() {
		return this.shape.isReady() && this.inter.isDone();
	},
	
	getTimeLeft: function() {
		return this.inter.getTimeLeft();
	},
	
	frame: function(time) {
		
		this.inter.frame(time);
		
        var newPoint = this.inter.getValue();
        
        this.movedShape.transform = new WB.Transform().translate(newPoint.x, newPoint.y);
		// console.log('draw image @ ' + newPoint.x + ' x ' + newPoint.y);
        this.movedShape.draw(this.pane);
		
	    var holdPoint;
	    if (this.width && this.height) {
	    	holdPoint = WB.Geom.movePoint(newPoint, this.width/2, this.height/3*2);
	    } else {
	    	holdPoint = WB.Geom.movePoint(newPoint, 10, 10);
	    }
	    
	    this.board.state({
	    	pointer: 'move',
	    	position: this.pane.toGlobalPoint(holdPoint),
	    	velocity: this.inter.velocity,
	    	angle: WB.Geom.angle(this.fromPoint, newPoint),
	    	height: 1.0
	    });
	},
	
	end: function() {
		var p = this.toPoint;
        this.movedShape.transform = new WB.Transform().translate(p.x, p.y);
		this.board.commitShape(this.movedShape, true);
	}
	
});

