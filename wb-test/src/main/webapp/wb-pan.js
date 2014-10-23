

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
		if (!this.zoomFactor) {
			var point = this.panTo;
			var pan = new WB.PanEpisode({point: point});
			return new WB.PanAnimation(pan);
		}
		return new WB.PanZoomAnimation4(this);
	}
	
});


WB.PanZoomAnimation4 = WB.Animation.extend('PanZoomAnimation4', {
	
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
		
		var startZoom = this.board.getZoomFactor();
		var endZoom = this.panzoom.zoomFactor ? this.panzoom.zoomFactor : startZoom;

		/*
		 * current anchor: {"x":-350.625,"y":-187.75}
		 * origin: {x: 370, y: 170}
		 * origin' = origin + anchor: {x: 19.375, y: -17.75}
		 *   origin'' = origin - anchor: {x: -720, y: -358}
		 * zz: (1.1 - 8) / 1.1 = -6.2727
		 * tp': zz * origin' = {x: -121.53, y: 111.34}
		 * tp: anchor - tp' = {x: -229.095, y: -299.09}
		 */
		var anchor = board.getAnchorPoint();
		console.log('current anchor: ' + JSON.stringify(anchor));
		var origin = WB.point(0, 0);
		if (this.panzoom.origin) {
			origin = WB.Geom.addPoint(this.panzoom.origin, anchor);
			/*
			var zz = (z - this.startZoom) / z;
			var newAnchor = WB.Geom.addPoint(this.anchor,
					WB.point(- this.origin.x * zz, - this.origin.y * zz));
			 */
		} else if (this.panzoom.panTo) {
			var panTo = this.panzoom.panTo;
			var zz = endZoom / (endZoom - startZoom);
			var tp = WB.point(- panTo.x * zz, - panTo.y * zz);
			origin = WB.Geom.addPoint(tp, anchor);
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
		if (endZoom && endZoom != startZoom) {
			
			// zoom growth = (Z1 - Z0) / Z0
			var gz = (endZoom - startZoom) / startZoom;
			console.log('gz = ' + gz);
			
			this.zoomer = new WB.NumInterpolator(0, gz, 1 / 1000 * rate);
			this.zoomer.start(board);
			console.log('zoomer velocity ' + this.zoomer.velocity 
					+ '; time: ' + this.zoomer.totalTime);
			
			var zoomTime = this.zoomer.totalTime;
			
			// zoom distance = BV * ln(Z1) / (Z1 - 1)
			var adjVelo = endZoom == 1 ? 1 : Math.log(endZoom) / (endZoom - 1);
			var zoomDist = (board.getBaseVelocity()/1000 * rate) * adjVelo * zoomTime;
			console.log('zoomDist: ' + zoomDist);

			/*
			var totalDist = WB.Geom.distance(anchorPoint, panTo);
			console.log('totalDist: ' + totalDist);
			
			if (totalDist > 1) {
				if (totalDist <= zoomDist) {
					zoomEndPan = panTo;
				} else {
					var dd = zoomDist/totalDist;
					zoomEndPan = {x: (anchorPoint.x + panTo.x) * dd, 
							y: (anchorPoint.y + panTo.y) * dd};
				}
			}
			console.log('zoomEndPan: ' + JSON.stringify(zoomEndPan));
			*/
		}

		/*
		if (panTo && !WB.Geom.pointsEqual(zoomEndPan, panTo)) {
			var v = board.getBaseVelocity() / endZoom;
			console.log('tail velocity: ' + v);
			this.panner = new WB.PointInterpolator(zoomEndPan, panTo, v / 1000 * rate);
			this.panner.start(board);
			console.log('tail pan: ' + JSON.stringify(zoomEndPan) + ' -> ' + JSON.stringify(panTo));
		}
		*/

		this.startZoom = startZoom;
		this.endZoom = endZoom;
		this.anchor = anchor;
		this.origin = origin;
	},
	
	frame: function(time) {

		this.zoomer.frame(time);

		// z = Z0 * (gz + 1)
        var gz = this.zoomer.getValue();
        var z = this.startZoom * (gz + 1);
		console.log('new zoom ' + z);
		
		var zz = (z - this.startZoom) / z;
		var tp = WB.Geom.addPoint(this.anchor,
				WB.point(- this.origin.x * zz, - this.origin.y * zz));
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
		
		var zz = (this.endZoom - this.startZoom) / this.endZoom;
		var tp = WB.Geom.addPoint(this.anchor,
				WB.point(- this.origin.x * zz, - this.origin.y * zz));
		console.log('end anchor: ' + JSON.stringify(tp));
        this.board.updateZoomFactor(this.endZoom, tp);
        
		var pane = this.board.animationPane;
		var resetPos = this.board.getResetPoint();
		console.log('!RESET POS! ' + JSON.stringify(resetPos));
		pane.moveTo(resetPos);
	    this.board.state({position: resetPos});
	}
	
});

