
var viewClassMap = {};
var boardClassMap = {};


/**
 */
var Episode = Backbone.Model.extend({
	
	select: function() {
		this.trigger('selected', this);
	},
	
	remove: function() {
		// TODO
		// this.destroy();
		this.trigger('destroy', this);
	},
	
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
		this.model.bind('destroy', this.remove, this);
		this.model.bind('change', this.changed, this);
	},			
	
	render: function() {
		
		this.$el.html(
				'<div class="Icon"><img/></div>' + 
				'<div class="Info">' +
					'<div class="Title"></div>' +
					'<div class="Desc"></div>' +
					'<div class="PosSize"></div>' +
				'</div>' +
				'<div class="Tools">' +
					'<div class="Seq"></div>' +
					'<div class="Menu"><a>Menu</a></div>' +
				'</div>' +
				'<div class="footer"></div>' +
				'<div class="MenuPopup" style="position: absolute; z-index: 5000;"><ul></ul></div>');
		
		this.$iconEl = this.$el.find('div.Icon img');
		this.$titleEl = this.$el.find('div.Title');
		this.$descEl = this.$el.find('div.Desc');
		this.$posSizeEl = this.$el.find('div.PosSize');
		this.$seqEl = this.$el.find('div.Seq');
		
		this.$menuEl = this.$el.find('div.Menu a');
		this.$menuPopupEl = this.$el.find('div.MenuPopup');
		this.$menuUlEl = this.$menuPopupEl.find('ul');
		
		this.$menuPopupEl.click(function() {return false;});
		
		this.$menuUlEl.append($('<li><a class="Action Delete">Delete</a></li>'));
		
		this.$menuPopupEl.popup({
			trigger: this.$menuEl,
			position: {
				my: "right top",
				at: "right bottom"
			}
			});
		
		var that = this;
		this.$menuUlEl.find('a.Delete').click(function() {
			console.log('DELETE!');
			that.closePopupMenu();
			var confirmDialog = $('<div>Are you sure?</div>');
			confirmDialog.dialog({
				autoOpen: true, 
				title: 'Confirm delete',
				buttons: {
					'Yes': function() {
						that.model.remove();
						confirmDialog.dialog('close');
					},
					'Cancel': function() {
						confirmDialog.dialog('close');
					}
				}});
		});
		
		/*
		var $test = this.$el.find('div.test');
		var images = [
		              'arrow_white_on_gray_16',
		              'arrow_white_on_gray_22',
		              'arrow_white_on_gray_32',
		              'arrow_white_on_gray_48',
		              'br_down_16',
		              'br_down_32',
		              'br_down_48',
		              'rnd_br_down_16',
		              'rnd_br_down_32',
		              'rnd_br_down_48',
		              'down_circle_22',
		              'down_circle_32',
		              'down_16',
		              'badge_down_32',
		              'control_down',
		              'navigation_down_frame',
		              'navigation_down_frame2',
		              'toggle_down',
		              'navigation_down_button',
		    ];
		for (var i = 0; i < images.length; i++) {
			var img = $('<img style="margin-left: 10px; width: 32px; height: 32px;"/>');
			img.attr('src', 'images/' + images[i] + '.png');
			img.attr('title', images[i]);
			$test.append(img);
		}
		*/
		
		this.doRender();
		
		this.changed(this.model);

		return this;
	},
	
	doRender: function() {
	},
	
	modelIconPath: function() {
		return this.model.get('icon');
	},

	setIconPath: function(iconPath) {
		this.$iconEl.attr('src', iconPath ? iconPath : '');
	},
	
	modelTitle: function() {
		return this.model.get('title');
	},
	
	setTitle: function(title) {
		this.$titleEl.text(title ? title : '');
	},
	
	modelDesc: function() {
		return this.model.get('desc');
	},
	
	setDesc: function(desc) {
		this.$descEl.text(desc ? desc : '');
	},
	
	modelPosSize: function() {
		return null;
	},
	
	setPosSize: function(posSize) {
		this.$posSizeEl.text(posSize ? posSize : '');
	},
	
	closePopupMenu: function() {
		this.$menuPopupEl.popup('close');
	},
	
	select: function(e) {
		//console.log('select');
		if (e) {
			e.stopPropagation();
		}
		this.closePopupMenu();
		this.model.select();
	},
	
	changed: function(model, event) {
		
		// standard attributes
		this.setIconPath(this.modelIconPath());
		this.setTitle(this.modelTitle());
		this.setDesc(this.modelDesc());
		this.setPosSize(this.modelPosSize());
		this.$seqEl.text(this.model.get('seq'));
		
		// the rest
		this.doChanged(event);
	},
	
	doChanged: function(event) {
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
		this.model.bind('change', this.episodeChanged, this);
		
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
		console.log('EpisodeListView.selected: ');
		console.log(episode);
		this.$el.find('.Episode.selected').removeClass('selected');
		if (episode.listView) {
			episode.listView.$el.toggleClass('selected', true);
			
			/*
			var scrollTo = null;
			var containerHeight = this.$el.height();
			var containerTop = this.$el.scrollTop(); 
			var containerBottom = containerTop + containerHeight;
			console.log('container top ' + containerTop + '; bottom ' + containerBottom);
			var elemTop = episode.listView.$el.offset().top - this.$el.offset().top;
			var elemBottom = elemTop + episode.listView.$el.height();
			console.log('elem top ' + elemTop + '; bottom ' + elemBottom);
			if (containerTop + elemTop < containerTop) {
				scrollTo = containerTop + elemTop;
			} else if (containerTop + elemBottom > containerBottom) {
				scrollTo = containerTop + elemBottom - containerHeight;
			}
			
			if (scrollTo) {
				console.log('scroll to: ' + scrollTo);
				//this.$el.scrollTop(scrollTo);
				this.$el.animate({scrollTop: scrollTo});
			}
			*/
			
			/*
			episode.listView.$el[0].scrollIntoView(true);
			*/
		
			var el = episode.listView.$el;
			//var el = this.model.at(0).listView.$el;
			console.log('el top: ' + this.$el.offset().top + '/' + this.$el.scrollTop());
			console.log('list top: ' + this.$listEl.offset().top + '/' + this.$listEl.scrollTop());
			console.log('elem top: ' + el.offset().top);
			console.log('1st elem top: ' + this.model.at(0).listView.$el.offset().top);
			var scrollTo = el.offset().top - this.$listEl.offset().top;
			console.log(scrollTo);
			this.$el.animate({scrollTop: scrollTo});
		}
	},
	
	episodeChanged: function(model, event) {
	}
	
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
		
		var canvasWidth = 720; // 592 (-128)
		var canvasHeight = 405; // 333
		
	    this.stage = new Kinetic.Stage({
	        container: "Board",
	        width: canvasWidth,
	        height: canvasHeight
	      });
	    
	    // this.stage.setScale(0.9, 0.9);
	    // this.stage.setOffset(-100, -100);
	    // this.stage.setScale(0.2, 0.2);
	    
	    function pos(layer, x, y) {
	    	var tr = layer.getAbsoluteTransform();
	    	tr.translate(x, y);
	    	return tr.getTranslation();
	    }
	    
	    this.bgLayer = new Kinetic.Layer({
	    	drawFunc: function() {
	    		console.log('_backgroundDraw:');
	    		console.log(this);
	    		var canvas = this.getCanvas();
	    		var context = this.getContext();
	    		console.log(canvas);
	    		console.log(context);
	            var topleft = pos(this, 0, 0);
	            var topright = pos(this, 700, 0);
	            var bottomright = pos(this, 700, 400);
	            var bottomleft = pos(this, 0, 400);
//	    		console.log(topleft); // 20,20
//	    		console.log(topright) // 160,20
//	    		console.log(bottomright); // 160,100
//	    		console.log(bottomleft); // 20,100
	    		
	    		// var startX = topleft.x - canvasWidth;
	    		
	    		var offset = this.getStage().getOffset();
	    		var scale = this.getStage().getScale();
	    		
	    		var offsetX = - offset.x * scale.x;
	    		var offsetY = - offset.y * scale.y;
	    		var w = canvasWidth * scale.x;
	    		var h = canvasHeight * scale.y;
	    		
	    		var startX = offsetX - w * Math.floor(offsetX / w) - w;
	    		var startY = offsetY - h * Math.floor(offsetY / h) - h;
	    		
	    		console.log(offsetX);
	    		console.log(offsetY);
	    		console.log(w);
	    		console.log(h);
	    		console.log(startX);
	    		
	    		context.save();
	    		
	    		context.strokeStyle = '#ddd';
	    		context.globalAlpha = 0.5;
	    		context.strokeWidth = 1;
	    		for (var x = startX; x <= canvasWidth; x += w) {
	    			context.beginPath();
	    			context.moveTo(x, 0);
	    			context.lineTo(x, canvasHeight);
	    			context.stroke();
	    		}
	    		for (var y = startY; y <= canvasHeight; y += h) {
	    			context.beginPath();
	    			context.moveTo(0, y);
	    			context.lineTo(canvasWidth, y);
	    			context.stroke();
	    		}
	    		
	    		context.strokeStyle = '#aaa';
	    		context.globalAlpha = 1;
	    		context.strokeWidth = 2;
	    		context.strokeRect(offsetX, offsetY, w, h);
	    		
	    		context.restore();
	    	}
	    });
	    this.stage.add(this.bgLayer);
	
	    this.layer = new Kinetic.Layer();
	    this.stage.add(this.layer);

        this.stage.start();
		
		this.model.bind('reset', this.reAddAll, this);
		this.model.bind('add', this.addOne, this);
		this.model.bind('remove', this.episodeRemoved, this);
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
		
		console.log('add shape or placeholder');
		
		var boardClass = this._getBoardClass(js._type);
		console.log(boardClass);
		
		if (boardClass != BoardElementViewNone) {
			var boardView = new boardClass({
				episode: model,
				spaceWidth: this.stage.attrs.width,
				spaceHeight: this.stage.attrs.height
				});
			console.log('boardView:');
			console.log(boardView);
			console.log('boardView.type:' + boardView.shapeType);
			this.layer.add(boardView);
			// these properties are best to be set after adding a shape to the layer
			boardView.setZIndex(model.get('seq'));
			boardView.setSelected(false);
			
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
	
	episodeRemoved: function(model) {
		console.log('REMOVE FROM BOARD!');
		if (model.boardView) {
			this.layer.remove(model.boardView);
			this.layer.draw();
		}
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
		console.log(this.model.length);
		for (var i = 0; i < this.model.length; i++) {
			console.log(i);
			var other = this.model.at(i);
			if (other != episode && other.boardView) {
				other.boardView.setSelected(false);
			}
		}
		if (episode.boardView) {
			console.log(episode.boardView);
			episode.boardView.setSelected(true);
		}
		console.log('layer draw');
		this.layer.draw();
	}
	
});

