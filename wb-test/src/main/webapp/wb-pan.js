

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
		this.velocity = board.getBaseVelocity();
		
		this.startPoint = board.getAnchorPoint();
		console.log('pan: startPoint = ' + JSON.stringify(this.startPoint));
		this.endPoint = this.pan.point;
		console.log('pan: endPoint = ' + JSON.stringify(this.endPoint));

		this.inter = new WB.PointInterpolator(this.startPoint, this.endPoint, 
				this.velocity/1000);
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
		var screenBounds = pane.globalBounds();
		var resetPos = {
				x: screenBounds.topleft.x + 30, 
				y: screenBounds.bottomright.y + 30};
		console.log('!RESET POS! ' + JSON.stringify(resetPos));
		pane.moveTo(resetPos);
	    this.board.state({position: resetPos});
	}
	
});

