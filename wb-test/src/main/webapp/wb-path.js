
/**
 */
WB.PathShape = WB.Shape.extend({
	
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

WB.PathSegment = WB.Segment.extend({
	
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
	}
	
});


WB.PathShapeAnimation = WB.AnimationDelegate.extend({
	
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

WB.PathSegmentAnimation = WB.Animation.extend({
	
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
	},
	
	isDone: function() {
		return this.done;
	},
	
	frame: function(time) {
		
		for (var i = 0; i < this.completeList.length; i++) {
			this.completeList[i].outline(this.pane);
		}
		
		var timeLeft = 0;
		do {
			
			if (!this.wip && this.pendingList.length) {
				do {
					if (!!this.wip) {
						this.wip.end();
						this.completeList.push(this.wip.source);
						this.wip = null;
					}
					if (this.pendingList.length) {
						var part = this.pendingList.splice(0, 1)[0];
						this.wip = new WB.SubAnimation(part, time - timeLeft);
						this.wip.start(this.board);
						// console.log(this.wip.animation);
						// TODO: what happens here with timeLeft? 
						// basically, we didn't spend any time at all
					}
				} while (!!this.wip && this.wip.isDone());
			}

			if (!!this.wip && !this.wip.isDone()) {
				timeLeft = 0;
				this.wip.frame(time);
			}

			if (!!this.wip && this.wip.isDone()) {
				timeLeft = this.wip.getTimeLeft();
				this.wip.end();
		    	this.completeList.push(this.wip.source);
		    	this.wip = null;
			}
		} while (timeLeft > 1 && this.pendingList.length);
		
		this.timeLeft = timeLeft > 1 ? timeLeft : 0;
		this.done = !this.pendingList.length && !this.wip;
	},
	
	getTimeLeft: function() {
		return this.timeLeft;
	}
	
});

