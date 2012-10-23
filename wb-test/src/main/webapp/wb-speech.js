

WB.SpeechPlayer = WB.Class.extend('SpeechPlayer', {
	
	audio: null,
	
	urlResolver: null,

    init: function(opts) {
    	
		if (opts && opts.urlResolver) {
			this.urlResolver = opts.urlResolver;
		} else {
			this.urlResolver = function(path) {return path;};
		}
    	
    	if (!this.audio) {
        	this.audio = new Audio();
        	this.audio.preload = 'auto';
    	}
    	console.log('speech: audio: ' + this.audio);
    	console.log('speech: can play WAV: ' + this.audio.canPlayType('audio/wav'));
    	console.log('speech: can play MP3: ' + this.audio.canPlayType('audio/mp3'));
    	
    	var audio = this.audio;
    	var that = this;
    	
    	audio.addEventListener('canplay', function(e) {
    		console.log('speech: canplay: ' + e);
    		that.state = 'loaded';
    		// TODO: testing
    		//audio.play();
    	});
    	audio.addEventListener('canplaythrough', function(e) {
    		console.log('speech: canplaythrough: ' + e);
    	});
    	audio.addEventListener('progress', function(e) {
    		console.log('speech: progress: ' + e);
    	});
    	audio.addEventListener('ended', function(e) {
    		console.log('speech: ended: ' + e);
    		that.state = 'ended';
    	});
    	audio.addEventListener('play', function(e) {
    		console.log('speech: play: ' + e);
        	// that.state = 'playing';
    	});
    	audio.addEventListener('playing', function(e) {
    		console.log('speech: playing: ' + e);
        	that.state = 'playing';
    	});
    	audio.addEventListener('pause', function(e) {
    		console.log('speech: pause: ' + e);
    		that.state = 'paused';
    	});
    	audio.addEventListener('abort', function(e) {
    		console.log('speech: abort: ' + e);
    		that.state = 'aborted';
    	});
    	audio.addEventListener('error', function(e) {
    		console.log('speech: error: ' + e);
    		that.state = 'error';
    	});
    	audio.addEventListener('waiting', function(e) {
    		console.log('speech: waiting: ' + e);
    	});
    	audio.addEventListener('loadedmetadata', function(e) {
    		console.log('speech: loadedmetadata: ' + e);
    	});
    	audio.addEventListener('stalled', function(e) {
    		console.log('speech: stalled: ' + e);
    	});
    	audio.addEventListener('suspend', function(e) {
    		console.log('speech: suspend: ' + e);
    	});
//    	audio.addEventListener('timeupdate', function(e) {
//    		console.log('speech: timeupdate: ' + e);
//    	});
    	
    	this.state = 'none';
    },
    
    play: function(track) {
		console.log('speech: play ' + track);
		console.log('speech: current state: ' + this.state);
    	
    	// decide b/w wav and other formats
    	var file = this.urlResolver(track + '.wav');
    	console.log('speech: play file: ' + file);
    	
    	this.state = 'loading';
    	this.audio.setAttribute("src", file);
    	this.audio.load();
    	//this.audio.playbackRate = 1;
		this.audio.play();
    },
    
    getState: function() {
    	return this.state;
    },
    
    beforeFrame: function() {
    },
    
    update: function(state) {
    }
	
});


WB.SpeechPlayerPseudo = WB.Class.extend('SpeechPlayerPseudo', {
	
	urlResolver: null,

    init: function(opts) {
    	
		if (opts && opts.urlResolver) {
			this.urlResolver = opts.urlResolver;
		} else {
			this.urlResolver = function(path) {return path;};
		}
    	
    	this.state = 'none';
    },
    
    play: function(track) {
    	// decide b/w wav and other formats
    	var file = this.urlResolver(track + '.wav');
    	console.log('speech: play file: ' + file);
    	this.state = 'playing';
    	this.playingUrl = file;
    },
    
    getState: function() {
    	return this.state;
    },
    
    beforeFrame: function() {
    	if (this.state == 'playing') {
    		this.state = 'ended';
    	}
    	this.playingUrl = null;
    },
    
    update: function(state) {
    }
	
});


WB.SpeechEpisode = WB.Episode.extend('SpeechEpisode', {
	
	track: null,
	
	init: function(opts) {
		if (opts && opts.track) {
			this.track = opts.track;
		}
	},
	
	createAnimation: function() {
		return new WB.SpeechAnimation(this);
	}
	
});


WB.SpeechAnimation = WB.Animation.extend('SpeechAnimation', {
	
	init: function(speech) {
		this.speech = speech;
	},
	
	start: function(board) {
		this.board = board;
		if (this.board.speechPlayer) {
			// TODO customize this behavior. allow scheduling ahead of time?
			this.waitToStart = new WB.SpeechEndEpisode().createAnimation();
			this.waitToStart.start(board);
			if (this.waitToStart.isDone()) {
				this.waitToStart.end();
				this.waitToStart = null;
				this.board.speechPlayer.play(this.speech.track);
			}
			this.done = false;
		} else {
			this.done = true;
		}
	},
	
	isDone: function() {
		return this.done;
	},
	
	frame: function(time) {
		if (this.done) {
			return;
		}
		
		if (this.waitToStart) {
			// console.log('waiting for previous to finish');
			this.waitToStart.frame(time);
			if (this.waitToStart.isDone()) {
				this.waitToStart.end();
				this.waitToStart = null;
				this.board.speechPlayer.play(this.speech.track);
			}
		}

		if (this.waitToStart) {
			return;
		}
		
		var state = this.board.speechPlayer.getState();
		// console.log('wait for start: ' + state);
		this.done = state == 'loaded' || state == 'ended' || state == 'playing'
			|| state == 'error';
	}
	
});


WB.SpeechEndEpisode = WB.Episode.extend('SpeechEndEpisode', {
	
	init: function(opts) {
	},
	
	createAnimation: function() {
		return new WB.WaitForAnimation(function() {
			if (!this.board.speechPlayer) {
				return true;
			}
			var state = this.board.speechPlayer.getState();
			// console.log('waitFor state: ' + state);
			return state != 'loading' && state != 'loaded' && state != 'playing';
		});
	}
	
});

