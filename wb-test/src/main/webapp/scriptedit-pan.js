

/**
 */
var PanEpisodeView = BaseEpisodeView.extend({
	
	className: "Episode PanEpisode",

	modelIconPath: function() {
		return 'images/compass.png';
	},
	
	modelPosSize: function() {
		var pos = this.model.get('point');
		var x = pos ? pos.x : null;
		var y = pos ? pos.y : null;
		return 'x: ' + Math.round(x) + ', y: ' + Math.round(y);
	},
	
});
viewClassMap['PanEpisode'] = PanEpisodeView;

boardClassMap['PanEpisode'] = BoardElementViewNone;

