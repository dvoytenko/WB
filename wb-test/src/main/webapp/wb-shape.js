

WB.DrawShapeEpisode = WB.Class.extend('DrawShapeEpisode', {
	
	shape: null,
	
//	shapeId: null,
	
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
		return new WB.DrawShapeEpisodeAnimation(this._shape().createAnimation(),
				this.rate);
	}
	
});


WB.DrawShapeEpisodeAnimation = WB.Animation.extend('DrawShapeEpisodeAnimation', {
	
	animation: null,
	
	board: null,
	
	init: function(animation, rate) {
		this.animation = animation;
		this.rate = rate;
	},
	
	start: function(board) {
		this.board = board;
		this.pane = board.animationPane;
		this.oldVelocity = this.board.baseVelocity;
		this.board.baseVelocity = this.oldVelocity * this.rate;
		console.log('rate: ' + this.rate);
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
		this.board.state({velocity: 0, height: 1});
		this.board.baseVelocity = this.oldVelocity;
	},
	
	getTimeLeft: function() {
		return this.animation.getTimeLeft();
	}
	
});


WB.Shape = WB.Class.extend('Shape', {
	
	draw: function(pane) {
	},
	
	createAnimation: function() {
		return null;
	}
	
});


WB.ShapeMeta = WB.Class.extend('ShapeMeta', {
	
	init: function(opts) {
		if (opts) {
			for (var k in opts) {
				this[k] = opts[k];
			}
		}
	}
	
});


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

