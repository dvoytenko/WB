
var viewClassMap = {};
var boardClassMap = {};


/**
 */
var Episode = Backbone.Model.extend({
	/*
	remove: function() {
		this.destroy();
	}
	*/
	
	select: function() {
		this.trigger('selected', this);
	}
	
});


/**
 */
var EpisodeList = Backbone.Collection.extend({
	
	model: Episode,
	
	comparator: function(episode) {
		return episode.get('seq');
	}
	
});


/**
 */
var BaseEpisodeView = Backbone.View.extend({
	
	tagName: "div",
	
	className: "Episode",
	
	events: {
		'click': 'select'
	},

	initialize: function() {
//		this.model.bind('destroy', this.remove, this);
	},			
	
	select: function(e) {
		//console.log('select');
		if (e) {
			e.stopPropagation();
		}
		this.model.select();
	}
	
});


/**
 */
var EpisodeListView = Backbone.View.extend({
	
	initialize: function() {
		var that = this;
		
		this.model.bind('add', this.addOne, this);
		this.model.bind('reset', this.reAddAll, this);
		this.model.bind('selected', this.episodeSelected, this);
		
		// episode list element
		this.$el.append('<div class="EpisodeList"></div>');
		this.$listEl = this.$el.find('.EpisodeList');

		this.$listEl.sortable({
			update: function() {
				that.sequenceUpdated();
			}
		});
		this.$listEl.disableSelection();
		
		// actions at the bottom
		this.$el.append('<div class="BottomBar"></div>');
		var bottomBar = this.$el.find('.BottomBar');
		
		bottomBar.append('<a class="AddNewEpisodeAction action"></a>');
		var addNewEpisodeAction = this.$el.find('a.AddNewEpisodeAction');
		addNewEpisodeAction.button({label: 'Add New Episode', icons: {primary: 'add'}});
		addNewEpisodeAction.click(function() {
			that.newEpisodeAction();
		});

		/*
		$('#NewEpisodeDialog').dialog({
			autoOpen: false,
			closeOnEscape: true
		});
		$('#NewEpisodeDialog div.EpisodeType').click(function() {
			that.episodeTypeSelected($(this).data('type'));
		});
		*/
	},

	render: function() {
		return this;
	},
	
	reAddAll: function() {
		this.$listEl.html('');
		this.model.each(this.addOne, this);
	},			
	
	getViewClass: function(type) {
		var v = viewClassMap[type];
		if (v) {
			return v;
		}
		throw "Unsupported episode type: " + type;
		//return BaseEpisodeView;
	},
	
	addOne: function(model) {
		console.log('EpisodeListView.addOne');
		console.log(model);
		var viewClass = this.getViewClass(model.get('_type'));
		console.log('viewClass: ' + viewClass);
		
		var view = new viewClass({model: model});
		//console.log('view:');
		//console.log(view);
		
		this.$listEl.append(view.render().el);
		
		model.listView = view;
	},
	
	newEpisodeAction: function() {
		console.log('new episode!');
		/*
		$('#NewEpisodeDialog').dialog('open');
		*/
	},
	
	sequenceUpdated: function() {
		console.log('sequence updated!');
	},
	
	episodeSelected: function(episode) {
		// console.log('EpisodeListView.selected: ');
		// console.log(episode);
		this.$el.find('.Episode.selected').removeClass('selected');
		if (episode.listView) {
			episode.listView.$el.toggleClass('selected', true);
			
			// TODO doesn't work properly yet, retest
			var par = this.$el.offset();
			var off = episode.listView.$el.offset();
			off.top -= par.top;
			off.left -= par.left;
			console.log('new offset: ' + JSON.stringify(off));
			off.left -= 20;
			off.top -= 20;
			this.$el.animate({
			    scrollTop: off.top
			  //  scrollLeft: offset.left
			});
		}
	},
	
});


/**
 */
var BoardElementViewProto = Kinetic.Group.extend({
	
	ready: function() {
	},
	
	setSelected: function(selected) {
	}
	
});

var BoardElementViewNone = BoardElementViewProto.extend({
});


/**
 */
var BoardView = Backbone.View.extend({
	
//	el: $('#EpisodesPane'),
	
	initialize: function() {
		
	    this.stage = new Kinetic.Stage({
	        container: "Board",
	        width: 592,
	        height: 333
	      });
	
	    this.layer = new Kinetic.Layer();
	    this.stage.add(this.layer);

        this.stage.start();
		
		this.model.bind('add', this.addOne, this);
		this.model.bind('reset', this.reAddAll, this);
		this.model.bind('selected', this.episodeSelected, this);
	},

	render: function() {
		return this;
	},
	
	reAddAll: function() {
		this.model.each(this.addOne, this);
	},			
	
	addOne: function(model) {
		console.log('BoardView.addOne');
		console.log(model);
		
		var js = model.toJSON();
		
		// console.log('add shape or placeholder');
		
		var boardClass = this._getBoardClass(js._type);
		
		if (boardClass != BoardElementViewNone) {
			var boardView = new boardClass({
				episode: model,
				spaceWidth: this.stage.attrs.width,
				spaceHeight: this.stage.attrs.height
				});
			this.layer.add(boardView);
			// these properties are best to be set after adding a shape to the layer
			boardView.setZIndex(model.get('seq'));
			
	        this.layer.draw();
	        
	        model.boardView = boardView;
	        
	        boardView.ready();
		}
	},
	
	_getBoardClass: function(type) {
		var v = boardClassMap[type];
		if (v) {
			return v;
		}
		throw "Unsupported episode type (board): " + type;
	},
	
	newEpisodeAction: function() {
		console.log('new episode!');
		/*
		$('#NewEpisodeDialog').dialog('open');
		*/
	},
	
	episodeSelected: function(episode) {
		console.log('BoardView.selected: ');
		console.log(episode);
		for (var i = 0; i < this.model.length; i++) {
			var other = this.model.at(i);
			if (other != episode && other.boardView) {
				other.boardView.setSelected(false);
			}
		}
		if (episode.boardView) {
			episode.boardView.setSelected(true);
		}
		this.layer.draw();
	}
	
});

