

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
		return new WB.PanZoomAnimation4(this);
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




WB.PanZoomAnimation2 = WB.Animation.extend('PanZoomAnimation2', {
	
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
		
		this.startZoom = this.board.getZoomFactor();
		this.endZoom = this.panzoom.zoomFactor;
		if (this.endZoom && this.endZoom != this.startZoom) {

			// swap to ensure that zoom is growing
			this.zoomDir = 1;
			if (Math.abs(this.endZoom - 1) < Math.abs(this.startZoom - 1)) {
				var tmp = this.endZoom;
				this.endZoom = this.startZoom;
				this.startZoom = tmp;
				this.zoomDir = -1;
			}
			
			var panTo = this.panzoom.panTo;
			
			this.anchorStart = anchorPoint;
			
			// P
			this.panPoint = panTo ? panTo : this.anchorStart;
			console.log('panPoint: ' + JSON.stringify(this.panPoint));
			
			this.deltaPan = {x: panTo.x - this.anchorStart.x, y: panTo.y - this.anchorStart.y};
			console.log('deltaPan: ' + JSON.stringify(this.deltaPan));
			
			// XXX  endZoom = 1???
			this.deltaZoom = this.endZoom / (this.endZoom - 1);
			console.log('deltaZoom: ' + this.deltaZoom);
			
			// XXX velocity? this.endZoom * (this.endZoom - 1)?
			this.zoomer = new WB.NumInterpolator(this.startZoom, 
					this.endZoom, 0.5 / 1000 / rate);
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
	
	frame: function(time) {
		
		// zoom
		if (this.zoomer) {
			this.zoomer.frame(time);

	        var z = this.zoomer.getValue();
	        if (this.zoomDir == -1) {
	        	z = this.endZoom - (z - this.startZoom);
	        }
			console.log('new zoom ' + z);
			
			// XXX  startZoom = 1?
			
			// dp = DP * Z1/(Z1-1) * (Z2-1)/Z2
			var dz = this.deltaZoom * (z - 1)/z;
			console.log('dz = ' + dz);
			var dpx, dpy;
			if (this.zoomDir == 1) {
				dpx = this.deltaPan.x * dz;
				dpy = this.deltaPan.y * dz;
			} else {
				dpx = this.deltaPan.x * (1 - dz);
				dpy = this.deltaPan.y * (1 - dz);
			}
			console.log('dp = ' + dpx + ', ' + dpy);

			// P2 = P1 + dp
			var px = this.anchorStart.x + dpx;
			var py = this.anchorStart.y + dpy;
			console.log('new p = ' + px + ', ' + py);
			
	        this.board.updateZoomFactor(z, {x: px, y: py});

//			if (this.zoomDir == -1) {
//				throw "!xxx!";
//			}
	        
		} else if (this.panner) {
			// pan
			this.panner.frame(time);
	        var newPoint = this.panner.getValue();
	        this.board.updateAnchorPoint(newPoint);
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
	
	end: function() {
		var pane = this.board.animationPane;
		var resetPos = this.board.getResetPoint();
		console.log('!RESET POS! ' + JSON.stringify(resetPos));
		pane.moveTo(resetPos);
	    this.board.state({position: resetPos});
	}
	
});




WB.PanZoomAnimation3 = WB.Animation.extend('PanZoomAnimation3', {
	
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

		var anchorPoint = board.getAnchorPoint();
		var panTo = this.panzoom.panTo ? this.panzoom.panTo : anchorPoint;
		console.log('pan: ' + JSON.stringify(anchorPoint) + ' -> ' + JSON.stringify(panTo));
		
		var zoomEndPan = anchorPoint;

		if (endZoom && endZoom != startZoom) {
			
			// zoom growth = (Z1 - Z0)/Z0
			var gz = (endZoom - startZoom)/startZoom;
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
		}

		if (panTo && !WB.Geom.pointsEqual(zoomEndPan, panTo)) {
			var v = board.getBaseVelocity() / endZoom;
			console.log('tail velocity: ' + v);
			this.panner = new WB.PointInterpolator(zoomEndPan, panTo, v / 1000 * rate);
			this.panner.start(board);
			console.log('tail pan: ' + JSON.stringify(zoomEndPan) + ' -> ' + JSON.stringify(panTo));
		}

		this.startZoom = startZoom;
		this.endZoom = endZoom;
		this.endPan = panTo;
		
		// XXX: next "Multiples of Zoom"
		/**
		 * Formula:
		 *   translateX = scalePointX * (newWidth - oldWidth) / newWidth
		 * See:
		 *   http://youtu.be/lcD9CF0bxyk?t=15m57s 
		 */
		throw "!xxx!";
	},
	
	frame: function(time) {
		
		// zoom
		if (this.zoomer) {
			this.zoomer.frame(time);

			// z = Z0 * (gz + 1)
	        var gz = this.zoomer.getValue();
	        var z = this.startZoom * (gz + 1);
			console.log('new zoom ' + z);
			
	        this.board.updateZoomFactor(z); // , {x: px, y: py}
	        
		} else if (this.panner) {
			
			// pan
			this.panner.frame(time);
	        this.board.updateAnchorPoint(this.panner.getValue());
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
	
	end: function() {
		
        this.board.updateZoomFactor(this.endZoom, this.endPan);
        
		var pane = this.board.animationPane;
		var resetPos = this.board.getResetPoint();
		console.log('!RESET POS! ' + JSON.stringify(resetPos));
		pane.moveTo(resetPos);
	    this.board.state({position: resetPos});
	}
	
});




WB.PanZoomAnimation3 = WB.Animation.extend('PanZoomAnimation3', {
	
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

		var anchorPoint = board.getAnchorPoint();
		var panTo = this.panzoom.panTo ? this.panzoom.panTo : anchorPoint;
		console.log('pan: ' + JSON.stringify(anchorPoint) + ' -> ' + JSON.stringify(panTo));
		
		var zoomEndPan = anchorPoint;

		if (endZoom && endZoom != startZoom) {
			
			// zoom growth = (Z1 - Z0)/Z0
			var gz = (endZoom - startZoom)/startZoom;
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
		}

		if (panTo && !WB.Geom.pointsEqual(zoomEndPan, panTo)) {
			var v = board.getBaseVelocity() / endZoom;
			console.log('tail velocity: ' + v);
			this.panner = new WB.PointInterpolator(zoomEndPan, panTo, v / 1000 * rate);
			this.panner.start(board);
			console.log('tail pan: ' + JSON.stringify(zoomEndPan) + ' -> ' + JSON.stringify(panTo));
		}

		this.startZoom = startZoom;
		this.endZoom = endZoom;
		this.endPan = panTo;
		
		// XXX: next "Multiples of Zoom"
		/**
		 * Formula:
		 *   translateX = scalePointX * (newWidth - oldWidth) / newWidth
		 * See:
		 *   http://youtu.be/lcD9CF0bxyk?t=15m57s 
		 */
		throw "!xxx!";
	},
	
	frame: function(time) {
		
		// zoom
		if (this.zoomer) {
			this.zoomer.frame(time);

			// z = Z0 * (gz + 1)
	        var gz = this.zoomer.getValue();
	        var z = this.startZoom * (gz + 1);
			console.log('new zoom ' + z);
			
	        this.board.updateZoomFactor(z); // , {x: px, y: py}
	        
		} else if (this.panner) {
			
			// pan
			this.panner.frame(time);
	        this.board.updateAnchorPoint(this.panner.getValue());
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
	
	end: function() {
		
        this.board.updateZoomFactor(this.endZoom, this.endPan);
        
		var pane = this.board.animationPane;
		var resetPos = this.board.getResetPoint();
		console.log('!RESET POS! ' + JSON.stringify(resetPos));
		pane.moveTo(resetPos);
	    this.board.state({position: resetPos});
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

		var anchor = board.getAnchorPoint();
		var origin = WB.point(0, 0);
		if (this.panzoom.origin) {
			origin = WB.Geom.addPoint(this.panzoom.origin, anchor);
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
		 * XXX
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

