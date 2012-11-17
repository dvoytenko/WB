

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
			if (i < this.episodeList.episodes.length - 1) {
				var next = this.episodeList.episodes[i + 1];
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
