

WB.CurveSegment = WB.Segment.extend('CurveSegment', {

	expand: function(pane) {
		return [];
	},
	
	pathSegment: function(pane) {
		// TODO: find a way to optimize. Resolve segments against the context? shape+pane+place
		// TODO: is this a good idea? can't reuse shapes with different start points
		if (!this._pathSegment) {
			var segments = this.expand(pane);
			this._pathSegment = new WB.PathSegment({segments: segments});
		}
		return this._pathSegment;
//		var segments = this.expand(pane);
//		return new WB.PathSegment({segments: segments});
	},

	outline: function(pane) {
		this.pathSegment(pane).outline(pane);
	},
	
	createAnimation: function() {
		return new WB.CurveSegmentAnimation(this);
	}
	
});


WB.CurveSegmentAnimation = WB.Animation.extend('CurveSegmentAnimation', {
	
	curve: null,
	
	init: function(curve) {
		this.curve = curve;
	},
	
	start: function(board) {
		this.anim = this.curve.pathSegment(board.animationPane).createAnimation();
		this.anim.start(board);
	},
	
	frame: function(time) {
		this.anim.frame(time);
	},
	
	isDone: function() {
		return this.anim.isDone();
	},
	
	end: function() {
		this.anim.end();
	},
	
	getTimeLeft: function() {
		return this.anim.getTimeLeft();
	}
	
});


WB.CubicSegment = WB.CurveSegment.extend('CubicSegment', {
	
	cp1: null,
	
	cp2: null,
	
	endPoint: null,
	
	curveRecursionLimit: 32,
	curveCollinearityEpsilon: 1e-30,
	curveAngleToleranceEpsilon: 0.01,
	approximationScale: 1.0,
	angleTolerance: 0.0,
	cuspLimit: 0.0,
	
	init: function(opts) {
		if (opts && opts.cp1) {
			this.cp1 = opts.cp1;
		}
		if (opts && opts.cp2) {
			this.cp2 = opts.cp2;
		}
		if (opts && opts.endPoint) {
			this.endPoint = opts.endPoint;
		}
	},
	
	expand: function(pane) {
    	var all = [];
    	var startPoint = pane.getCurrentPoint();
		this._bezier(startPoint.x, startPoint.y, 
				this.cp1.x, this.cp1.y, this.cp2.x, this.cp2.y, 
				this.endPoint.x, this.endPoint.y);
		for (var i = 1; i < this.points.length; i++) {
			all.push(new WB.LineToSegment({point: this.points[i]}));
		}
    	return all;
	},
	
    _bezier: function(x1, y1, x2, y2, x3, y3, x4, y4) {
    	
		this.distanceToleranceSquare = 0.5 / this.approximationScale;
		this.distanceToleranceSquare *= this.distanceToleranceSquare;
    	this.points = [];
    	
		this.points.push({x: x1, y: y1});
		this._recursiveBezier(x1, y1, x2, y2, x3, y3, x4, y4, 0);
		this.points.push({x: x4, y: y4});
	},
	
    _calcSqDistance: function(x1, y1, x2, y2) {
		var dx = x2 - x1;
		var dy = y2 - y1;
		return dx * dx + dy * dy;
    },
    
    _recursiveBezier: function(x1, y1, x2, y2, x3, y3, x4, y4, level) {
		if (level > this.curveRecursionLimit) {
			return;
		}

		// Calculate all the mid-points of the line segments
		// ----------------------
		var x12 = (x1 + x2) / 2;
		var y12 = (y1 + y2) / 2;
		var x23 = (x2 + x3) / 2;
		var y23 = (y2 + y3) / 2;
		var x34 = (x3 + x4) / 2;
		var y34 = (y3 + y4) / 2;
		var x123 = (x12 + x23) / 2;
		var y123 = (y12 + y23) / 2;
		var x234 = (x23 + x34) / 2;
		var y234 = (y23 + y34) / 2;
		var x1234 = (x123 + x234) / 2;
		var y1234 = (y123 + y234) / 2;

		// Try to approximate the full cubic curve by a single straight line
		// ------------------
		var dx = x4 - x1;
		var dy = y4 - y1;

		var d2 = Math.abs(((x2 - x4) * dy - (y2 - y4) * dx));
		var d3 = Math.abs(((x3 - x4) * dy - (y3 - y4) * dx));
		var da1, da2, k;

		// DANGER
		// switch((int(d2 > curveCollinearityEpsilon ? 1 : 0) << 1) +
		// int(d3 > curveCollinearityEpsilon))
		switch (((d2 > this.curveCollinearityEpsilon ? 1 : 0) << 1)
				+ (d3 > this.curveCollinearityEpsilon ? 1 : 0)) {
		case 0:
			// All collinear OR p1==p4
			// ----------------------
			k = dx * dx + dy * dy;
			if (k == 0) {
				d2 = this._calcSqDistance(x1, y1, x2, y2);
				d3 = this._calcSqDistance(x4, y4, x3, y3);
			} else {
				k = 1 / k;
				da1 = x2 - x1;
				da2 = y2 - y1;
				d2 = k * (da1 * dx + da2 * dy);
				da1 = x3 - x1;
				da2 = y3 - y1;
				d3 = k * (da1 * dx + da2 * dy);
				if (d2 > 0 && d2 < 1 && d3 > 0 && d3 < 1) {
					// Simple collinear case, 1---2---3---4
					// We can leave just two endpoints
					return;
				}
				if (d2 <= 0)
					d2 = this._calcSqDistance(x2, y2, x1, y1);
				else if (d2 >= 1)
					d2 = this._calcSqDistance(x2, y2, x4, y4);
				else
					d2 = this._calcSqDistance(x2, y2, x1 + d2 * dx, y1 + d2 * dy);

				if (d3 <= 0)
					d3 = this._calcSqDistance(x3, y3, x1, y1);
				else if (d3 >= 1)
					d3 = this._calcSqDistance(x3, y3, x4, y4);
				else
					d3 = this._calcSqDistance(x3, y3, x1 + d3 * dx, y1 + d3 * dy);
			}
			if (d2 > d3) {
				if (d2 < this.distanceToleranceSquare) {
					this.points.push({x: x2, y: y2});
					return;
				}
			} else {
				if (d3 < this.distanceToleranceSquare) {
					this.points.push({x: x3, y: y3});
					return;
				}
			}
			break;

		case 1:
			// p1,p2,p4 are collinear, p3 is significant
			// ----------------------
			if (d3 * d3 <= this.distanceToleranceSquare * (dx * dx + dy * dy)) {
				if (this.angleTolerance < this.curveAngleToleranceEpsilon) {
					this.points.push({x: x23, y: y23});
					return;
				}

				// Angle Condition
				// ----------------------
				da1 = Math.abs(Math.atan2(y4 - y3, x4 - x3)
						- Math.atan2(y3 - y2, x3 - x2));
				if (da1 >= Math.PI)
					da1 = 2 * Math.PI - da1;

				if (da1 < this.angleTolerance) {
					this.points.push({x: x2, y: y2});
					this.points.push({x: x3, y: y3});
					return;
				}

				if (this.cuspLimit != 0.0) {
					if (da1 > this.cuspLimit) {
						this.points.push({x: x3, y: y3});
						return;
					}
				}
			}
			break;

		case 2:
			// p1,p3,p4 are collinear, p2 is significant
			// ----------------------
			if (d2 * d2 <= this.distanceToleranceSquare * (dx * dx + dy * dy)) {
				if (this.angleTolerance < this.curveAngleToleranceEpsilon) {
					this.points.push({x: x23, y: y23});
					return;
				}

				// Angle Condition
				// ----------------------
				da1 = Math.abs(Math.atan2(y3 - y2, x3 - x2)
						- Math.atan2(y2 - y1, x2 - x1));
				if (da1 >= Math.PI)
					da1 = 2 * Math.PI - da1;

				if (da1 < this.angleTolerance) {
					this.points.push({x: x2, y: y2});
					this.points.push({x: x3, y: y3});
					return;
				}

				if (this.cuspLimit != 0.0) {
					if (da1 > this.cuspLimit) {
						this.points.push({x: x2, y: y2});
						return;
					}
				}
			}
			break;

		case 3:
			// Regular case
			// -----------------
			if ((d2 + d3) * (d2 + d3) <= this.distanceToleranceSquare
					* (dx * dx + dy * dy)) {
				// If the curvature doesn't exceed the distance_tolerance value
				// we tend to finish subdivisions.
				// ----------------------
				if (this.angleTolerance < this.curveAngleToleranceEpsilon) {
					this.points.push({x: x23, y: y23});
					return;
				}

				// Angle & Cusp Condition
				// ----------------------
				k = Math.atan2(y3 - y2, x3 - x2);
				da1 = Math.abs(k - Math.atan2(y2 - y1, x2 - x1));
				da2 = Math.abs(Math.atan2(y4 - y3, x4 - x3) - k);
				if (da1 >= Math.PI)
					da1 = 2 * Math.PI - da1;
				if (da2 >= Math.PI)
					da2 = 2 * Math.PI - da2;

				if (da1 + da2 < this.angleTolerance) {
					// Finally we can stop the recursion
					// ----------------------
					this.points.push({x: x23, y: y23});
					return;
				}

				if (this.cuspLimit != 0.0) {
					if (da1 > this.cuspLimit) {
						this.points.push({x: x2, y: y2});
						return;
					}

					if (da2 > this.cuspLimit) {
						this.points.push({x: x3, y: y3});
						return;
					}
				}
			}
			break;
		}

		// Continue subdivision
		// ----------------------
		this._recursiveBezier(x1, y1, x12, y12, x123, y123, x1234, y1234, level + 1);
		this._recursiveBezier(x1234, y1234, x234, y234, x34, y34, x4, y4, level + 1);
    }
	
});


WB.QuadSegment = WB.CurveSegment.extend('QuadSegment', {

	cp: null,
	
	endPoint: null,
	
	curveRecursionLimit: 32,
	curveCollinearityEpsilon: 1e-30,
	curveAngleToleranceEpsilon: 0.01,
	approximationScale: 1.0,
	angleTolerance: 0.0,
	
	init: function(opts) {
		if (opts && opts.cp) {
			this.cp = opts.cp;
		}
		if (opts && opts.endPoint) {
			this.endPoint = opts.endPoint;
		}
	},

	expand: function(pane) {
    	var all = [];
    	var startPoint = pane.getCurrentPoint();
		this._bezier(startPoint.x, startPoint.y, 
				this.cp.x, this.cp.y, 
				this.endPoint.x, this.endPoint.y);
		for (var i = 1; i < this.points.length; i++) {
			all.push(new WB.LineToSegment({point: this.points[i]}));
		}
    	return all;
	},
	
    _bezier: function(x1, y1, x2, y2, x3, y3) {
    	
		this.distanceToleranceSquare = 0.5 / this.approximationScale;
		this.distanceToleranceSquare *= this.distanceToleranceSquare;
    	this.points = [];
    	
		this.points.push({x: x1, y: y1});
		this._recursiveBezier(x1, y1, x2, y2, x3, y3, 0);
		this.points.push({x: x3, y: y3});
	},
    
    _calcSqDistance: function(x1, y1, x2, y2) {
		var dx = x2 - x1;
		var dy = y2 - y1;
		return dx * dx + dy * dy;
    },
	
    _recursiveBezier: function(x1, y1, x2, y2, x3, y3, level) {
		if (level > this.curveRecursionLimit) {
			return;
		}

        // Calculate all the mid-points of the line segments
        //----------------------
		var x12 = (x1 + x2) / 2;
		var y12 = (y1 + y2) / 2;
		var x23 = (x2 + x3) / 2;
		var y23 = (y2 + y3) / 2;
		var x123 = (x12 + x23) / 2;
		var y123 = (y12 + y23) / 2;

		var dx = x3 - x1;
		var dy = y3 - y1;
		var d = Math.abs(((x2 - x3) * dy - (y2 - y3) * dx));
		var da;

		if (d > this.curveCollinearityEpsilon) {
            // Regular case
            //-----------------
			if (d * d <= this.distanceToleranceSquare * (dx * dx + dy * dy)) {
                // If the curvature doesn't exceed the distance_tolerance value
                // we tend to finish subdivisions.
                //----------------------
				if (this.angleTolerance < this.curveAngleToleranceEpsilon) {
					this.points.push({x: x123, y: y123});
					return;
				}

                // Angle & Cusp Condition
                //----------------------
				da = Math.abs(Math.atan2(y3 - y2, x3 - x2)
						- Math.atan2(y2 - y1, x2 - x1));
				if (da >= Math.PI) {
					da = 2 * Math.PI - da;
				}

				if (da < this.angleTolerance) {
                    // Finally we can stop the recursion
                    //----------------------
                    this.points.push({x: x123, y: y123});
                    return;                 
                }
            }
		} else {
            // Collinear case
            //------------------
			da = dx * dx + dy * dy;
			if (da == 0) {
				d = this._calcSqDistance(x1, y1, x2, y2);
			} else {
				d = ((x2 - x1) * dx + (y2 - y1) * dy) / da;
				if (d > 0 && d < 1) {
					// Simple collinear case, 1---2---3
					// We can leave just two endpoints
					return;
				}
				if (d <= 0) {
					d = this._calcSqDistance(x2, y2, x1, y1);
				} else if (d >= 1) {
					d = this._calcSqDistance(x2, y2, x3, y3);
				} else {
					d = this._calcSqDistance(x2, y2, x1 + d * dx, y1 + d * dy);
				}
            }
			if (d < this.distanceToleranceSquare) {
				this.points.push({x: x2, y: y2});
				return;
			}
        }

        // Continue subdivision
        //----------------------
        this._recursiveBezier(x1, y1, x12, y12, x123, y123, level + 1); 
        this._recursiveBezier(x123, y123, x23, y23, x3, y3, level + 1); 
    }

});

