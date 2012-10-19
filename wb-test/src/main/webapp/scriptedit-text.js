

/**
 */
var DrawTextEpisodeView = BaseEpisodeView.extend({
	
	className: "Episode DrawTextEpisode",
	
	render: function() {
		var model = this.model.toJSON();
		// console.log('render: ' + JSON.stringify(model));
		
		this.$el.html(
				'<div class="Icon"><img/></div>' + 
				'<div class="Desc"></div>' + 
				'<div style="clear: both;"></div>');
		
		this.$el.find('div.Icon img').attr('src', 'images/quill.png');
		this.$el.find('div.Desc').text('"' + model.text + '"');
		
		return this;
	},
	
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
	
	calcLocalBounds: function(shape) {
		if (!shape.realWidth && !shape.realHeight) {
			return null;
		}
		return {
			topleft: {x: 0, y: 0},
			bottomright: {x: shape.realWidth, y: shape.realHeight}
		};
	},
	
});
boardClassMap['DrawTextEpisode'] = BoardTextEditable;

