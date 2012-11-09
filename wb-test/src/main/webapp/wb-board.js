

/**
 * State:
 * - position
 * - velocity
 * - angle
 * - height
 * - pressure
 */
WB.Board = WB.Class.extend('Board', {
	
	commitPane: null,
	
	animationPane: null,
	
	baseVelocity: null,
	
	drawingSoundEngine: null,
	
	pointer: null,
	
	speechPlayer: null,
	
	urlResolver: null,
	
	showBounds: false,
	
	init: function(opts) {
		
		if (opts && opts.urlResolver) {
			this.urlResolver = opts.urlResolver;
		} else {
			this.urlResolver = function(path) {return path;};
		}
		
		if (opts && opts.commitPane) {
			this.commitPane = opts.commitPane;
		}
		if (opts && opts.animationPane) {
			this.animationPane = opts.animationPane;
		}
		
		if (opts && opts.drawingSoundEngine) {
			this.drawingSoundEngine = opts.drawingSoundEngine;
		}
		if (opts && opts.speechPlayer) {
			this.speechPlayer = opts.speechPlayer;
		}
		if (opts && opts.pointer) {
			this.pointer = opts.pointer;
		}
		
		if (opts && opts.baseVelocity) {
			this.baseVelocity = opts.baseVelocity;
		} else {
			this.baseVelocity = 100;
		}
		
		if (opts && opts.showBounds) {
			this.showBounds = opts.showBounds;
		}
		
		this._shapes = [];
		this._state = {};
	},
	
	getBaseMoveVelocity: function() {
		return 200;
	},
	
	getBaseVelocity: function() {
		return this.baseVelocity;
	},
	
	getCurrentPosition: function() {
		return null;
	},
	
	commitShape: function(shape, render) {
		
		// console.log('commit shape: ' + shape._type);
		
		// render and bounds
		this.commitPane.captureBounds(render);
		shape.draw(this.commitPane);
		var bounds = this.commitPane.endCaptureBounds();
		// console.log('got bounds! ' + JSON.stringify(bounds));
		
		if (bounds) {
			var d = 4;
			bounds = WB.Geom.growBounds(bounds, d);
			// console.log('new bounds: ' + JSON.stringify(bounds));
		}
		
		if (this.showBounds && bounds) {
			var loc = this.commitPane.toLocalBounds(bounds);
			// console.log('local: ' + JSON.stringify(loc));
			var ctx = this.commitPane.context;
			ctx.save();
			ctx.lineWidth = 1;
			ctx.strokeStyle = 'blue';
			ctx.strokeRect(loc.topleft.x, loc.topleft.y, 
					loc.bottomright.x - loc.topleft.x,
					loc.bottomright.y - loc.topleft.y);
			ctx.restore();
		}

		this._shapes.push({
			shape: shape,
			tr: this.commitPane.currentTransform,
			bounds: bounds
		});
	},
	
	withRate: function(rate, runnable) {
		var oldVelocity = this.baseVelocity;
		this.baseVelocity = oldVelocity * rate;
		runnable();
		this.baseVelocity = oldVelocity;
	},
	
	withTr: function(transform, runnable) {
		if (transform) {
			var pane1 = this.commitPane;
			var pane2 = this.animationPane;
			pane1.withTr(transform, function() {
				//console.log('!!!TR1 ' + JSON.stringify(pane1.toGlobalPoint({x: 100, y: 100})));
				//console.log('!!!TR1 ' + JSON.stringify(pane2.toGlobalPoint({x: 100, y: 100})));
				pane2.withTr(transform, function() {
					//console.log('!!!TR2 ' + JSON.stringify(pane1.toGlobalPoint({x: 100, y: 100})));
					//console.log('!!!TR2 ' + JSON.stringify(pane2.toGlobalPoint({x: 100, y: 100})));
					runnable();
				});
			});
		} else {
			runnable();
		}
	},
	
	state: function(state) {
		if (state) {
			for (var k in state) {
				this._state[k] = state[k];
			}
		}
	},
	
	resetPosition: function() {
		// TODO smoothly move
		this.state({position: {x: 10, y: 390}});
	},

	beforeFrame: function() {
		this.animationPane._clearCanvas();
		if (this.drawingSoundEngine) {
			this.drawingSoundEngine.beforeFrame();
		}
		if (this.speechPlayer) {
			this.speechPlayer.beforeFrame();
		}
		if (this.pointer) {
			this.pointer.beforeFrame();
		}
	},
	
	afterFrame: function() {
		if (this.drawingSoundEngine) {
			this.drawingSoundEngine.update(this._state);
		}
		if (this.speechPlayer) {
			this.speechPlayer.update(this._state);
		}
		if (this.pointer) {
			this.pointer.update(this._state);
		}
	},
	
	cancel: function() {
		console.log('cancelled!');
		this.commitPane._clearCanvas();
		this.animationPane._clearCanvas();
	},
	
	getAnchorPoint: function() {
		var tr = this.commitPane.defaultTransform;
		return tr.transformPoint(0, 0);
	},
	
	updateAnchorPoint: function(newPoint) {
		console.log('updateAnchorPoint: ' + newPoint.x + ', ' + newPoint.y);
		
		var tr = new WB.Transform(this.commitPane.defaultTransform);
		var oldPoint = tr.transformPoint(0, 0);
		var dx = newPoint.x - oldPoint.x;
		var dy = newPoint.y - oldPoint.y;
		tr.translate(dx, dy);
		
		this.commitPane.updateDefaultTransform(tr);
		this.animationPane.updateDefaultTransform(tr);
		if (this.pointer && this.pointer.pane) {
			// TODO put pointer's pane under board and have pointer access it from heres
			this.pointer.pane.updateDefaultTransform(tr);
		}

		// redraw main pane
		var that = this;
		var pane = this.commitPane;
		pane._clearCanvas();
		var screenBounds = pane.globalBounds();
		// console.log('screenBounds: ' + JSON.stringify(screenBounds));
		for (var i = 0; i < this._shapes.length; i++) {
			var c = this._shapes[i];
			// console.log('shape bounds: ' + JSON.stringify(c.bounds));
			var incl = !c.bounds || WB.Geom.boundsOverlap(screenBounds, c.bounds);
			if (incl) {
				// console.log('shape overlaps');
				
				var tr = c.tr ? c.tr : new WB.Transform();
				pane.withTr(tr, function() {
					if (that.showBounds && c.bounds) {
						var loc = pane.toLocalBounds(c.bounds);
						// console.log('local: ' + JSON.stringify(loc));
						var ctx = pane.context;
						ctx.save();
						ctx.lineWidth = 1;
						ctx.strokeStyle = 'blue';
						ctx.strokeRect(loc.topleft.x, loc.topleft.y, 
								loc.bottomright.x - loc.topleft.x,
								loc.bottomright.y - loc.topleft.y);
						ctx.restore();
					}
					c.shape.draw(pane);
				});
			} else {
				// console.log('shape doesn\'t overlaps');
			}
		}
	},
	
	createAnimation: function(animation) {
		return new WB.BoardAnimation(animation, this);
	}
	
});


WB.BoardAnimation = WB.Class.extend('BoardAnimation', {
	
	init: function(animation, board) {
		this.animation = animation;
		this.board = board;
	},
	
	start: function(board) {
		console.log('animation started');
		this.animation.start(board);
	},
	
	frame: function(time) {
		this.board.beforeFrame();
		this.animation.frame(time);
		this.board.afterFrame();
	},
	
	isDone: function() {
		return this.animation.isDone();
	},
	
	end: function() {
		this.animation.end();
		this.board.animationPane._clearCanvas();
		this.board.state({height: 1});
		
		this.board.resetPosition();
		
		this.board.afterFrame();
		console.log('animation stopped');
	},
	
	getTimeLeft: function() {
		return this.animation.getTimeLeft();
	}
	
});

