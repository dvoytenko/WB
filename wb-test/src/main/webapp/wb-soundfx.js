
WB.DrawingSoundEngine = WB.Class.extend('DrawingSoundEngine', {
	
	audio: null,
	
	playing: false,
	
	baseVelocity: 100,
	
    init: function(opts) {
    	
    	this.playing = false;
    	
    	if (opts && opts.baseVelocity) {
    		this.baseVelocity = opts.baseVelocity;
    	}
    	
    	if (!this.audio) {
        	this.audio = new Audio();
    	}
    	console.log('soundfx: audio: ' + this.audio);
    	console.log('soundfx: can play WAV: ' + this.audio.canPlayType('audio/wav'));
    	console.log('soundfx: can play MP3: ' + this.audio.canPlayType('audio/mp3'));
    	
    	var file;
    	if (!!this.audio.canPlayType 
    			&& this.audio.canPlayType('audio/wav')) {
    		file = 'wb-sounds-2.wav';
    	} else {
    		file = 'wb-sounds-2.mp3';
    	}
    	this.audio.setAttribute("src", file);
    	// this.audio.setAttribute("loop", "loop");
    	this.audio.load();
    	
    	var audio = this.audio;
    	var that = this;
    	
    	audio.addEventListener('canplay', function(e) {
//    		console.log('soundfx: canplay: ' + e);
    	});
    	audio.addEventListener('canplaythrough', function(e) {
//    		console.log('soundfx: canplaythrough: ' + e);
    	});
    	audio.addEventListener('progress', function(e) {
//    		console.log('soundfx: progress: ' + e);
    	});
    	audio.addEventListener('ended', function(e) {
//    		console.log('soundfx: ended: ' + e);
    		// restart
    		if (that.playing) {
//    			console.log('soundfx: restart');
        		audio.play();
    		}
    	});
    	audio.addEventListener('play', function(e) {
//    		console.log('soundfx: play: ' + e);
    	});
    	audio.addEventListener('pause', function(e) {
//    		console.log('soundfx: pause: ' + e);
    	});
    },
    
    beforeFrame: function() {
    },
    
    update: function(state) {
    	// console.log('soundfx: update: ' + JSON.stringify(state));
    	if (state.height > 0.0 || !state.velocity) {
    		this.playing = false;
    		if (!this.audio.paused) {
//        		console.log('soundfx: velocity = 0 -> stop');
        		this.audio.pause();
    		}
    	} else {
    		if (this.prevVelocity != state.velocity) {
    			var rate = state.velocity/this.baseVelocity;
    			rate = Math.max(Math.min(rate, 3.8), 0.7);
//    			console.log('soundfx: rate: ' + rate);
    			this.audio.playbackRate = rate;
    		}
    		this.audio.play();
    		this.playing = true;
    	}
    	
    	this.prevVelocity = state.velocity;
    },
    
    toString: function() {
    	return 'DrawingSoundEngine[' + 
    				'Audio:' + this.audio
    				'] '
    }

});

