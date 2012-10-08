

WB.EraserEpisode = WB.Episode.extend({
	
	eraseAll: false,
	
	init: function(opts) {
		if (opts) {
			for (var k in opts) {
				this[k] = opts[k];
			}
		}
	},
	
	_eraser: function() {
		return new WB.EraserShape({
			eraseAll: this.eraseAll
		});
	},
	
	createAnimation: function() {
		return this._eraser().createAnimation();
	}
	
});


WB.EraserShape = WB.Shape.extend({
	
	eraseAll: false,

	transform: null,
	
	pathSegment: null,
	
	init: function(opts) {
		if (opts) {
			for (var k in opts) {
				this[k] = opts[k];
			}
		}
	},
	
	draw: function(pane) {
		if (this.eraseAll) {
			pane._clearCanvas();
		} else {
			if (!this.transform) {
				this._draw(pane);
			} else {
				var that = this;
				pane.withTr(this.transform, function() {
					that._draw(pane);
				});
			}
		}
	},
	
	_draw: function(pane) {
		
		var context = pane.context;
		
		context.save();
		
		// source-atop: works, but makes black lines white
		// source-in: makes destination transparent!
		// destination-in: the source image is transparent
		// destination-out: ??? could work with 0 color
		
		context.globalCompositeOperation = 'destination-out';
		context.fillStyle = 'rgb(0,0,0,0.5)';
		
		context.beginPath();
		this.pathSegment.outline(pane);
		context.clip();
		
//		context.fill();
		
		/* alpha: 1 - not transparent, 0 - completely transparent
		var grad = context.createRadialGradient(190, 240, 20, 
				170, 250, 100);
		grad.addColorStop(0, 'rgba(255,255,255,0.99)');
		grad.addColorStop(1, 'rgba(255,255,255,0.21)');
		context.fillStyle = grad;
		*/

		context.fillRect(90, 190, 170, 120);
		
		/*
			var grad = context.createLinearGradient(200, 90, 200, 170);
			grad.addColorStop(0, 'rgba(255,255,255,0.98)');
			grad.addColorStop(0.7, 'rgba(255,255,255,0.85)');
			grad.addColorStop(1, 'rgba(255,255,255,0.21)'); // white
			context.fillStyle = grad;
			context.fillRect(200, 90, 80, 80);
			context.strokeRect(200, 90, 80, 80);
		 */
		
		context.restore();
	},
	
	createAnimation: function() {
		return new WB.EraserAnimation(this);
	}
	
});


WB.EraserAnimation = WB.Animation.extend({
	
	eraser: null,
	
	init: function(eraser) {
		this.eraser = eraser;
	},
	
	start: function(board) {
		// TODO different types
		this.eraser.draw(board.commitPane);
	},
	
	isDone: function() {
		return true;
	},
	
	end: function() {
	},
	
	frame: function(time) {
	},
	
	getTimeLeft: function() {
		return 0;
	}
	
});

