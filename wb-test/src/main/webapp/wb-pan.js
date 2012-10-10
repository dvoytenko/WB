

WB.PanEpisode = WB.Episode.extend({
	
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


WB.PanAnimation = WB.Animation.extend({
	
	pan: null,
	
	init: function(pan) {
		this.pan = pan;
	},
	
	start: function(board) {
		this.board = board;
		this.velocity = board.getBaseVelocity();
		
		this.startPoint = board.getAnchorPoint();
		console.log('pan: startPoint = ' + JSON.stringify(this.startPoint));
		this.endPoint = this.pan.point;
		console.log('pan: endPoint = ' + JSON.stringify(this.endPoint));
		
    	this.dx = this.endPoint.x - this.startPoint.x;
    	this.dy = this.endPoint.y - this.startPoint.y;
        this.totalDistance = WB.Geom.distance(this.startPoint, this.endPoint);
		
        this.done = this.totalDistance < 1.0;
	},
	
	isDone: function() {
		return this.done;
	},
	
	frame: function(time) {
		
		var distance = time * this.velocity / 1000;
		if (distance > this.totalDistance) {
			distance = this.totalDistance;
		}

        var x2 = this.dx * distance/this.totalDistance;
        var y2 = this.dx != 0 ? (this.dy/this.dx) * x2 : 
			this.dy * distance/this.totalDistance;
		
        var newPoint = WB.Geom.movePoint(this.startPoint, x2, y2);
        this.board.updateAnchorPoint(newPoint);
        
	    this.done = Math.abs(this.totalDistance - distance) < 1.0;
	}
	
});

