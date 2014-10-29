

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
	
	origin: null,
	
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


WB.PanZoomAnimation = WB.AnimationDelegate.extend('PanZoomAnimation', {
	
	init: function(panZoomEpisode) {
		this.panZoomEpisode = panZoomEpisode;
	},
	
	start: function(board) {
		this.board = board;
		this.pane = board.animationPane;
		
		this.animation = this.createAnimation_(board);
		this.animation.start(board);
	},
	
	createAnimation_: function(board) {
		var startZoom = board.getZoomFactor();
		var endZoom = this.panZoomEpisode.zoomFactor || startZoom;
		if (startZoom == endZoom) {
			return new WB.PanAnimation(new WB.PanEpisode(
					{point: this.panZoomEpisode.panTo}));
		}
		return new WB.PanZoomAnimationZoomer(this.panZoomEpisode);
	}
});


WB.PanZoomAnimationZoomer = WB.Animation.extend('PanZoomAnimationZoomer', {
	
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

		var rate = 1;
		
		var z0 = this.board.getZoomFactor();
		var z1 = this.panzoom.zoomFactor ? this.panzoom.zoomFactor : startZoom;
		if (z0 == z1) {
			throw 'startZoom == endZoom';
		}
		var startZoom = Math.min(z0, z1);
		var endZoom = Math.max(z0, z1);
		var dir = z0 < z1 ? 1 : -1;

		var anchor = board.getAnchorPoint();
		console.log('current anchor: ' + JSON.stringify(anchor));
		var origin = WB.point(0, 0);
		if (this.panzoom.origin) {
			origin = WB.Geom.addPoint(this.panzoom.origin, anchor);
		} else if (this.panzoom.panTo) {
			var panTo = this.panzoom.panTo;
			var zz = endZoom / (endZoom - startZoom);
			if (dir == 1) {
				// origin = anchor - panTo * zz
				origin = WB.Geom.addPoint(anchor, WB.Geom.multiPoint(panTo, zz), -1);
			} else {
				// origin = panTo + anchor * zz
				origin = WB.Geom.addPoint(panTo, WB.Geom.multiPoint(panTo, zz), 1);
			}
			console.log('pan: ' + JSON.stringify(panTo) + ' -> ' + JSON.stringify(origin));
		}
		console.log('origin: ' + JSON.stringify(origin) + '; at anchor: ' +
				JSON.stringify(anchor));
		
		/**
		 * Formula:
		 *   translateX = scalePointX * (newWidth - oldWidth) / newWidth
		 * See:
		 *   http://youtu.be/lcD9CF0bxyk?t=15m57s 
		 */
		// zoom growth = (Z1 - Z0) / Z0
		var gz = (endZoom - startZoom) / startZoom;
		console.log('gz = ' + gz);
		
		if (dir == 1) {
			this.zoomer = new WB.NumInterpolator(0, gz, 1 / 1000 * rate);
		} else {
			this.zoomer = new WB.NumInterpolator(gz, 0, 1 / 1000 * rate);
		}
		this.zoomer.start(board);
		console.log('zoomer velocity ' + this.zoomer.velocity 
				+ '; time: ' + this.zoomer.totalTime);
		
		var zoomTime = this.zoomer.totalTime;
		
		// zoom distance = BV * ln(Z1) / (Z1 - 1)
		var adjVelo = endZoom == 1 ? 1 : Math.log(endZoom) / (endZoom - 1);
		var zoomDist = (board.getBaseVelocity()/1000 * rate) * adjVelo * zoomTime;
		console.log('zoomDist: ' + zoomDist);

		// startZoom < endZoom so that the back zooming could be simply played back
		this.startZoom = startZoom;
		this.endZoom = endZoom;
		this.dir = dir;
		this.origin = origin;
		this.anchor = anchor;
		this.endAnchor = WB.Geom.multiPoint(WB.Geom.addPoint(anchor, origin, -1),
				(endZoom - startZoom) / endZoom);
	},
	
	frame: function(time) {

		this.zoomer.frame(time);

		// z = Z0 * (gz + 1)
        var gz = this.zoomer.getValue();
        var z = this.startZoom * (gz + 1);
		console.log('new zoom ' + z);
		
		var zz = (z - this.startZoom) / z;
		var tp = WB.point((this.anchor.x - this.origin.x) * zz,
				(this.anchor.y - this.origin.y) * zz);
		console.log('new zz: ' + zz);
		console.log('new tp\': ' + JSON.stringify(WB.point(this.origin.x * zz, this.origin.y * zz)));
		if (this.panzoom.origin) {
			console.log('new origin\': ' + JSON.stringify(
					WB.Geom.addPoint(this.panzoom.origin, tp)
					));
		}
		console.log('new anchor: ' + JSON.stringify(tp));
        this.board.updateZoomFactor(z, tp);
        // updateAnchorPoint
	},
	
	isDone: function() {
		return this.zoomer.isDone();
	},
	
	getTimeLeft: function() {
		var t = 150;
		if (this.zoomer && this.zoomer.getTimeLeft() < t) {
			t = this.zoomer.getTimeLeft();
		}
		return t;
	},
	
	end: function() {
		
		console.log('end anchor: ' + JSON.stringify(this.endAnchor));
        this.board.updateZoomFactor(this.endZoom, this.endAnchor);
        
		var pane = this.board.animationPane;
		var resetPos = this.board.getResetPoint();
		console.log('!RESET POS! ' + JSON.stringify(resetPos));
		pane.moveTo(resetPos);
	    this.board.state({position: resetPos});
	}
	
});

