

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
		
		var pointer = state.pointer || 'draw';
		console.log('pointer ' + state.pointer + ' -> ' + pointer);
		
		// console.log('marker: update: ' + JSON.stringify(state.position));
		this.pane._clearCanvas();
		if (state.position && this.markerImage.complete && pointer == 'draw') {
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
			console.log('render hand @ ' + mx + ',' + my);
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
		this.markerImage.src = 'pointer/HandPoint.png';
		
		this.pointers = {
			draw: {
				x: 0,
				y: 0,
				width: 146,
				height: 449,
				anchorX: 31,
				anchorY: 31
			}
			/*
			move: {
				x: 146,
				y: 0,
				width: 146,
				height: 449,
				anchorX: 77,
				anchorY: 18
			}
			*/
		};
	},
	
	update: function(state) {
		var pointer = state.pointer || 'draw';
		//console.log('pointer ' + state.pointer + ' -> ' + pointer);
		
		// console.log('marker: update: ' + JSON.stringify(state.position));
		this.pane._clearCanvas();
		
		var pt = this.pointers[pointer];
		
		/*
			original size: 146x449 (1/3.1)
			anchor: 32,31
		*/
		if (state.position && this.markerImage.complete && pt) {
			var height = 550;
			var width = height / (pt.height / pt.width);
			var mx = state.position.x - pt.anchorX * width/pt.width;
			var my = state.position.y - pt.anchorY * height/pt.height;
			
			var context = this.pane.context;
			
//			console.log('marker: image ' + JSON.stringify([state.position.x, state.position.y,
//			                                               pt.x, pt.y, pt.width, pt.height,
//			                                       		mx, my, width, height]));
            context.drawImage(this.markerImage,
            		pt.x, pt.y, pt.width, pt.height,
            		mx, my, width, height);
		}
		
		/*
		if (state.position && this.markerImage.complete && pointer == 'draw') {
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
//			console.log('render hand @ ' + mx + ',' + my);
            context.drawImage(this.markerImage, mx, my, width, height);
//			context.restore();
		}
		*/
	}
	
});


WB.SwiperPoint = WB.Pointer.extend('SwiperPoint', {
	
	pane: null,
	
	init: function(opts) {
		if (opts && opts.pane) {
			this.pane = opts.pane;
		}

		this.markerImage = new Image();
		this.markerImage.src = 'pointer/swiper-648w.png';
	},
	
	update: function(state) {
		var pointer = state.pointer || 'draw';
//		console.log('pointer ' + state.pointer + ' -> ' + pointer);
		
		// console.log('marker: update: ' + JSON.stringify(state.position));
		this.pane._clearCanvas();
		if (state.position && this.markerImage.complete) {
			/*
				original size: 648x840 (1/1.296)
				anchor: 30,35
			*/
			var height = 550;
			var width = height / 1.296;
			var mx = state.position.x - 30*width/648;
			var my = state.position.y - 35*height/840;
			
			var context = this.pane.context;
			
//			context.save();
//            context.shadowColor = 'black';
//            context.shadowBlur = 30;
//            context.shadowOffsetX = -5;
//            context.shadowOffsetY = 5;
//			console.log('render hand @ ' + mx + ',' + my);
            context.drawImage(this.markerImage, mx, my, width, height);
//			context.restore();
		}
	}
	
});

