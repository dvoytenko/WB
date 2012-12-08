

/**
 * Deprecated. Use PanZoomAnimation
 */
WB.PanEpisode = WB.Episode.extend('PanEpisode', {
	
	point: null,
	
	init: function(opts) {
		if (opts) {
			for (var k in opts) {
				this[k] = opts[k];
			}
		}
	},
	
	createAnimation: function() {
		return new WB.PanAnimation(this);
	}
	
});


WB.PanAnimation = WB.Animation.extend('PanAnimation', {
	
	pan: null,
	
	init: function(pan) {
		this.pan = pan;
	},
	
	start: function(board) {
		this.board = board;
		
		this.board.state({
			pointer: 'panzoom',
			height: 1.0
		});
		
		this.startPoint = board.getAnchorPoint();
		console.log('pan: startPoint = ' + JSON.stringify(this.startPoint));
		this.endPoint = this.pan.point;
		console.log('pan: endPoint = ' + JSON.stringify(this.endPoint));

		this.inter = new WB.PointInterpolator(this.startPoint, this.endPoint, 
				board.getBaseVelocity()/1000);
		this.inter.start(board);
	},
	
	isDone: function() {
		return this.inter.isDone();
	},
	
	getTimeLeft: function() {
		return this.inter.getTimeLeft();
	},
	
	frame: function(time) {
	
		this.inter.frame(time);
		
        var newPoint = this.inter.getValue();
        
        this.board.updateAnchorPoint(newPoint);
        
	    this.board.state({
	    	height: 1.0
	    });
	},
	
	end: function() {
		var pane = this.board.animationPane;
		this.board.resetPosition(true);
	}
	
});


/**
 */
WB.PanZoomEpisode = WB.Episode.extend('PanZoomEpisode', {
	
	zoomFactor: null,
	
	anchor: null,
	
	panTo: null,
	
	init: function(opts) {
		if (opts) {
			for (var k in opts) {
				console.log('set ' + k + ' = ' + JSON.stringify(opts[k]));
				this[k] = opts[k];
			}
		}
	},
	
	createAnimation: function() {
		return new WB.PanZoomAnimation(this);
	}
	
});


WB.PanZoomAnimation = WB.Animation.extend('PanZoomAnimation', {
	
	panzoom: null,
	
	init: function(panzoom) {
		this.panzoom = panzoom;
	},
	
	start: function(board) {
		this.board = board;
		
		this.board.state({
			pointer: 'panzoom',
			height: 1.0
		});

		var anchorPoint = board.getAnchorPoint();
		console.log('anchorPoint = ' + JSON.stringify(anchorPoint));
		
		var rate = 1;
		
		var startZoom = this.board.getZoomFactor();
		var zoom = this.panzoom.zoomFactor;
		if (zoom && zoom != startZoom) {
			
			var panTo = this.panzoom.panTo;
			
			// P
			var panPoint = panTo ? panTo : anchorPoint;
			console.log('panPoint: ' + JSON.stringify(panPoint));
			
			// C = -P * Z / (Z - 1)
			// XXX what if zoom is equal to 1?
			var zz = zoom / (zoom - 1);
			this.centerPoint = {x: - panPoint.x * zz, y: - panPoint.y * zz};
			console.log('centerPoint: ' + JSON.stringify(this.centerPoint));
			
			this.zoomer = new WB.NumInterpolator(startZoom, 
					zoom, 1.5 * (zoom - 1) / 1000 / rate);
			this.zoomer.start(board);
			console.log('zoomer velocity ' + this.zoomer.velocity 
					+ '; time: ' + this.zoomer.totalTime);
		} else {

			var panTo = this.panzoom.panTo;
			console.log('panTo: ' + JSON.stringify(panTo));
			if (panTo && (panTo.x != anchorPoint.x || panTo.y != anchorPoint.y)) {
				this.panner = new WB.PointInterpolator(anchorPoint, panTo, 
						board.getBaseVelocity()/rate);
				this.panner.start(board);
			}
		}
	},
	
	isDone: function() {
		if (this.zoomer && !this.zoomer.isDone()) {
			return false;
		}
		if (this.panner && !this.panner.isDone()) {
			return false;
		}
		return true;
	},
	
	getTimeLeft: function() {
		var t = 150;
		if (this.panner && this.panner.getTimeLeft() < t) {
			t = this.panner.getTimeLeft();
		}
		if (this.zoomer && this.zoomer.getTimeLeft() < t) {
			t = this.zoomer.getTimeLeft();
		}
		return t;
	},
	
	frame: function(time) {
		
		// zoom
		if (this.zoomer) {
			this.zoomer.frame(time);
			
	        var z = this.zoomer.getValue();
			console.log('new zoom ' + z);
			
			// c = C / z
			var cx = this.centerPoint.x / z;
			var cy = this.centerPoint.y / z;
			console.log('new center ' + cx + ', ' + cy);
			
			// p = c - C
			var px = cx - this.centerPoint.x;
			var py = cy - this.centerPoint.y;
			console.log('new pan ' + px + ', ' + py);
			
	        this.board.updateZoomFactor(z, {x: px, y: py});
	        
		} else if (this.panner) {
			// pan
			this.panner.frame(time);
	        var newPoint = this.panner.getValue();
	        this.board.updateAnchorPoint(newPoint);
		}
	},
	
	end: function() {
		var pane = this.board.animationPane;
		var resetPos = this.board.getResetPoint();
		console.log('!RESET POS! ' + JSON.stringify(resetPos));
		pane.moveTo(resetPos);
	    this.board.state({position: resetPos});
	}
	
});

