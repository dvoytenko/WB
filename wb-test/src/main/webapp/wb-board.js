
WB.Board = WB.Class.extend({
	
	commitPane: null,
	
	animationPane: null,
	
	baseVelocity: null,
	
	init: function(opts) {
		if (opts && opts.commitPane) {
			this.commitPane = opts.commitPane;
		}
		if (opts && opts.animationPane) {
			this.animationPane = opts.animationPane;
		}
		
		if (opts && opts.baseVelocity) {
			this.baseVelocity = opts.baseVelocity;
		} else {
			this.baseVelocity = 100;
		}
		
		this._shapes = [];
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
	}
	
});

