
WB.Font = WB.Class.extend({
	
	glyphMap: null,
	
	missingGlyph: null,
	
	baseHeight: null,
	
	baseAdvX: null,
	
	init: function(opts) {
		if (opts) {
			for (var k in opts) {
				this[k] = opts[k];
			}
		}
	},
	
	getGlyph: function(c) {
		var glyph = this.glyphMap[c];
		if (glyph) {
			return glyph;
		}
		return this.missingGlyph;
	}
	
});


WB.Glyph = WB.Class.extend({
	
	pathSegment: null,
	
	advX: null,
	
	init: function(opts) {
		if (opts) {
			for (var k in opts) {
				this[k] = opts[k];
			}
		}
	}
	
});


WB.TextShape = WB.Shape.extend({
	
	text: null,
	
	font: null,
	
	fontHeight: null,
	
	startPoint: null,
	
	init: function(opts) {
		if (opts) {
			for (var k in opts) {
				this[k] = opts[k];
			}
		}
	},
	
	createTransform: function() {
		var tr = new WB.Transform();
		tr.translate(this.startPoint.x, this.startPoint.y);
		var baseHeight = this.font.baseHeight;
		var isResize = Math.abs(this.fontHeight - baseHeight) >= 1e-2;
		var scale = isResize ? this.fontHeight/baseHeight : 1;
		tr.scale(scale, -scale);
		tr.translate(0, -baseHeight);
		return tr;
	},
	
	draw: function(pane) {
		var baseHeight = this.font.baseHeight;
		console.log('baseHeight = ' + baseHeight);
		// final boolean isUpsideDown = this.font.isUpsideDown();
		var that = this;
		pane.withTr(this.createTransform(), function() {
			that._draw(pane);
		});
	},
	
	_draw: function(pane) {
		var p = {x: 0, y: 0};
		for (var i = 0; i < this.text.length; i++) {
			var c = this.text.substr(i, 1);
			var glyph = this.font.getGlyph(c);
			if (glyph) {
				
				// render
				if (glyph.pathSegment) {
					var tr = new WB.Transform().translate(p.x, p.y);
					pane.withTr(tr, function() {
						pane.beginPath();
						pane.moveTo({x: 0, y: 0});
						glyph.pathSegment.outline(pane);
						pane.stroke();
					});
				}
				
				// offset
				var advX = glyph.advX;
				if (!advX) {
					advX = this.font.baseAdvX;
				}
				if (advX) {
					p = WB.Geom.movePoint(p, advX, 0);
				}
			}
		}
	},
	
	createAnimation: function() {
		return new WB.TextShapeAnimation(this);
	}
	
});


WB.TextShapeAnimation = WB.ListAnimation.extend({
	
	init: function(textShape) {
		this.textShape = textShape;
		
		this.items = [];
		var p = {x:0, y:0};
		for (var i = 0; i < textShape.text.length; i++) {
			var c = textShape.text.substr(i, 1);
			var glyph = textShape.font.getGlyph(c);
			if (glyph) {
				
				if (glyph.pathSegment) {
					// move over
					this.items.push(new WB.MoveToSegment({point: p}));
					
					// render
					var tr = new WB.Transform().translate(p.x, p.y);
					this.items.push(new WB.GlyphShape(glyph, tr));
					
					// TODO remove
					//this.items.push(new WB.PauseAnimation(5000));
				}
				
				// offset
				var advX = glyph.advX;
				if (!advX) {
					advX = textShape.font.baseAdvX;
				}
				if (advX) {
					p = WB.Geom.movePoint(p, advX, 0);
				}
			}
		}

		this.tr = textShape.createTransform();
	},
	
	start: function(board) {
		var that = this;
		board.withTr(this.tr, function() {
			that._start(board);
		});
	},
	
	frame: function(time) {
		var that = this;
		this.board.withTr(this.tr, function() {
			that._frame(time);
		});
	}
	
});


WB.GlyphShape = WB.Shape.extend({
	
	glyph: null,
	
	tr: null,
	
	init: function(glyph, tr) {
		this.glyph = glyph;
		this.tr = tr;
	},
	
	draw: function(pane) {
		var that = this;
		pane.withTr(this.tr, function() {
			pane.beginPath();
			pane.moveTo({x:0, y:0});
			that.glyph.pathSegment.outline(pane);
			pane.stroke();
		});
	},
	
	createAnimation: function() {
		return new WB.GlyphAnimation(this);
	}
	
});


WB.GlyphAnimation = WB.Animation.extend({
	
	glyphShape: null,
	
	board: null,
	
	pane: null,
	
	anim: null,
	
	init: function(glyphShape) {
		this.glyphShape = glyphShape;
	},
	
	start: function(board) {
		this.board = board;
		this.pane = board.animationPane;
		this.anim = this.glyphShape.glyph.pathSegment.createAnimation();
		var that = this;
		this.board.withTr(this.glyphShape.tr, function() {
			that.anim.start(board);
		});
	},
	
	frame: function(time) {
		var that = this;
		this.board.withTr(this.glyphShape.tr, function() {
			that.pane.beginPath();
			that.pane.moveTo({x:0, y:0});
			that.anim.frame(time);
			that.pane.stroke();
		});
	},
	
	end: function() {
		var that = this;
		this.board.commitShape(this.glyphShape, true);
	},
	
	isDone: function() {
		return this.anim.isDone();
	},
	
	getTimeLeft: function() {
		return this.anim.getTimeLeft();
	}
	
});

