

/**
 */
var DrawShapeEpisodeView = BaseEpisodeView.extend({
	
	className: "Episode DrawShapeEpisode",
	
	modelIconPath: function() {
		return this.model.get('thumbUrl');
	},
	
	modelPosSize: function() {
		var pos = this.model.get('position');
		var x = pos ? pos.x : null;
		var y = pos ? pos.y : null;
		var width = this.model.get('width');
		var height = this.model.get('height');
		return 'x: ' + Math.round(x) + ', y: ' + Math.round(y)
			+ ', w: ' + Math.round(width) + ', h: ' + Math.round(height);
	},
	
});
viewClassMap['DrawShapeEpisode'] = DrawShapeEpisodeView;


/**
 */
var BoardShapeEditable = Kinetic.Group.extend({

	init: function(config) {
        this.setDefaultAttrs({
        });
        this.shapeType = 'BoardShapeEditable';
        this._super(config);
        
        this.episode = this.attrs.episode;
        var shape = this.episode.get('shape');

		// pick initial size
		var width = this.episode.get('width');
		var height = this.episode.get('height');
		if (!width || !height) {
			var estimatedSize = this.estimateSize(width, height);
			width = estimatedSize.width;
			height = estimatedSize.height;
			// model will be assigned in _assignShape
			// this.episode.set('width', width);
			// this.episode.set('height', height);
		}
		
		this.insets = this.attrs.insets;
		if (!this.insets) {
			this.insets = {top: 0, bottom: 0, left: 0, right: 0};
		}

        // pick initial position
        var x, y;
        var position = this.episode.get('position');
        if (position) {
        	x = position.x;
        	y = position.y;
        } else {
            x = Math.max(Math.round(Math.random() * this.attrs.spaceWidth) - width, 10);
            y = Math.max(Math.round(Math.random() * this.attrs.spaceHeight) - height, 10);
            this.episode.set('position', {x: x, y: y});
        }
        this.setPosition(x, y);
        
        var fullWidth = width + this.insets.left + this.insets.right;
        var fullHeight = height + this.insets.top + this.insets.bottom;
        
        var frame = new Kinetic.Rect({
			x: 0,
			y: 0,
			width: fullWidth,
			height: fullHeight,
			stroke: "black",
			strokeWidth: 2,
			name: "frame"
        });
		this.add(frame);

		var image = new BoardShape({
			x: this.insets.left,
			y: this.insets.top,
			width: width,
			height: height,
			stroke: "black",
			strokeWidth: 2,
			name: "image",
			shape: shape
		});
		this.add(image);

		var that = this;
		
		// mouseover
		this.on("mousemove", function(e) {
			document.body.style.cursor = 'move';
        });
		this.on("mouseout", function() {
			document.body.style.cursor = 'default';
        });       

		this._addAnchor(0, 0, "topLeft");
		this._addAnchor(fullWidth, 0, "topRight");
		this._addAnchor(fullWidth, fullHeight, "bottomRight");
		this._addAnchor(0, fullHeight, "bottomLeft");

		this.on("dragend", function() {
			that._updateModel(true, false);
		});

//		this.on("dragstart", function() {
//			this.moveToTop();
//		});
		
		if (shape) {
			this._assignShape(shape);
		}
	},
	
	estimateSize: function(width, height) {
		var size = width ? width : height;
		if (!size) {
			size = 200;
		}
		return {width: width ? width : size, height: height ? height : size};
	},
	
	ready: function() {
	    if (!this.shape) {
	    	this.loadShape();
	    }
	},
	
	loadShape: function() {
    	var shapeId = this.episode.get('shapeId');
    	if (shapeId) {
	    	this._downloadAndSetShape('shapedb/' + shapeId + '.json');
    	}
	},
	
	setSelected: function(selected) {
//		console.log('selected = ' + selected);
		var children = this.getChildren();
		for (var i = 0; i < children.length; i++) {
			var e = children[i];
			if (e.attrs.name != 'image') {
				if (selected) {
					e.show();
				} else {
					e.hide();
				}
			}
		}
		this.setDraggable(selected);
	},

	_downloadAndSetShape: function(path) {
		var that = this;
		// console.log('downloadAndShowShape: ' + path);
		$.ajax({url: path, contentType: 'application/json'})
		.done(function(json) {
			console.log('got data');
			if (json) {
				if (json.constructor == String) {
					json = JSON.parse(json);
				}
				console.log(json);
				
				var parser = new WB.Parser();
				var shape = parser.parse(json);
				that._setShape(shape);
			}
		});
	},
	
	_setShape: function(shape) {
		this._assignShape(shape);
		this.getLayer().draw();
	},
	
	_assignShape: function(shape) {
        this.shape = shape;
        
		var topLeft = this.get(".topLeft")[0];
		var topRight = this.get(".topRight")[0];
		var bottomRight = this.get(".bottomRight")[0];
		var bottomLeft = this.get(".bottomLeft")[0];
		var image = this.get(".image")[0];
		var frame = this.get(".frame")[0];
        
        var currSize = image.getSize();
        
        image.setShape(shape);

		var width = this.episode.get('width');
		var height = this.episode.get('height');
		if (!width || !height) {
			// recalc size
			var dWidth = currSize.width;
			var dHeight = currSize.height;
			width = dWidth;
			height = dHeight;
			if (!shape.localBounds) {
				shape.localBounds = this.calcLocalBounds(shape);
			}
			if (shape.localBounds) {
				width = Math.abs(shape.localBounds.bottomright.x - shape.localBounds.topleft.x);
				height = Math.abs(shape.localBounds.bottomright.y - shape.localBounds.topleft.y);
				// w/h: 200 vs 295; 200 vs 132
				// console.log('w/h: ' + dWidth + ' vs ' + width + '; ' + dHeight + ' vs ' + height);
				var scaleX = 1.0;
				var scaleY = 1.0;
				if (Math.abs(dWidth - width) > 1e-2) {
					scaleX = dWidth / width;
				}
				if (Math.abs(dHeight - height) > 1e-2) {
					scaleY = dHeight / height;
				}
				var scale = Math.min(scaleX, scaleY);
				// scale: 0.6779661016949152
				// console.log('scale: ' + scale);
				
				width *= scale;
				height *= scale;
				// console.log('w/h: ' + width + ' x ' + height);
			}

			// asign new size to the model
			if (width && height) {
				this.episode.set('width', width);
				this.episode.set('height', height);
			}
		}
        
		if (width && height) {
	        var fullWidth = width + this.insets.left + this.insets.right;
	        var fullHeight = height + this.insets.top + this.insets.bottom;
	        image.setPosition(this.insets.left, this.insets.top);
			image.setSize(width, height);
			frame.setSize(fullWidth, fullHeight);
			topRight.attrs.x = topLeft.attrs.x + fullWidth;
			bottomRight.attrs.x = topLeft.attrs.x + fullWidth;
			bottomRight.attrs.y = topLeft.attrs.y + fullHeight;
			bottomLeft.attrs.y = topLeft.attrs.y + fullHeight;
		}
	},
	
	calcLocalBounds: function(shape) {
		return null;
	},
	
	_update: function(activeAnchor) {
		var topLeft = this.get(".topLeft")[0];
		var topRight = this.get(".topRight")[0];
		var bottomRight = this.get(".bottomRight")[0];
		var bottomLeft = this.get(".bottomLeft")[0];
		var image = this.get(".image")[0];
		var frame = this.get(".frame")[0];
		
		// update anchor positions
		switch (activeAnchor.getName()) {
			case "topLeft":
				topRight.attrs.y = activeAnchor.attrs.y;
				bottomLeft.attrs.x = activeAnchor.attrs.x;
				break;
			case "topRight":
				topLeft.attrs.y = activeAnchor.attrs.y;
				bottomRight.attrs.x = activeAnchor.attrs.x;
				break;
			case "bottomRight":
				bottomLeft.attrs.y = activeAnchor.attrs.y;
				topRight.attrs.x = activeAnchor.attrs.x;
				break;
			case "bottomLeft":
				bottomRight.attrs.y = activeAnchor.attrs.y;
				topLeft.attrs.x = activeAnchor.attrs.x;
				break;
		}

		var x = topLeft.attrs.x + this.insets.left;
		var y = topLeft.attrs.y + this.insets.top;
		image.setPosition(x, y);
		frame.setPosition(topLeft.attrs.x, topLeft.attrs.y);
		
		var fullWidth = topRight.attrs.x - topLeft.attrs.x;
		var fullHeight = bottomLeft.attrs.y - topLeft.attrs.y;
		var width = fullWidth - this.insets.left - this.insets.right;
		var height = fullHeight - this.insets.top - this.insets.bottom;
		if (width && height) {
			image.setSize(width, height);
			frame.setSize(fullWidth, fullHeight);
		}
		
		this._updateModel(true, true);
	},
	
	_updateModel: function(updatePosition, updateSize) {

		if (updatePosition) {
			var pos = this.getPosition();
			var x = pos.x + this.insets.left;
			var y = pos.y + this.insets.top;
			console.log('update position to: ' + x + ', ' + y);
			this.episode.set('position', {x: x, y: y});
		}
		
		if (updateSize) {
			var image = this.get(".image")[0];
			var size = image.getSize();
			console.log('update size to: ' + size.width + ' x ' + size.height);
			this.episode.set('width', size.width);
			this.episode.set('height', size.height);
		}
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
boardClassMap['DrawShapeEpisode'] = BoardShapeEditable;


/**
 */
var BoardShape = Kinetic.Shape.extend({
	
    init: function(config) {
        this.setDefaultAttrs({
            width: 0,
            height: 0,
            cornerRadius: 0
        });
        this.shapeType = 'BoardShape';
        config.drawFunc = this.drawFunc;
        // call super constructor
        this._super(config);
        
        this.shape = config.shape;
    },
    
    setShape: function(shape) {
    	this.shape = shape;
    },
    
    _draw: function(canvas) {
        var stage = this.getStage();
        var context = canvas.getContext();
        var family = [];
        var parent = this.parent;

        family.unshift(this);
        while(parent) {
            family.unshift(parent);
            parent = parent.parent;
        }

        context.save();
        
        var tr = new WB.Transform();
        for(var n = 0; n < family.length; n++) {
            var node = family[n];
            var t = node.getTransform();
            var m = t.getMatrix();
            tr.multiply({m: m});
        }

        /*
         * pre styles include alpha, linejoin
         */
        var absAlpha = this.getAbsoluteAlpha();
        if(absAlpha !== 1) {
            context.globalAlpha = absAlpha;
        }
        this.applyLineJoin(context);

        // draw the shape
        this.appliedShadow = false;
        this.doDraw(canvas, context, tr);
        
        context.restore();
    },
    
    doDraw: function(canvas, context, defaultTransform) {
    	
		var pane = new WB.Pane({canvas: canvas, defaultTransform: defaultTransform});
		
		/*
        context.beginPath();
	    context.rect(0, 0, this.attrs.width, this.attrs.height);
        context.closePath();

		//this.fill(context);
		this.stroke(context);
		*/
		
		var shape = this.shape;
		if (shape) {
			var tr = new WB.Transform();
			//tr.translate(this.attrs.x, this.attrs.y);
			if (shape.localBounds) {
				var dWidth = this.attrs.width;
				var dHeight = this.attrs.height;
				var lWidth = Math.abs(shape.localBounds.bottomright.x - shape.localBounds.topleft.x);
				var lHeight = Math.abs(shape.localBounds.bottomright.y - shape.localBounds.topleft.y);
				var scaleX = 1.0;
				var scaleY = 1.0;
				if (Math.abs(dWidth - lWidth) > 1e-2) {
					scaleX = dWidth / lWidth;
				}
				if (Math.abs(dHeight - lHeight) > 1e-2) {
					scaleY = dHeight / lHeight;
				}
				tr.scale(scaleX, scaleY);

				// compensate
				tr.translate(-shape.localBounds.topleft.x, 
						-shape.localBounds.topleft.y);

				// center
				var dx = (dWidth - lWidth * scaleX) / 2;
				var dy = (dHeight - lHeight * scaleY) / 2;
				tr.translate(dx/scaleX, dy/scaleY);
			}

			var group = new WB.GroupShape({transform: tr, shapes: [shape]});
			group.draw(pane);

		} else {
			pane.context.font = '10px Arial';
			pane.context.fillStyle = 'gray';
			pane.context.fillText('Loading...', 10, 10);
		}
	},
	
    setSize: function() {
        var size = Kinetic.Type._getSize(Array.prototype.slice.call(arguments));
        this.setAttrs(size);
    },
    
    getSize: function() {
        return {
            width: this.attrs.width,
            height: this.attrs.height
        };
    }
});

