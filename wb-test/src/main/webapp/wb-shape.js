

WB.Shape = WB.Class.extend({
	
	getStartPoint: function() {
		return null;
	},
	
	draw: function(pane) {
	},
	
	createAnimation: function() {
		return null;
	}
	
});


WB.GroupShape = WB.Shape.extend({
	
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


WB.GroupShapeAnimation = WB.ListAnimation.extend({
	
	init: function(group) {
		this.group = group;
		this.items = [];
		if (group.shapes.length) {
			for (var i = 0; i < group.shapes.length; i++) {
				var shape = group.shapes[i];
				var p = shape.getStartPoint();
				if (p) {
					this.items.push(new WB.MoveToSegment({point: p}));
				}
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

