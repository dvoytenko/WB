
WB.Player = WB.Class.extend({
	
	board: null,
	
	framer: null,
	
	init: function(opts) {
		if (opts && opts.board) {
			this.board = opts.board;
		}
		
		if (opts && opts.framer) {
			this.framer = opts.framer;
		}
		if (!this.framer) {
			this.framer = (function(callback) {
		        return window.requestAnimationFrame || 
			        window.webkitRequestAnimationFrame || 
			        window.mozRequestAnimationFrame || 
			        window.oRequestAnimationFrame || 
			        window.msRequestAnimationFrame ||
			        function(callback) {
			          window.setTimeout(callback, 1000 / 60);
			        };
		      })();
		}
	},
	
	play: function(animation, shape, onDone) {
		
		var startTime = new Date().getTime();
		var board = this.board;
		var framer = this.framer;
		var that = this;
		
		animation.start(board);

		if (animation.isDone()) {
			animation.end();
			if (shape) {
				board.animationPane._clearCanvas();
			}
			console.log('animation stopped');
			if (onDone) {
				onDone();
			}
			board.state({height: 1});
			board.afterFrame();
			return;
		}
		
		function frame() {
			if (!animation.isDone()) {
				var frameTime = new Date().getTime() - startTime;
				if (frameTime > 0) {
					if (shape) {
						board.animationPane._clearCanvas();
					}
					animation.frame(frameTime);
					that._notify(frameTime);
				}
			}
			if (animation.isDone()) {
				animation.end();
				if (shape) {
					board.animationPane._clearCanvas();
				}
				console.log('animation stopped');
				if (onDone) {
					onDone();
				}
				board.state({height: 1});
			} else {
				framer(function() {
					frame();
				});
			}
			
			board.afterFrame();
		}
		
		frame();
	},
	
	playShape: function(shape, onDone) {
		this.play(shape.createAnimation(), shape, onDone);
	},
	
	postFrame: function(listener) {
		if (!this._postFrameListeners) {
			this._postFrameListeners = [];
		}
		this._postFrameListeners.push(listener);
	},
	
	_notify: function(time) {
		if (this._postFrameListeners) {
			for (var i = 0; i < this._postFrameListeners.length; i++) {
				this._postFrameListeners[i](time);
			}
		}
	}
	
});
