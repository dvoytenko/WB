

/**
 */
WB.Glyph = WB.Class.extend('Glyph', {
	
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


/**
 */
WB.DrawTextEpisode = WB.ShapeEpisodeBase.extend('DrawTextEpisode', {
});


/**
 */
WB.TextShape = WB.Shape.extend('TextShape', {
	
	text: null,
	
	glyphs: null,
	
	height: null,
	
	width: null,
	
	init: function(opts) {
		if (opts) {
			for (var k in opts) {
				this[k] = opts[k];
			}
		}
	},
	
	createTransform: function() {
		var tr = new WB.Transform();
		tr.scale(1, -1);
		tr.translate(0, -this.height);
		return tr;
	},
	
	draw: function(pane) {
		var that = this;
		pane.withTr(this.createTransform(), function() {
			that._draw(pane);
		});
	},
	
	_draw: function(pane) {
		var p = {x: 0, y: 0};
		for (var i = 0; i < this.glyphs.length; i++) {
			var glyph = this.glyphs[i];
			
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
			if (glyph.advX) {
				p = WB.Geom.movePoint(p, glyph.advX, 0);
			}
		}
	},
	
	createAnimation: function() {
		return new WB.TextShapeAnimation(this);
	}
	
});


WB.TextShapeAnimation = WB.ListAnimation.extend('TextShapeAnimation', {
	
	init: function(textShape) {
		this.textShape = textShape;
		
		this.items = [];
		var p = {x:0, y:0};
		
		for (var i = 0; i < textShape.glyphs.length; i++) {
			var glyph = textShape.glyphs[i];
			
			// render
			if (glyph.pathSegment) {
				// move over
				this.items.push(new WB.MoveToSegment({point: p}));
				
				// render
				var tr = new WB.Transform().translate(p.x, p.y);
				this.items.push(new WB.GlyphShape(glyph, tr));
			}
			
			// offset
			if (glyph.advX) {
				p = WB.Geom.movePoint(p, glyph.advX, 0);
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


WB.GlyphShape = WB.Shape.extend('GlyphShape', {
	
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


WB.GlyphAnimation = WB.Animation.extend('GlyphAnimation', {
	
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

