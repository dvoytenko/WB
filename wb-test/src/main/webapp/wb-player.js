
WB.Player = WB.Class.extend({
	
	board: null,
	
	framer: null,
	
	state: null,
	
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
		
		this.state = 'none';
	},
	
	play: function(animation, onDone) {
		
		if (this.state != 'none' 
				&& this.state != 'ended'
				&& this.state != 'cancelled') {
			throw "player is busy, state: " + this.state;
		}

		this.startTime = new Date().getTime();
		this.animation = this.board.createAnimation(animation);
		this.onDone = onDone;
		this.state = 'playing';
		if (this._listeners) {
			this._notify('state', {state: this.state});
		}
		
		this.animation.start(this.board);

		if (this.animation.isDone()) {
			this._end();
		} else {
			this._frame();
		}
	},
	
	cancel: function() {
		console.log('cancel');
		this.state = 'cancelled';
		if (this._listeners) {
			this._notify('state', {state: this.state});
		}
	},

	suspend: function() {
		console.log('suspend');
		this.state = 'suspended';
		this.suspendedTime = new Date().getTime();
		if (this._listeners) {
			this._notify('state', {state: this.state});
		}
	},

	resume: function() {
		console.log('resume');
		
		var resumeTime = new Date().getTime();
		var newStartTime = this.startTime + (resumeTime - this.suspendedTime);
		this.suspendedTime = null;
		this.startTime = newStartTime;
		
		this.state = 'playing';
		var framer = this.framer;
		var that = this;
		framer(function() {
			that._frame();
		});

		if (this._listeners) {
			this._notify('state', {state: this.state});
		}
	},
	
	_end: function() {
		this.animation.end();
		this.state = 'ended';
		if (this._listeners) {
			this._notify('state', {state: this.state});
		}
		if (this.onDone) {
			this.onDone();
		}
	},
	
	_frame: function() {
		
//		debugger;
		
		if (!this.animation.isDone()) {
			var frameTime = new Date().getTime() - this.startTime;
			if (frameTime > 0) {
				this.animation.frame(frameTime);
				if (this._listeners) {
					this._notify('frame', {time: frameTime});
				}
			}
		}
		
		if (this.state == 'cancelled') {
			this.board.cancel();
			this.state = 'ended';
		} else if (this.animation.isDone()) {
			this._end();
		} else if (this.state == 'playing') {
			var that = this;
			var framer = this.framer;
			framer(function() {
				that._frame();
			});
		}
	},

	
	playShape: function(shape, onDone) {
		this.play(shape.createAnimation(), onDone);
	},
	
	playEpisode: function(episode, onDone) {
		this.play(episode.createAnimation(), onDone);
	},
	
	playAny: function(animable, onDone) {
		this.play(animable.createAnimation(), onDone);
	},
	
	bind: function(listener) {
		if (!this._listeners) {
			this._listeners = [];
		}
		this._listeners.push(listener);
	},
	
	_notify: function(type, payload) {
		if (this._listeners) {
			for (var i = 0; i < this._listeners.length; i++) {
				this._listeners[i](type, payload);
			}
		}
	}
	
});

