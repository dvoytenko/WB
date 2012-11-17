
/**
 */
WB.PasteImageEpisode = WB.ShapeEpisodeBase.extend('PasteImageEpisode', {
});


/**
 */
WB.ImageShape = WB.Shape.extend('ImageShape', {
	
	image: null,
	
	data: null,
	
	width: null,
	
	height: null,
	
	init: function(opts) {
		if (opts && opts.image) {
			this.image = opts.image;
		}
		
		if (opts && opts.data) {
			this.data = opts.data;
			this.image = new Image();
			this.image.src = 'data:image/png;base64,' + opts.data;
			this.image.onload = function() {
				console.log('image loaded');
			};
			this.image.onerror = function() {
				console.log('image error!');
			};
			//console.log('Image:');
			//console.log(opts.data);
			//console.log(this.image);
		}
		
		if (opts && opts.width) {
			this.width = opts.width;
		}
		if (opts && opts.height) {
			this.height = opts.height;
		}
	},
	
	isReady: function() {
		return !this.image || this.image.complete;
	},
	
	draw: function(pane) {
		pane.drawImage(this.image, 0, 0, this.image.width, this.image.height);
	},
	
	createAnimation: function() {
		if (this.animationFactory) {
			return this.animationFactory(this);
		}
		return null;
	}
	
});

