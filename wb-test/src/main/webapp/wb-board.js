

/**
 * State:
 * - position
 * - velocity
 * - angle
 * - height
 * - pressure
 */
WB.Board = WB.Class.extend({
	
	commitPane: null,
	
	animationPane: null,
	
	baseVelocity: null,
	
	drawingSoundEngine: null,
	
	pointer: null,
	
	speechPlayer: null,
	
	urlResolver: null,
	
	font: null,
	
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
		
		if (opts && opts.font) {
			this.font = opts.font;
		}
		
		if (opts && opts.baseVelocity) {
			this.baseVelocity = opts.baseVelocity;
		} else {
			this.baseVelocity = 100;
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
		// TODO: get the current transform
		this._shapes.push(shape);
		if (render) {
			shape.draw(this.commitPane);
		}
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
	
	afterFrame: function() {
		if (this.drawingSoundEngine) {
			this.drawingSoundEngine.update(this._state);
		}
		if (this.pointer) {
			this.pointer.update(this._state);
		}
	}
	
});

