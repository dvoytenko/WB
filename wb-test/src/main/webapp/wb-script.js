

WB.Script = WB.Class.extend('Script', {
	
	episodes: null,
	
	init: function(opts) {
		if (opts) {
			for (var k in opts) {
				this[k] = opts[k];
			}
		}
	}
	
});


WB.Episode = WB.Class.extend('Episode', {
	
	pause: null,
	
	init: function(opts) {
		if (opts) {
			for (var k in opts) {
				this[k] = opts[k];
			}
		}
	},
	
	getPointer: function() {
		return null;
	},
	
	prepare: function(board) {
	},
	
	createAnimation: function() {
		return null;
	}
	
});


WB.PauseEpisode = WB.Episode.extend('PauseEpisode', {
	
	pause: null,
	
	createAnimation: function() {
		return new WB.PauseAnimation(pause);
	}
	
});


WB.EpisodeList = WB.Class.extend('EpisodeList', {
	
	episodes: null,
	
	pause: 0,
	
	init: function(opts) {
		for (var k in opts) {
			this[k] = opts[k];
		}
	},
	
	createAnimation: function() {
		return new WB.EpisodeListAnimation(this);
	}
	
});


WB.EpisodeListAnimation = WB.ListAnimation.extend('EpisodeListAnimation', {
	
	init: function(episodeList) {
		this.episodeList = episodeList;
		
		var items = [];
		for (var i = 0; i < this.episodeList.episodes.length; i++) {
			var episode = this.episodeList.episodes[i];
			items.push(episode);
			
			var next = null;
			if (i < this.episodeList.episodes.length - 1) {
				next = this.episodeList.episodes[i + 1];
			}
			
			// remove pointer
			if (episode.getPointer() == 'draw' && 
					(!next || next.getPointer() != 'draw')) {
				items.push(new WB.MoveAwayAnimation());
			}
			
			// pause
			if (next) {
				var pause = next.pause || next.pause == 0 ? 
						next.pause : this.episodeList.pause;
				if (pause) {
					items.push(new WB.PauseAnimation(pause));
				}
			}
		}
		this.items = items;
	},
	
	start: function(board) {
		this._super(board);
		for (var i = 0; i < this.episodeList.episodes.length; i++) {
			this.episodeList.episodes[i].prepare(board);
		}
	}
	
});


WB.MoveAwayAnimation = WB.Animation.extend('MoveAwayAnimation', {
	
	createAnimation: function() {
		return this;
	},
	
	start: function(board) {
		this.board = board;

		var point = this.board.getResetPoint();
		console.log('!MOVE AWAY! ' + JSON.stringify(point));
		
		this.anim = new WB.MoveToAnimation(point);
		
		this.tr = null;
		if (!this.tr) {
			this.anim.start(this.board);
		} else {
			var that = this;
			that.board.withTr(this.tr, function() {
				that.anim.start(that.board);
			});
		}
	},
	
	frame: function(time) {
		if (!this.tr) {
			this.anim.frame(time);
		} else {
			var that = this;
			that.board.withTr(this.tr, function() {
				that.anim.frame(time);
			});
		}
	},
	
	isDone: function() {
		return this.anim.isDone();
	},
	
	end: function() {
		if (!this.tr) {
			this.anim.end();
		} else {
			var that = this;
			that.board.withTr(this.tr, function() {
				that.anim.end();
			});
		}
	},
	
	getTimeLeft: function() {
		return this.anim.getTimeLeft();
	}
	
});

