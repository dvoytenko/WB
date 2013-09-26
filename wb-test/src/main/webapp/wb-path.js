
/**
 */
WB.PathShape = WB.Shape.extend('PathShape', {
	
	pathSegment: null,
	
	init: function(opts) {
		if (opts && opts.pathSegment) {
			this.pathSegment = opts.pathSegment;
		}
		if (!this.pathSegment) {
			this.pathSegment = new WB.PathSegment();
		}
		if (opts && opts.segments) {
			this.pathSegment.segments = opts.segments;
		}
	},
	
	draw: function(pane) {
		pane.beginPath();
		this.pathSegment.outline(pane);
		pane.stroke();
	},
	
	createAnimation: function() {
		return new WB.PathShapeAnimation(this);
	}
	
});

WB.PathSegment = WB.Segment.extend('PathSegment', {

	segments: null,
	
	init: function(opts) {
		if (opts && opts.segments) {
			this.segments = opts.segments;
		}
	},
	
	outline: function(pane) {
		for (var i = 0; i < this.segments.length; i++) {
			this.segments[i].outline(pane);
		}
	},

	createAnimation: function() {
		return new WB.PathSegmentAnimation(this);
	},
	
	polygon: function(pane, points) {
		for (var i = 0; i < this.segments.length; i++) {
			this.segments[i].polygon(pane, points);
		}
	}
});


WB.PathShapeAnimation = WB.AnimationDelegate.extend('PathShapeAnimation', {
	
	pathShape: null,
	
	init: function(pathShape) {
		this.pathShape = pathShape;
		this.animation = pathShape.pathSegment.createAnimation();
	},
	
	end: function() {
		this.board.commitShape(this.pathShape, true);
	},
	
	frame: function(time) {
		this.pane.beginPath();
		this.animation.frame(time);
		this.pane.stroke();
	}
	
});

WB.PathSegmentAnimation = WB.Animation.extend('PathSegmentAnimation', {
	
	ps: null,
	
	init: function(ps) {
		this.ps = ps;
	},
	
	start: function(board) {
		this.board = board;
		this.pane = board.animationPane;
		
		this.completeList = [];
		this.pendingList = [];
		
		var segments = this.ps.segments;
		for (var i = 0; i < segments.length; i++) {
			/*
			if (!(segment instanceof MoveToSegment)) {
				this.pendingList.add(new ChangeAngleSegment(segment));
			}
			*/
			this.pendingList.push(segments[i]);
		}
		this.timeLeft = 0;
		this.done = !this.pendingList.length;
		this.prevTime = 0;
	},
	
	isDone: function() {
		return this.done;
	},
	
	frame: function(time) {
		
		for (var i = 0; i < this.completeList.length; i++) {
			this.completeList[i].outline(this.pane);
		}

		var currentTime = this.prevTime;
		do {
			
			if (!this.wip && this.pendingList.length) {
				do {
					if (this.wip) {
						this.wip.end();
						this.completeList.push(this.wip.source);
						this.wip = null;
					}
					if (this.pendingList.length) {
						var part = this.pendingList.splice(0, 1)[0];
						this.wip = new WB.SubAnimation(part, currentTime);
						this.wip.start(this.board);
					}
				} while (this.wip && this.wip.isDone());
			}
			
			if (this.wip) {
				
				// do a frame if not over
				if (!this.wip.isDone()) {
					this.wip.frame(time);
				}
				
				// complete frame if finished in the last frame
				if (this.wip.isDone()) {
					var timeLeft = this.wip.getTimeLeft();
					currentTime = time - (timeLeft ? timeLeft : 0);
					this.wip.end();
			    	this.completeList.push(this.wip.source);
			    	this.wip = null;
				} else {
					currentTime = time;
				}
			}

		} while (currentTime < time && this.pendingList.length);
		
		this.done = !this.pendingList.length && !this.wip;
		this.timeLeft = time > currentTime + 1 ? time - currentTime : 0;
		this.prevTime = currentTime;
	},
	
	getTimeLeft: function() {
		return this.timeLeft;
	}
	
});

