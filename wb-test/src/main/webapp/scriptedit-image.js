

/**
 */
var PasteImageEpisodeView = BaseEpisodeView.extend({
	
	className: "Episode PasteImageEpisode",
	
	modelIconPath: function() {
		if (this.model.get('imageId')) {
			return 'imagedb/' + this.model.get('imageId') + '.png';
		}
		return 'images/image.png';
	},
	
	modelPosSize: function() {
		var pos = this.model.get('position');
		var x = pos ? pos.x : null;
		var y = pos ? pos.y : null;
		var width = this.model.get('width');
		var height = this.model.get('height');
		return 'x: ' + Math.round(x) + ', y: ' + Math.round(y)
			+ ', w: ' + Math.round(width) + ', h: ' + Math.round(height);
	},
	
});
viewClassMap['PasteImageEpisode'] = PasteImageEpisodeView;

boardClassMap['PasteImageEpisode'] = BoardShapeEditable;
//boardClassMap['PasteImageEpisode'] = BoardElementViewNone;

