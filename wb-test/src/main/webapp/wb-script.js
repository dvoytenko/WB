

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
		var items = [];
		for (var i = 0; i < this.episodes.length; i++) {
			items.push(this.episodes[i]);
			if (i < this.episodes.length - 1 && this.pause > 0) {
				items.push(new WB.PauseAnimation(this.pause));
			}
		}
		return new WB.ListAnimation(items);
	}
	
});

