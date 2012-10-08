
WB.Segment = WB.Class.extend({
	
	// potentially consider prevSegment and nextSegment in this contract
	outline: function(pane) {
	},
	
	createAnimation: function() {
		return null;
	}
	
});


WB.MoveToSegment = WB.Segment.extend({
	
	point: null,
	
	init: function(opts) {
		if (opts && opts.point) {
			this.point = opts.point;
		}
	},
	
	outline: function(pane) {
		pane.moveTo(this.point);
	},
	
	createAnimation: function() {
		return new WB.MoveToAnimation(this);
	}
	
});


WB.MoveToAnimation = WB.Animation.extend({
	
	moveto: null,
	
	init: function(moveto) {
		this.moveto = moveto;
	},
	
	start: function(board) {
		this.board = board;
	},
	
	isDone: function() {
		return this.done;
	},
	
	end: function() {
	},
	
	frame: function(time) {
		// TODO: implement moving cursor + timeLeft
		var pane = this.board.animationPane;
		pane.moveTo(this.moveto.point);
		this.done = true;
		
		this.board.state({
			position: pane.toGlobalPoint(this.moveto.point),
			velocity: 0,
			angle: 0,
			pressure: 0,
			height: 0
		});
	},
	
	getTimeLeft: function() {
		// TODO
		return 0;
	}
	
});


WB.ClosePathSegment = WB.Segment.extend({
	
	outline: function(pane) {
		var p = pane.getPathStartPoint();
		if (p) {
			pane.lineTo(p);
		}
	},
	
	createAnimation: function() {
		return new WB.ClosePathAnimation();
	}
	
});


WB.ClosePathAnimation = WB.Animation.extend({
	
	start: function(board) {
		this.board = board;
		this.pane = board.animationPane;
		this.pathStart = this.pane.getPathStartPoint();
		if (this.pathStart) {
			this.animation = new WB.LineAnimation(null, this.pathStart);
			this.animation.start(board);
		}
	},
	
	frame: function(time) {
		if (this.animation) {
			this.animation.frame(time);
		}
	},
	
	isDone: function() {
		if (this.animation) {
			return this.animation.isDone();
		}
		return true;
	},
	
	end: function() {
		if (this.animation) {
			this.animation.end();
		}
	},
	
	getTimeLeft: function() {
		if (this.animation) {
			return this.animation.getTimeLeft();
		}
		return 0;
	}
	
});


WB.LineSegment = WB.Segment.extend({
	
	resolvePoint: function(pane) {
		return null;
	},
	
	outline: function(pane) {
        //console.log('outline start: ' + JSON.stringify(pane.getCurrentPoint()));
        //console.log('outline end: ' + JSON.stringify(this.resolvePoint(pane)));
		pane.lineTo(this.resolvePoint(pane));
	},
	
	createAnimation: function() {
		return new WB.LineAnimation(this);
	}
	
});

WB.LineToSegment = WB.LineSegment.extend({
	
	point: null,
	
	init: function(opts) {
		if (opts && opts.point) {
			this.point = opts.point;
		}
	},
	
	resolvePoint: function(pane) {
		return this.point;
	}
	
});


WB.LineAnimation = WB.Animation.extend({
	
	line: null,
	
	init: function(line, endPoint) {
		this.line = line;
		this.endPoint = endPoint;
	},
	
	start: function(board) {
		this.board = board;
		this.velocity = board.getBaseVelocity();
		
		this.pane = board.animationPane;
		this.startPoint = this.pane.getCurrentPoint();
		if (!this.endPoint) {
			this.endPoint = this.line.resolvePoint(this.pane);
		}
		
    	this.dx = this.endPoint.x - this.startPoint.x;
    	this.dy = this.endPoint.y - this.startPoint.y;
        this.totalDistance = this.pane.distanceGlobal(this.startPoint, 
        		this.endPoint, false, true);
//        console.log('start: ' + JSON.stringify(this.startPoint));
//        console.log('end: ' + JSON.stringify(this.endPoint));
//        console.log('total distance: ' + this.totalDistance);
//        console.log('velocity: ' + this.velocity);
        // time - this.totalDistance / this.velocity * 1000;
        this.timeLeft = 0;
        this.done = this.totalDistance < 1.0;
	},
	
	isDone: function() {
		return this.done;
	},
	
	getTimeLeft: function() {
		return this.timeLeft;
	},
	
	frame: function(time) {
		
        // var distance = Math.min(time * this.velocity / 1000, this.totalDistance);
        // console.log('distance: ' + distance + ' vs total ' + this.totalDistance);
		var distance = time * this.velocity / 1000;
		if (distance > this.totalDistance) {
	        this.timeLeft = time - this.totalDistance / this.velocity * 1000;
	        // console.log('timeLeft: ' + this.timeLeft + ' from ' + time);
			distance = this.totalDistance;
		}
        
        var x2 = this.dx * distance/this.totalDistance;
        var y2 = this.dx != 0 ? (this.dy/this.dx) * x2 : 
			this.dy * distance/this.totalDistance;
		
        var newPoint = WB.Geom.movePoint(this.startPoint, x2, y2);
		
		this.pane.lineTo(newPoint);
		
	    this.done = Math.abs(this.totalDistance - distance) < 1.0;
	    
	    this.board.state({
	    	position: this.pane.toGlobalPoint(newPoint),
	    	velocity: this.velocity,
	    	angle: WB.Geom.angle(this.startPoint, newPoint),
	    	height: 0.0
	    });
	}
	
});


/**
 * Arc{Point center, double radiusX, double radiusY, double xAxisRotation, 
 * 		double startAngle, double endAngle, boolean counterclockwise}
 */
WB.ArcSegment = WB.Segment.extend({
	
	arc: null,
	
	init: function(opts) {
		if (opts && opts.arc) {
			this.arc = opts.arc;
		}
	},
	
	resolveArc: function(pane) {
		return this.arc;
	},
	
	outline: function(pane) {
		var arc = this.resolveArc(pane);
		
		var isCircle = Math.abs(arc.radiusX - arc.radiusY) < 1e-2;
		var isRotated = Math.abs(arc.xAxisRotation) >= 1e-2;
		
		if (isCircle && !isRotated) {
			// simple arc
			pane.arc(arc.center, arc.radiusX, arc.startAngle, arc.endAngle, 
					arc.counterclockwise);
		} else {
			
			var r = Math.max(arc.radiusX, arc.radiusY); 
            //console.log('- radius: ' + r);
            
            var tr = new WB.Transform();
            tr.translate(arc.center.x, arc.center.y);
            //console.log('- translate: ' + JSON.stringify(pane.toGlobalPoint(arc.center)));
            if (isRotated) {
            	tr.rotate(arc.xAxisRotation);
                //console.log('- rotate: ' + arc.xAxisRotation);
            }
            if (!isCircle) {
            	tr.scale(arc.radiusX/r, arc.radiusY/r);
            //    console.log('- scale: ' + (arc.radiusX/r) + ', ' + (arc.radiusY/r));
            }
            
            //console.log('1: ' + JSON.stringify(pane.toGlobalPoint({x: 0, y: 0})));
            
            //console.log(JSON.stringify(pane.currentTransform.m));
            
            pane.withTr(tr, function() {
                //console.log('2: ' + JSON.stringify(pane.toGlobalPoint({x: 0, y: 0})));
                //console.log(JSON.stringify(pane.currentTransform.m));
				pane.arc({x: 0, y: 0}, r, arc.startAngle, 
						arc.endAngle, arc.counterclockwise);
			});
		}
	},
	
	createAnimation: function() {
		return new WB.ArcSegmentAnimation(this);
	}
	
});


WB.ArcSegmentAnimation = WB.Animation.extend({
	
	init: function(arcsegm) {
		this.arcsegm = arcsegm;
	},
	
	start: function(board) {
		this.board = board;
		this.pane = board.animationPane;
		this.velocity = board.getBaseVelocity();
		
		this.arc = this.arcsegm.resolveArc(this.pane);
        this.da = this.arc.endAngle - this.arc.startAngle;
		this.done = false;
	},
	
	isDone: function() {
		return this.done;
	},
	
	end: function() {
	},
	
	frame: function(time) {
		
		var isCircle = Math.abs(this.arc.radiusX - this.arc.radiusY) < 1e-2;
		var isRotated = Math.abs(this.arc.xAxisRotation) >= 1e-2;
		
		var eap;
		if (isCircle) {
			// simple arc
	        var globalRadius = this.pane.globalLength(this.arc.radiusX);
	        var totalDistance = Math.abs(this.da) * globalRadius;
	        var distance = this.velocity * time / 1000;
		    if (distance > totalDistance) {
		    	distance = totalDistance;
		    }
			eap = this.arc.startAngle + this.da*distance/totalDistance;
			if (!isRotated) {
				this.pane.arc(this.arc.center, this.arc.radiusX, this.arc.startAngle, 
						eap, this.arc.counterclockwise);
				this.lastPoint = pane.toGlobalPoint({x: Math.cos(eap) * this.arc.radiusX, 
			    		y: Math.sin(eap) * this.arc.radiusX});
				if (!this.endPoint) {
					this.endPoint = this.pane.toGlobalPoint({
						x: Math.cos(this.arc.endAngle) * this.arc.radiusX, 
			    		y: Math.sin(this.arc.endAngle) * this.arc.radiusX});
				}
			} else {
	            var tr = new WB.Transform();
	            tr.translate(this.arc.center.x, this.arc.center.y);
            	tr.rotate(this.arc.xAxisRotation);
    			var that = this;
    			that.pane.withTr(tr, function() {
    				that.pane.arc({x:0, y:0}, that.arc.radiusX, that.arc.startAngle, 
    						eap, that.arc.counterclockwise);
    				that.lastPoint = that.pane.toGlobalPoint({x: Math.cos(eap) * that.arc.radiusX, 
    			    		y: Math.sin(eap) * that.arc.radiusX});
    				if (!that.endPoint) {
    					that.endPoint = that.pane.toGlobalPoint({
    						x: Math.cos(that.arc.endAngle) * that.arc.radiusX, 
    			    		y: Math.sin(that.arc.endAngle) * that.arc.radiusX});
    				}
    			});
			}
		} else {
			
			var r = Math.max(this.arc.radiusX, this.arc.radiusY);
			
			if (!this.lastAngle) {
				this.lastAngle = this.arc.startAngle;
				
			}
            
            var tr = new WB.Transform();
            tr.translate(this.arc.center.x, this.arc.center.y);
            if (isRotated) {
            	tr.rotate(this.arc.xAxisRotation);
            }
            if (!isCircle) {
            	tr.scale(this.arc.radiusX/r, this.arc.radiusY/r);
            }

            // TODO wrong: arc length is not always the same!!!
            // can do it better using Rz = Rx*cos(a) + Ry*sin(a) and saving lastAngle
	        var globalRadius = this.pane.globalLength(this.arc.radiusX);
	        var totalDistance = Math.abs(this.da) * globalRadius;
	        var distance = this.velocity * time / 1000;
		    if (distance > totalDistance) {
		    	distance = totalDistance;
		    }
			eap = this.arc.startAngle + this.da * distance/totalDistance;
            
			var that = this;
            this.pane.withTr(tr, function() {
				that.pane.arc({x:0, y:0}, r, that.arc.startAngle, 
						eap, that.arc.counterclockwise);
				that.lastPoint = that.pane.toGlobalPoint({x: Math.cos(eap) * r, 
			    		y: Math.sin(eap) * r});
				if (!that.endPoint) {
					that.endPoint = that.pane.toGlobalPoint({
						x: Math.cos(that.arc.endAngle) * r, 
			    		y: Math.sin(that.arc.endAngle) * r});
				}
			});
		}

		var dist = WB.Geom.distance(this.lastPoint, this.endPoint);
		// console.log('arc frame: ' + eap + ' vs ' + this.arc.endAngle + '; distance left: ' + dist);
		this.done = dist < 1;
		
		this.board.state({
			position: this.lastPoint,
			velocity: this.velocity,
			angle: this.da >= 0 ? WB.Geom.PI_HALF + eap : WB.Geom.PI_1_HALF + eap,
			height: 0
		});
	}
	
});


WB.ArcToSvgSegment = WB.ArcSegment.extend({

	radiusX: null,
	
	radiusY: null,
	
	xAxisRotation: null,
	
	largeArcFlag: null,
	
	sweepFlag: null,
	
	endPoint: null,

	init: function(opts) {
		for (var k in opts) {
			this[k] = opts[k];
		}
	},
	
	resolveArc: function(pane) {
		
		var angle = function(u, v) {
			return (u.x * v.y < u.y * v.x ? -1 : 1) * Math.acos(ratio(u, v));
		};
		var ratio = function(u, v) {
			return (u.x * v.x + u.y * v.y) / (mag(u) * mag(v));
		};
		var mag = function(v) {
			return Math.sqrt(v.x * v.x + v.y * v.y);
		};
		
		var cp = pane.getCurrentPoint();
		var psi = this.xAxisRotation;
		var x1 = cp.x;
		var y1 = cp.y;
		var x2 = this.endPoint.x;
		var y2 = this.endPoint.y;
		var rx = this.radiusX;
		var ry = this.radiusY;
		var fa = this.largeArcFlag;
		var fs = this.sweepFlag;

		var xp = Math.cos(psi) * (x1 - x2) / 2.0 + Math.sin(psi) * (y1 - y2) / 2.0;
		var yp = -1 * Math.sin(psi) * (x1 - x2) / 2.0 + Math.cos(psi) * (y1 - y2) / 2.0;
		
		var lambda = (xp * xp) / (rx * rx) + (yp * yp) / (ry * ry);
		if (lambda > 1) {
			rx *= Math.sqrt(lambda);
			ry *= Math.sqrt(lambda);
		}

		var f = Math.sqrt((((rx * rx) * (ry * ry)) - ((rx * rx) * (yp * yp)) - ((ry * ry) * (xp * xp))) 
				/ ((rx * rx) * (yp * yp) + (ry * ry) * (xp * xp)));
		if (fa == fs) {
			f *= -1;
		}
		if (isNaN(f)) {
			f = 0;
		}

		var cxp = f * rx * yp / ry;
		var cyp = f * -ry * xp / rx;
		var cx = (x1 + x2) / 2.0 + Math.cos(psi) * cxp - Math.sin(psi) * cyp;
		var cy = (y1 + y2) / 2.0 + Math.sin(psi) * cxp + Math.cos(psi) * cyp;

		var theta = angle({x:1, y:0}, {x:(xp - cxp) / rx, y:(yp - cyp) / ry});
		var u = {x:(xp - cxp) / rx, y:(yp - cyp) / ry};
		var v = {x:(-1 * xp - cxp) / rx, y:(-1 * yp - cyp) / ry};
		
		var dTheta = angle(u, v);
		if (ratio(u, v) <= -1) {
			dTheta = Math.PI;
		}
		if (ratio(u, v) >= 1) {
			dTheta = 0;
		}
		if(fs == 0 && dTheta > 0) {
			dTheta = dTheta - 2 * Math.PI;
		}
		if(fs == 1 && dTheta < 0) {
			dTheta = dTheta + 2 * Math.PI;
		}

		/* Arc{Point center, double radiusX, double radiusY, double xAxisRotation, 
		 * 		double startAngle, double endAngle, boolean counterclockwise}
		 */
		return {center: {x:cx, y:cy}, radiusX: rx, radiusY: ry, 
			xAxisRotation: psi, startAngle: theta, endAngle: theta + dTheta, 
			counterclockwise: fs === 0.0};
	}

});

