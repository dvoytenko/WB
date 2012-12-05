

/**
 */
var SpeechEpisodeView = BaseEpisodeView.extend({
	
	className: "Episode SpeechEpisode HasTextEpisode",

	modelIconPath: function() {
		return 'images/microphone.png';
	},

	modelDesc: function() {
		return this.model.get('text');
	},
	
});
viewClassMap['SpeechEpisode'] = SpeechEpisodeView;

boardClassMap['SpeechEpisode'] = BoardElementViewNone;


/**
 */
var SpeechEndEpisodeView = BaseEpisodeView.extend({
	
	className: "Episode SpeechEndEpisode",

	modelIconPath: function() {
		return 'images/microphone-end.png';
	},
	
});
viewClassMap['SpeechEndEpisode'] = SpeechEndEpisodeView;

boardClassMap['SpeechEndEpisode'] = BoardElementViewNone;

