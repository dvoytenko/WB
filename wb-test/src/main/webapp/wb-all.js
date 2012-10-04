
var WB = {};
(function() {
    var initializing = false;
    // The base Class implementation (does nothing)
    WB.Class = function() {
    };
    // Create a new Class that inherits from this class
    WB.Class.extend = function(prop) {
        var _super = this.prototype;

        // Instantiate a base class (but only create the instance,
        // don't run the init constructor)
        initializing = true;
        var prototype = new this();
        initializing = false;

        // Copy the properties over onto the new prototype
        for(var name in prop) {
            // Check if we're overwriting an existing function
            prototype[name] = typeof prop[name] == "function" && typeof _super[name] == "function" ? (function(name, fn) {
                return function() {
                    var tmp = this._super;

                    // Add a new ._super() method that is the same method
                    // but on the super-class
                    this._super = _super[name];

                    // The method only need to be bound temporarily, so we
                    // remove it when we're done executing
                    var ret = fn.apply(this, arguments);
                    this._super = tmp;

                    return ret;
                };
            })(name, prop[name]) : prop[name];
        }

        // The dummy class constructor
        function Class() {
            // All construction is actually done in the init method
            if(!initializing && this.init)
                this.init.apply(this, arguments);
        }
        // Populate our constructed prototype object
        Class.prototype = prototype;

        // Enforce the constructor to be what we expect
        Class.prototype.constructor = Class;

        // And make this class extendable
        Class.extend = arguments.callee;

        return Class;
    };
})();

WB.Segment = WB.Class.extend({
    init: function(config) {
        this.defaultNodeAttrs = {
	    /*
            visible: true,
            listening: true,
            name: undefined,
            alpha: 1,
            x: 0,
            y: 0,
            scale: {
                x: 1,
                y: 1
            },
            rotation: 0,
            offset: {
                x: 0,
                y: 0
            },
	    */
        };

        this.setDefaultAttrs(this.defaultNodeAttrs);
        this.setAttrs(config);
    },

    /**
     */
    getAttrs: function() {
        return this.attrs;
    },

    /**
     */
    setDefaultAttrs: function(config) {
        // create attrs object if undefined
        if(this.attrs === undefined) {
            this.attrs = {};
        }

        if(config) {
            for(var key in config) {
                /*
                 * only set the attr if it's undefined in case
                 * a developer writes a custom class that extends
                 * a WB Class such that their default property
                 * isn't overwritten by the WB Class default
                 * property
                 */
                if(this.attrs[key] === undefined) {
                    this.attrs[key] = config[key];
                }
            }
        }
    },

    /**
     */
    setAttrs: function(config) {
        if(config !== undefined) {
            for(var key in config) {
                    this.attrs[key] = config[key];
            }
	}
    },

    render: function(canvas, context) {
    },

    getLastPoint: function() {
    	return null;
    },
    
    expand: function() {
    	return [this];
    },
    
    animVelocityAdj: function() {
    	return 1.0;
    },

    startAnim: function(prevPoint) {
    	this.prevPoint = prevPoint;
    },

    isAnimDone: function() {
		return true;
    },

    stepAnim: function(canvas, context, distance) {
		this.render(canvas, context);
    },
    
    animPoint: function() {
    	return this.getLastPoint();
    }

});

/**
 * Attrs: {point}
 */
WB.MoveSegment = WB.Segment.extend({

    getLastPoint: function() {
		return this.attrs.point;
    },

    render: function(canvas, context) {
		context.moveTo(this.attrs.point.x, this.attrs.point.y);
    },
    
    animVelocityAdj: function() {
    	return 2.0;
    },
    
    startAnim: function(prevPoint) {
    	this.prevPoint = prevPoint;
    	this.animDone = !(prevPoint);
    },
    
    isAnimDone: function() {
		return this.animDone;
    },

    stepAnim: function(canvas, context, distance) {
    	var dx = this.attrs.point.x - this.prevPoint.x;
    	var dy = this.attrs.point.y - this.prevPoint.y;
        var a = dy/dx;
        
        var actualDistance = Math.sqrt(dx * dx + dy * dy);
        if (distance > actualDistance) {
        	distance = actualDistance;
        }
        
		var x2 = dx * distance/actualDistance;
		var y2 = dx != 0 ? a * x2 : dy * distance/actualDistance;

	    context.moveTo(this.prevPoint.x + x2, this.prevPoint.y + y2);
	    
	    this.lastAnimPoint = {x: this.prevPoint.x + x2, y: this.prevPoint.y + y2};
	    
	    this.animDone = Math.abs(distance - actualDistance) < 1e-3;
	    //console.log(this.animDone);
    },
    
    animPoint: function() {
    	return this.lastAnimPoint;
    }

});

/**
 * Attrs: {point}
 */
WB.LineSegment = WB.Segment.extend({
	
	lastAnimPoint: null,

    getLastPoint: function() {
		return this.attrs.point;
    },

    render: function(canvas, context) {
		context.lineTo(this.attrs.point.x, this.attrs.point.y);
    },

    startAnim: function(prevPoint) {
    	this.prevPoint = prevPoint;
    	this.animDone = false;
    },
    
    isAnimDone: function() {
		return this.animDone;
    },

    stepAnim: function(canvas, context, distance) {
    	var dx = this.attrs.point.x - this.prevPoint.x;
    	var dy = this.attrs.point.y - this.prevPoint.y;
        var a = dy/dx;
        
        var actualDistance = Math.sqrt(dx * dx + dy * dy);
        if (distance > actualDistance) {
        	distance = actualDistance;
        }
        
		var x2 = dx * distance/actualDistance;
		var y2 = dx != 0 ? a * x2 : dy * distance/actualDistance;

	    context.lineTo(this.prevPoint.x + x2, this.prevPoint.y + y2);
	    
	    this.lastAnimPoint = {x: this.prevPoint.x + x2, y: this.prevPoint.y + y2};
	    
	    this.animDone = Math.abs(distance - actualDistance) < 1e-3;
	    //console.log(this.animDone);
    },
    
    animPoint: function() {
    	return this.lastAnimPoint;
    }
    
});

/**
 * Attrs: {cp1, cp2, point}
 */
WB.CubicLineSegment = WB.Segment.extend({

    getLastPoint: function() {
		return this.attrs.point;
    },

    render: function(canvas, context) {
		context.bezierCurveTo(this.attrs.cp1.x,this.attrs.cp1.y, 
			this.attrs.cp2.x,this.attrs.cp2.y, 
			this.attrs.point.x,this.attrs.point.y);
    }

});


/**
 * Attrs: {!start!, cp1, cp2, point}
 */
WB.CubicLinePatchesSegment = WB.Segment.extend({
	
	curveRecursionLimit: 32,
	curveCollinearityEpsilon: 1e-30,
	curveAngleToleranceEpsilon: 0.01,
	approximationScale: 1.0,
	angleTolerance: 0.0,
	cuspLimit: 0.0,

    getLastPoint: function() {
		return this.attrs.point;
    },
    
    calcSqDistance: function(x1, y1, x2, y2) {
		var dx = x2 - x1;
		var dy = y2 - y1;
		return dx * dx + dy * dy;
    },
    
    recursiveBezier: function(x1, y1, x2, y2, x3, y3, x4, y4, level) {
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
				d2 = this.calcSqDistance(x1, y1, x2, y2);
				d3 = this.calcSqDistance(x4, y4, x3, y3);
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
					d2 = this.calcSqDistance(x2, y2, x1, y1);
				else if (d2 >= 1)
					d2 = this.calcSqDistance(x2, y2, x4, y4);
				else
					d2 = this.calcSqDistance(x2, y2, x1 + d2 * dx, y1 + d2 * dy);

				if (d3 <= 0)
					d3 = this.calcSqDistance(x3, y3, x1, y1);
				else if (d3 >= 1)
					d3 = this.calcSqDistance(x3, y3, x4, y4);
				else
					d3 = this.calcSqDistance(x3, y3, x1 + d3 * dx, y1 + d3 * dy);
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
		this.recursiveBezier(x1, y1, x12, y12, x123, y123, x1234, y1234, level + 1);
		this.recursiveBezier(x1234, y1234, x234, y234, x34, y34, x4, y4, level + 1);
    },

    bezier: function(x1, y1, x2, y2, x3, y3, x4, y4) {
    	
		this.distanceToleranceSquare = 0.5 / this.approximationScale;
		this.distanceToleranceSquare *= this.distanceToleranceSquare;
    	this.points = [];
    	
		this.points.push({x: x1, y: y1});
		this.recursiveBezier(x1, y1, x2, y2, x3, y3, x4, y4, 0);
		this.points.push({x: x4, y: y4});
	},
    
    render: function(canvas, context) {
		this.bezier(this.attrs.start.x, this.attrs.start.y, 
				this.attrs.cp1.x, this.attrs.cp1.y, 
				this.attrs.cp2.x, this.attrs.cp2.y, 
				this.attrs.point.x, this.attrs.point.y);
		for (var i = 1; i < this.points.length; i++) {
			context.lineTo(this.points[i].x, this.points[i].y);
		}
    },

    expand: function() {
    	var all = [];
		this.bezier(this.attrs.start.x, this.attrs.start.y, 
				this.attrs.cp1.x, this.attrs.cp1.y, 
				this.attrs.cp2.x, this.attrs.cp2.y, 
				this.attrs.point.x, this.attrs.point.y);
		for (var i = 1; i < this.points.length; i++) {
			all.push(new WB.LineSegment({point: this.points[i]}));
		}
    	return all;
    },
    
    isAnimDone: function() {
		return true;
    },

    stepAnim: function(canvas, context, distance) {
    }

});


/**
 * Attrs: {!start!, cp, point}
 */
WB.QuadLinePatchesSegment = WB.Segment.extend({
	
	curveRecursionLimit: 32,
	curveCollinearityEpsilon: 1e-30,
	curveAngleToleranceEpsilon: 0.01,
	approximationScale: 1.0,
	angleTolerance: 0.0,
	
    getLastPoint: function() {
		return this.attrs.point;
    },
    
    calcSqDistance: function(x1, y1, x2, y2) {
		var dx = x2 - x1;
		var dy = y2 - y1;
		return dx * dx + dy * dy;
    },
    
    recursiveBezier: function(x1, y1, x2, y2, x3, y3, level) {
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
				d = this.calcSqDistance(x1, y1, x2, y2);
			} else {
				d = ((x2 - x1) * dx + (y2 - y1) * dy) / da;
				if (d > 0 && d < 1) {
					// Simple collinear case, 1---2---3
					// We can leave just two endpoints
					return;
				}
				if (d <= 0) {
					d = this.calcSqDistance(x2, y2, x1, y1);
				} else if (d >= 1) {
					d = this.calcSqDistance(x2, y2, x3, y3);
				} else {
					d = this.calcSqDistance(x2, y2, x1 + d * dx, y1 + d * dy);
				}
            }
			if (d < this.distanceToleranceSquare) {
				this.points.push({x: x2, y: y2});
				return;
			}
        }

        // Continue subdivision
        //----------------------
        this.recursiveBezier(x1, y1, x12, y12, x123, y123, level + 1); 
        this.recursiveBezier(x123, y123, x23, y23, x3, y3, level + 1); 
    },

    bezier: function(x1, y1, x2, y2, x3, y3) {
    	
		this.distanceToleranceSquare = 0.5 / this.approximationScale;
		this.distanceToleranceSquare *= this.distanceToleranceSquare;
    	this.points = [];
    	
		this.points.push({x: x1, y: y1});
		this.recursiveBezier(x1, y1, x2, y2, x3, y3, 0);
		this.points.push({x: x3, y: y3});
	},
    
    render: function(canvas, context) {
		this.bezier(this.attrs.start.x, this.attrs.start.y, 
				this.attrs.cp.x, this.attrs.cp.y, 
				this.attrs.point.x, this.attrs.point.y);
		for (var i = 1; i < this.points.length; i++) {
			context.lineTo(this.points[i].x, this.points[i].y);
		}
    },

    expand: function() {
    	var all = [];
		this.bezier(this.attrs.start.x, this.attrs.start.y, 
				this.attrs.cp.x, this.attrs.cp.y, 
				this.attrs.point.x, this.attrs.point.y);
		for (var i = 1; i < this.points.length; i++) {
			all.push(new WB.LineSegment({point: this.points[i]}));
		}
    	return all;
    },
    
    isAnimDone: function() {
		return true;
    },

    stepAnim: function(canvas, context, distance) {
    }

});
