

WB.PathBasedShape = WB.Shape.extend('PathBasedShape', {
	
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


/**
 * 
 */
WB.RectangleShape = WB.PathBasedShape.extend('RectangleShape', {
	
	topleft: null,
	
	width: null,
	
	height: null,
	
	init: function(opts) {
		if (opts && opts.topleft) {
			this.topleft = opts.topleft;
		}
		if (!this.topleft) {
			this.topleft = {x: 0, y: 0};
		}
		if (opts && opts.width) {
			this.width = opts.width;
		}
		if (opts && opts.height) {
			this.height = opts.height;
		}
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
		segments.push(new WB.ClosePathSegment());
		
		return segments;
	}
	
});


/**
 * http://www.w3.org/TR/SVG/shapes.html#EllipseElement
 */
WB.EllipseShape = WB.PathBasedShape.extend('EllipseShape', {
	
	center: null,
	
	radiusX: null,
	
	radiusY: null,
	
	init: function(opts) {
		if (opts && opts.center) {
			this.center = opts.center;
		}
		if (!this.center) {
			this.center = {x: 0, y: 0};
		}
		if (opts && opts.radiusX) {
			this.radiusX = opts.radiusX;
		}
		if (opts && opts.radiusY) {
			this.radiusY = opts.radiusY;
		}
	},
	
	resolveSegments: function() {
		var segments = [];
		// segments.push(new WB.MoveToSegment({point: this.topleft}));
		
		segments.push(new WB.ArcSegment({
			arc: {
				center: this.center, 
				radiusX: this.radiusX, 
				radiusY: this.radiusY, 
				xAxisRotation: 0, 
				startAngle: Math.PI, 
				endAngle: Math.PI * 3, 
				counterclockwise: false
				}
		}));
		
		return segments;
	}
	
});


/**
 * http://www.w3.org/TR/SVG/shapes.html#PolylineElement
 */
WB.PolylineShape = WB.PathBasedShape.extend('PolylineShape', {
	
	/**
	 * array of points
	 */
	points: null,
	
	init: function(opts) {
		if (opts && opts.points) {
			this.points = opts.points;
		}
	},
	
	resolveSegments: function() {
		var segments = [];
		segments.push(new WB.MoveToSegment({point: this.points[0]}));
		for (var i = 1; i < this.points.length; i++) {
			segments.push(new WB.LineToSegment({point: this.points[i]}));
		}
		return segments;
	}
	
});


/**
 * http://www.w3.org/TR/SVG/shapes.html#PolygonElement
 */
WB.PolygonShape = WB.PathBasedShape.extend('PolygonShape', {
	
	/**
	 * Array<Point>
	 */
	points: null,
	
	init: function(opts) {
		if (opts && opts.points) {
			this.points = opts.points;
		}
	},
	
	resolveSegments: function() {
		var segments = [];
		segments.push(new WB.MoveToSegment({point: this.points[0]}));
		for (var i = 1; i < this.points.length; i++) {
			segments.push(new WB.LineToSegment({point: this.points[i]}));
		}
		segments.push(new WB.ClosePathSegment());
		return segments;
	}
	
});

