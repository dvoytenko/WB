

WB.Pointer = WB.Class.extend('Pointer', {
	
	beforeFrame: function() {
	},
	
	update: function(state) {
	}
	
});


WB.MarkerPoint = WB.Pointer.extend('MarkerPoint', {
	
	pane: null,
	
	init: function(opts) {
		if (opts && opts.pane) {
			this.pane = opts.pane;
		}

		this.markerImage = new Image();
		this.markerImage.src = 'pointer/marker-up.png';
	},
	
	update: function(state) {
		// console.log('marker: update: ' + JSON.stringify(state.position));
		this.pane._clearCanvas();
		if (state.position && this.markerImage.complete) {
			/*
				original size: 300x300
				anchor: 20,15
			*/
			var mx = state.position.x - 20*70/300;
			var my = state.position.y - 15*70/300;
			
			var context = this.pane.context;
			
//			context.save();
//            context.shadowColor = 'black';
//            context.shadowBlur = 30;
//            context.shadowOffsetX = -5;
//            context.shadowOffsetY = 5;
            context.drawImage(this.markerImage, mx, my, 70, 70);
//			context.restore();
		}
	}
	
});


WB.HandPoint = WB.Pointer.extend('HandPoint', {
	
	pane: null,
	
	init: function(opts) {
		if (opts && opts.pane) {
			this.pane = opts.pane;
		}

		this.markerImage = new Image();
		this.markerImage.src = 'pointer/hand-450w.png';
	},
	
	update: function(state) {
		// console.log('marker: update: ' + JSON.stringify(state.position));
		this.pane._clearCanvas();
		if (state.position && this.markerImage.complete) {
			/*
				original size: 146x449 (1/3.1)
				anchor: 32,31
			*/
			var height = 550;
			var width = height / 3.1;
			var mx = state.position.x - 32*width/146;
			var my = state.position.y - 31*height/449;
			
			var context = this.pane.context;
			
//			context.save();
//            context.shadowColor = 'black';
//            context.shadowBlur = 30;
//            context.shadowOffsetX = -5;
//            context.shadowOffsetY = 5;
            context.drawImage(this.markerImage, mx, my, width, height);
//			context.restore();
		}
	}
	
});

