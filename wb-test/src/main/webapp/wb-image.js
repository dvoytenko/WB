

/**
 */
WB.ImageShape = WB.Shape.extend('ImageShape', {
	
	image: null,
	
	init: function(opts) {
		if (opts && opts.image) {
			this.image = opts.image;
		}
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

