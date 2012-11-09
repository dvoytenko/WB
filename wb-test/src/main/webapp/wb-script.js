

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
	
	prepare: function(board) {
	},
	
	createAnimation: function() {
		return null;
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
			items.push(this.episodeList.episodes[i]);
			if (i < this.episodeList.episodes.length - 1 && this.episodeList.pause > 0) {
				items.push(new WB.PauseAnimation(this.episodeList.pause));
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
