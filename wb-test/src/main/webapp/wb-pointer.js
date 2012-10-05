

WB.Pointer = WB.Class.extend({
	
	update: function(state) {
	}
	
});


WB.MarkerPoint = WB.Pointer.extend({
	
	pane: null,
	
	init: function(opts) {
		if (opts && opts.pane) {
			this.pane = opts.pane;
		}

		this.markerImage = new Image();
		this.markerImage.src = "marker-up.png";
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

