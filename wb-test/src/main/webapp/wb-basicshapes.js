

WB.PathBasedShape = WB.Shape.extend({
	
	getStartPoint: function() {
		return _path().getStartPoint();
	},
	
	resolveSegments: function() {
		return [];
	},
	
	_path: function() {
		if (!this._pathCache) {
			this._pathCache = new WB.PathShape({
				segments: this.resolveSegments()
			});
		}
		return this._pathCache;
	},
	
	draw: function(pane) {
		this._path().draw(pane);
	},
	
	createAnimation: function() {
		return this._path().createAnimation();
	}
	
});


WB.RectangleShape = WB.PathBasedShape.extend({
	
	topleft: null,
	
	width: null,
	
	height: null,
	
	init: function(opts) {
		if (opts && opts.topleft) {
			this.topleft = opts.topleft;
		}
		if (opts && opts.width) {
			this.width = opts.width;
		}
		if (opts && opts.height) {
			this.height = opts.height;
		}
	},
	
	getStartPoint: function() {
		return this.topleft;
	},
	
	resolveSegments: function() {
		var segments = [];
		segments.push(new WB.MoveToSegment({point: this.topleft}));
		segments.push(new WB.LineToSegment({
			point: {
				x: this.topleft.x + this.width, 
				y: this.topleft.y
				}
		}));
		segments.push(new WB.LineToSegment({
			point: {
				x: this.topleft.x + this.width, 
				y: this.topleft.y + this.height
				}
		}));
		segments.push(new WB.LineToSegment({
			point: {
				x: this.topleft.x, 
				y: this.topleft.y + this.height
				}
		}));
		// TODO close segment instead
		segments.push(new WB.LineToSegment({point: this.topleft}));
		
		return segments;
	}
	
});

