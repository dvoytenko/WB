
WB.Stepper = WB.Class.extend('Stepper', {
	
	board: null,
	
	state: null,
	
	init: function(opts) {
		if (opts && opts.board) {
			this.board = opts.board;
		}
		if (opts && opts.interval) {
			this.interval = opts.interval;
		}
		if (!this.interval && opts && opts.fps) {
			this.interval = 1000 / opts.fps;
		}
		console.log('stepper interval: ' + this.interval);
		this.state = 'none';
		this.time = null;
	},
	
	start: function(animable) {
		this.animable = animable;
		this.board.cancel();
		this.animation = this.board.createAnimation(this.animable.createAnimation());
		this.animation.start(this.board);
		this.state = 'playing';
	},
	
	step: function(interval) {
		if (!interval) {
			interval = this.interval;
		}
		if (!this.animation) {
			throw "not started!";
		}

		this.time += interval;

		if (!this.animation.isDone()) {
			this.animation.frame(this.time);
			if (this._listeners) {
				this._notify('frame', {time: this.time});
			}
		}
		
		if (this.animation.isDone()) {
			this.animation.end();
			this.state = 'ended';
			if (this._listeners) {
				this._notify('state', {state: this.state});
			}
			this.animation = null;
		}
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

