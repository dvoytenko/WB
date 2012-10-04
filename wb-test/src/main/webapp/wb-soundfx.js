
WB.DrawingSoundEngine = WB.Class.extend({
	
	audio: null,
	
	playing: false,
	
	normalVelocity: 100,
	
    init: function(opts) {
    	
    	this.playing = false;
    	this.normalVelocity = 100;
    	
    	if (!this.audio) {
        	this.audio = new Audio();
    	}
    	console.log('Audio: ' + this.audio);
    	console.log('Can play WAV: ' + this.audio.canPlayType('audio/wav'));
    	console.log('Can play MP3: ' + this.audio.canPlayType('audio/mp3'));
    	
    	var file;
    	if (!!this.audio.canPlayType 
    			&& this.audio.canPlayType('audio/wav')) {
    		file = 'wb-sounds-2.wav';
    	} else {
    		file = 'wb-sounds-2.mp3';
    	}
    	this.audio.setAttribute("src", file);
    	this.audio.setAttribute("loop", "loop");
    	this.audio.load();
    	
    	var audio = this.audio;
    	var that = this;
    	
    	audio.addEventListener('canplay', function(e) {
    		console.log('canplay: ' + e);
    	});
    	audio.addEventListener('canplaythrough', function(e) {
    		console.log('canplaythrough: ' + e);
    	});
    	audio.addEventListener('progress', function(e) {
    		console.log('progress: ' + e);
    	});
    	audio.addEventListener('ended', function(e) {
    		console.log('ended: ' + e);
    		// restart
    		if (that.playing) {
    			console.log('restart');
        		audio.play();
    		}
    	});
    	audio.addEventListener('play', function(e) {
    		console.log('play: ' + e);
    	});
    	audio.addEventListener('pause', function(e) {
    		console.log('pause: ' + e);
    	});
    },
    
    update: function(height, velocity) {
    	if (height > 0.0 || velocity == 0.0) {
    		console.log('velocity = 0 -> stop');
    		this.playing = false;
    		this.audio.pause();
    	} else {
    		if (this.prevVelocity != velocity) {
    			var rate = velocity/this.normalVelocity;
    			rate = Math.max(Math.min(rate, 3.8), 0.7);
    			console.log('rate: ' + rate);
    			this.audio.playbackRate = rate;
    		}
    		this.audio.play();
    		this.playing = true;
    	}
    	
    	this.prevVelocity = velocity;
    },
    
    toString: function() {
    	return 'DrawingSoundEngine[' + 
    				'Audio:' + this.audio
    				'] '
    }

});

