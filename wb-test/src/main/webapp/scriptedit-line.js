
var lineEndClassMap = {};


/**
 */
var DrawLineEpisodeView = BaseEpisodeView.extend({
	
	className: "Episode DrawLineEpisode",
	
	modelIconPath: function() {
		return 'images/arrow_right.png';
	},
	
	modelPosSize: function() {
		var point1 = this.model.get('point1');
		var point2 = this.model.get('point2');
		var x1 = point1 ? point1.x : null;
		var y1 = point1 ? point1.y : null;
		var x2 = point2 ? point2.x : null;
		var y2 = point2 ? point2.y : null;
		return 'p1: ' + x1 + ', ' + y1
			+ '; p2: ' + x2 + ', ' + y2;
	}
	
});
viewClassMap['DrawLineEpisode'] = DrawLineEpisodeView;


/**
 */
var LineEndShapeProto = Kinetic.Shape.extend({
	
    adjustEndPoint: function(toPoint, fromPoint) {
    },
    
    updateLine: function(toPoint, fromPoint) {
    }
    
});


/**
 */
var BoardLineEditable = Kinetic.Group.extend({

	init: function(config) {
        this.setDefaultAttrs({
        });
        this.shapeType = 'BoardLineEditable';
        this._super(config);
        
        this.episode = this.attrs.episode;

		// pick initial coords
        var point1 = this.episode.get('point1');
        var point2 = this.episode.get('point2');
        if (!point1) {
        	point1 = {
        		x: Math.max(Math.round(Math.random() * this.attrs.spaceWidth), 10),
        		y: Math.max(Math.round(Math.random() * this.attrs.spaceHeight), 10)
        	};
        	this.episode.set('point1', point1);
        }
        if (!point2) {
        	point2 = {
        		x: Math.max(Math.round(Math.random() * this.attrs.spaceWidth), 10),
        		y: Math.max(Math.round(Math.random() * this.attrs.spaceHeight), 10)
        	};
        	this.episode.set('point2', point2);
        }
//        console.log('initial coord: ' + JSON.stringify(point1) + ' to ' + 
//        		JSON.stringify(point2));
        
        var point1Type = this.episode.get('point1Type');
        var point2Type = this.episode.get('point2Type');
        if (!point1Type) {
        	point1Type = 'simple';
        }
        if (!point2Type) {
        	point2Type = 'simple';
        }

        var point1Shape;
		if (lineEndClassMap[point1Type]) {
			point1Shape = new lineEndClassMap[point1Type]({
				toPoint: point1,
				fromPoint: point2,
				stroke: "black",
				strokeWidth: 2,
				name: 'point1'
			});
		}

		var point2Shape;
		if (lineEndClassMap[point2Type]) {
			point2Shape = new lineEndClassMap[point2Type]({
				toPoint: point2,
				fromPoint: point1,
				stroke: "black",
				strokeWidth: 2,
				name: 'point2'
			});
		}

		var point1Adj = point1;
		var point2Adj = point2;
		if (point1Shape) {
			point1Adj = point1Shape.adjustEndPoint(point1, point2);
		}
		if (point2Shape) {
			point2Adj = point2Shape.adjustEndPoint(point2, point1);
		}
        
		var line = new Kinetic.Line({
			points: [point1Adj.x, point1Adj.y, point2Adj.x, point2Adj.y],
			stroke: "black",
			strokeWidth: 2,
			name: "line"
		});
		this.add(line);
		
		if (point1Shape) {
			this.add(point1Shape);
		}
		if (point2Shape) {
			this.add(point2Shape);
		}
		
		// mouseover
		this.on("mousemove", function(e) {
			document.body.style.cursor = 'move';
        });
		this.on("mouseout", function() {
			document.body.style.cursor = 'default';
        });       

		this._addAnchor(point1.x, point1.y, "point1Anchor");
		this._addAnchor(point2.x, point2.y, "point2Anchor");
		
//		this.on("dragstart", function() {
//			this.moveToTop();
//		});
	},
	
	ready: function() {
	},
	
	setSelected: function(selected) {
//		console.log('selected = ' + selected);
		var children = this.getChildren();
		for (var i = 0; i < children.length; i++) {
			var e = children[i];
			if (e.attrs.name != 'line' 
					&& e.attrs.name != 'point1'
					&& e.attrs.name != 'point2') {
				if (selected) {
					e.show();
				} else {
					e.hide();
				}
			}
		}
		this.setDraggable(selected);
	},

	_update: function(activeAnchor) {
		var point1Anchor = this.get(".point1Anchor")[0];
		var point2Anchor = this.get(".point2Anchor")[0];
		var point1Shape = this.get(".point1")[0];
		var point2Shape = this.get(".point2")[0];
		var line = this.get(".line")[0];
		
		var point1 = {x: point1Anchor.attrs.x, y: point1Anchor.attrs.y};
		var point2 = {x: point2Anchor.attrs.x, y: point2Anchor.attrs.y};
		var point1Adj = point1Shape ? point1Shape.adjustEndPoint(point1, point2) : point1;
		var point2Adj = point2Shape ? point2Shape.adjustEndPoint(point2, point1) : point2;
		line.setPoints(point1Adj.x, point1Adj.y, point2Adj.x, point2Adj.y);
		if (point1Shape) {
			point1Shape.updateLine(point1, point2);
		}
		if (point2Shape) {
			point2Shape.updateLine(point2, point1);
		}

    	this.episode.set('point1', point1);
    	this.episode.set('point2', point2);
	},

	_addAnchor: function(x, y, name) {
		var that = this;
		
		var anchor = new Kinetic.Circle({
			x: x,
			y: y,
			stroke: "#666",
			fill: "#ddd",
			strokeWidth: 2,
			radius: 8,
			name: name,
			draggable: true
		});
		
		anchor.on("dragmove", function() {
			that._update(this, this);
			that.getLayer().draw();
		});
		anchor.on("mousedown touchstart", function() {
			that.setDraggable(false);
			// this.moveToTop();
		});
		anchor.on("dragend", function() {
			that.setDraggable(true);
			that.getLayer().draw();
		});
		
		// add hover styling
		anchor.on("mouseover", function(e) {
			e.cancelBubble = true;
			document.body.style.cursor = 'pointer';
			this.setStrokeWidth(4);
			this.getLayer().draw();
		});
		anchor.on("mousemove", function(e) {
			e.cancelBubble = true;
			document.body.style.cursor = 'pointer';
		});
		anchor.on("mouseout", function(e, a, b, c) {
			var inter = that.getIntersections({x: e.x, y: e.y});
			if (inter && inter.length) {
				document.body.style.cursor = 'move';
			} else {
				document.body.style.cursor = 'default';
			}
			this.setStrokeWidth(2);
			this.getLayer().draw();
		});
		
		this.add(anchor);
	}
	
});
boardClassMap['DrawLineEpisode'] = BoardLineEditable;


/**
 */
var LineEndCaretShape = Kinetic.Line.extend({
	
    init: function(config) {
        this.setDefaultAttrs({
        });
        this.shapeType = 'LineEndCaret';
        this._super(config);

        // xxx
        var toPoint = this.attrs.toPoint;
        var fromPoint = this.attrs.fromPoint;
        if (toPoint && fromPoint) {
        	this.updateLine(toPoint, fromPoint);
        }
    },
    
    adjustEndPoint: function(toPoint, fromPoint) {
    	return toPoint;
    },
    
    updateLine: function(toPoint, fromPoint) {
    	
//    	console.log('point: ' + JSON.stringify(toPoint) + ' -> ' + 
//    			JSON.stringify(fromPoint));
    	
        // TODO customize
        var radius = 20; 
        var angle = 30;
        
        var a = WB.Geom.angle(fromPoint, toPoint);
        var da = WB.Geom.rad(angle);
        var startAngle = a + da;
        var endAngle = a - da;
        
        var x1 = toPoint.x + Math.cos(startAngle) * radius;
        var y1 = toPoint.y + Math.sin(startAngle) * radius;
        var x2 = toPoint.x + Math.cos(endAngle) * radius;
        var y2 = toPoint.y + Math.sin(endAngle) * radius;
        
        this.setPoints(
        		x1, y1,
        		toPoint.x, toPoint.y,
        		x2, y2);
    }

});
lineEndClassMap['caret'] = LineEndCaretShape;


/**
 */
var LineEndTriangleShape = Kinetic.Line.extend({
	
    init: function(config) {
        this.setDefaultAttrs({
        });
        this.shapeType = 'LineEndTriangle';
        this._super(config);

        // xxx
        var toPoint = this.attrs.toPoint;
        var fromPoint = this.attrs.fromPoint;
        if (toPoint && fromPoint) {
        	this.updateLine(toPoint, fromPoint);
        }
    },
    
    adjustEndPoint: function(toPoint, fromPoint) {
    	
        // TODO customize
        var radius = 20; 
        var angle = 30;
        
        var a = WB.Geom.angle(fromPoint, toPoint);
        var da = WB.Geom.rad(angle);
        var startAngle = a + da;
        var endAngle = a - da;

        var x1 = toPoint.x + Math.cos(startAngle) * radius;
        var y1 = toPoint.y + Math.sin(startAngle) * radius;
        var x2 = toPoint.x + Math.cos(endAngle) * radius;
        var y2 = toPoint.y + Math.sin(endAngle) * radius;

    	return {x: (x1+x2)/2, y: (y1+y2)/2};
    },
    
    updateLine: function(toPoint, fromPoint) {
    	
//    	console.log('point: ' + JSON.stringify(toPoint) + ' -> ' + 
//    			JSON.stringify(fromPoint));
    	
        // TODO customize
        var radius = 20; 
        var angle = 30;
        
        var a = WB.Geom.angle(fromPoint, toPoint);
        var da = WB.Geom.rad(angle);
        var startAngle = a + da;
        var endAngle = a - da;
        
        var x1 = toPoint.x + Math.cos(startAngle) * radius;
        var y1 = toPoint.y + Math.sin(startAngle) * radius;
        var x2 = toPoint.x + Math.cos(endAngle) * radius;
        var y2 = toPoint.y + Math.sin(endAngle) * radius;
        
        this.setPoints(
        		x1, y1,
        		toPoint.x, toPoint.y,
        		x2, y2,
        		x1, y1);
    }

});
lineEndClassMap['triangle'] = LineEndTriangleShape;

