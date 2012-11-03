

/**
 */
var DrawTextEpisodeView = BaseEpisodeView.extend({
	
	className: "Episode DrawTextEpisode",

	modelIconPath: function() {
		return 'images/quill.png';
	},
	
	modelPosSize: function() {
		var pos = this.model.get('position');
		var x = pos ? pos.x : null;
		var y = pos ? pos.y : null;
		var width = this.model.get('width');
		var height = this.model.get('height');
		return 'x: ' + Math.round(x) + ', y: ' + Math.round(y)
			+ ', w: ' + Math.round(width) + ', h: ' + Math.round(height);
	}
	
});
viewClassMap['DrawTextEpisode'] = DrawTextEpisodeView;


var BoardTextEditable = BoardShapeEditable.extend({
	
	init: function(config) {
        this.setDefaultAttrs({
        	insets: {top: 4, bottom: 4, left: 4, right: 4}
        });
        this._super(config);
        this.shapeType = 'BoardTextEditable';
	},
	
	estimateSize: function(width, height) {
		
		var text = this.episode.get('text');
		if (!text) {
			text = 'aaa';
		}
		var bh = 30;
		var bw = bh * text.length * 0.6;

		if (!width && !height) {
			height = 30;
		}
		
		if (height) {
			width = bw * height/bh;
		} else {
			height = bh * width/bw;
		}
		return {width: width, height: height};
	},

	loadShape: function() {
    	var text = this.episode.get('text');
    	if (!text) {
    		text = '?';
    	}
    	this._downloadAndSetShape('service/text/getshape.json' + 
    			'?text=' + encodeURIComponent(text));
	},
	
	calcSize: function(shape) {
		if (!shape.width && !shape.height) {
			return null;
		}
		return {width: shape.width, height: shape.height};
	},
	
});
boardClassMap['DrawTextEpisode'] = BoardTextEditable;

