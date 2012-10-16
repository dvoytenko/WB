
if (!window.WB_Bookmarklet) {
	(function() {
		console.log('WB_Bookmarklet init v1.0');
		
		var bmBaseUrl = 'http://localhost:8080/wb';
		
		var revPopupDiv = document.createElement('div');
		revPopupDiv.id = 'WB_Bookmarklet_Popup';
		revPopupDiv.style.position = 'fixed'; /* absolute */
		revPopupDiv.style.display = 'none'; 
		revPopupDiv.style.height = 'auto';
		revPopupDiv.style.zIndex = 2147483649;
		revPopupDiv.style.border = '1px dotted gray';
		revPopupDiv.style['border-radius'] = '4px';
		revPopupDiv.style['background-color'] = '#ffffff';
		/*
-webkit-border-radius: 4px;
		 */
		revPopupDiv.tabindex = -1;
		document.body.appendChild(revPopupDiv);
		
		var revPopupTopDiv = document.createElement('div');
		revPopupTopDiv.style.padding = '.4em 1em';
		revPopupTopDiv.style['font-weight'] = 'bold';
		revPopupTopDiv.style.cursor = 'move';
		revPopupTopDiv.style['background-color'] = '#aaaaaa';
		revPopupDiv.appendChild(revPopupTopDiv);

		var revPopupCloseButton = document.createElement('a');
		revPopupCloseButton.innerHTML = 'x';
		revPopupCloseButton.href = 'javascript:void(0)';
		revPopupCloseButton.style.float = 'right';
		revPopupCloseButton.style.color = '#222';
		revPopupCloseButton.style['text-decoration'] = 'none';
		revPopupCloseButton.onclick = function() {
			revPopupDiv.style.display = 'none';
		};
		revPopupTopDiv.appendChild(revPopupCloseButton);
		
		var revPopupTitleLabel = document.createElement('div');
		revPopupTitleLabel.innerHTML = 'WB Shapes Bookmark';
		// revPopupTitleLabel.style.display = 'inline-block';
		revPopupTopDiv.appendChild(revPopupTitleLabel);
		
		var revPopupContentDiv = document.createElement('div');
		revPopupDiv.appendChild(revPopupContentDiv);
		
		var contentIframe = document.createElement('iframe');
		contentIframe.setAttribute('frameborder', 0);
		/*
		contentIframe.setAttribute('allowtransparency', false);
		*/
		contentIframe.style.width = '100%';
		revPopupContentDiv.appendChild(contentIframe);
		
		function receiveMessage(event) {  
			console.log('parent message received ' + event.origin);
			if (event.origin != bmBaseUrl) {  
				return;  
			}
			
			var data = {title: document.title};
			contentIframe.contentWindow.postMessage(JSON.stringify(data), bmBaseUrl);
		}
		window.addEventListener("message", receiveMessage, false);
		
		window.WB_Bookmarklet = function(){
			console.log('WB_Bookmarklet launch: ' + document.title + '; ' + document.location.href);
			
			revPopupTitleLabel.innerHTML = 'WB Shapes: ' + document.title;
			
			// coord
			var dim = ZWWin.findWindowDimensions();
			var width = Math.min(500, dim.width -10);
			console.log('window dim: ' + dim.width + ' x ' + dim.height);
			revPopupDiv.style.width = width + 'px';
			console.log('dialog width: ' + revPopupDiv.style.width);
			contentIframe.style.height = '400px';
			revPopupDiv.style.left = (dim.width - width - 30) + 'px';
			revPopupDiv.style.top = '10px';
			
			contentIframe.style.visibility = 'hidden';
			contentIframe.onload = function() {
				contentIframe.style.visibility = 'visible';
			};
			contentIframe.src = bmBaseUrl + '/bm.html?' 
				+ 'u=' + encodeURIComponent(document.location.href);
			// contentIframe.onload = function() {};
			console.log('content window: ' + contentIframe.contentWindow);
			revPopupDiv.style.display = 'block';
		};
		
		
		/*
		 * Popup Dialog
		 */
		window.addEventListener("resize", function() {
		    ZWWin.docDimensions = ZWWin.findDocumentDimensions();
		    ZWWin.winDimensions = ZWWin.findWindowDimensions();
		}, false);
		
		var ZWWin = {
		    docDimensions : null,
		    winDimensions : null,

		    getWindowDimensions : function()
		    {
		        if (!ZWWin.winDimensions) ZWWin.winDimensions = ZWWin.findWindowDimensions();
		        return ZWWin.winDimensions;
		    },

		    findWindowDimensions : function()
		    {
		        var x,y;
		        if (self.innerHeight) // all except Explorer
		        {
		            x = self.innerWidth;
		            y = self.innerHeight;
		        }
		        else if (document.documentElement && document.documentElement.clientHeight)
		            // Explorer 6 Strict Mode
		        {
		            x = document.documentElement.clientWidth;
		            y = document.documentElement.clientHeight;
		        }
		        else if (document.body) // other Explorers
		        {
		            x = document.body.clientWidth;
		            y = document.body.clientHeight;
		        }
		        return {"width":x, "height":y};
		    },

		    getDocumentDimensions : function ()
		    {
		        if (!ZWWin.docDimensions) ZWWin.docDimensions = ZWWin.findDocumentDimensions();
		        return ZWWin.docDimensions;
		    },

		    findDocumentDimensions : function ()
		    {
		        var x,y;
		        var test1 = document.body.scrollHeight;
		        var test2 = document.body.offsetHeight
		        if (test1 > test2) // all but Explorer Mac
		        {
		            x = document.body.scrollWidth;
		            y = document.body.scrollHeight;
		        }
		        else
		        {
		            // Explorer Mac; would also work in:
		            // Explorer 6 Strict, Mozilla and Safari
		            x = document.body.offsetWidth;
		            y = document.body.offsetHeight;
		        }
		        return {"width":x, "height":y};
		    },
		    //usage: ZWWin.getCenteredTopLeft( {"width":200, "height":400} );
		    getCenteredTopLeft : function (params)
		    {
		        var width   = params.width;
		        var height  = params.height;
		        var dim     = ZWWin.getWindowDimensions();
		        var hScroll = 0;
		        var vScroll = 0;
		        var hPos    = Math.round(hScroll+((dim.width-width)/2));
		        var vPos    = Math.round(vScroll+((dim.height-height)/2));
		        var left    = (hPos < 0)?0:hPos;
		        var top     = (vPos < 0)?0:vPos;
		        return {"top":top, "left":left};
		    },
		    //usage: ZWWin.removeElementbyId('maindiv');
		    removeElementbyId : function (element_name)
		    {
		        d = document.getElementById(element_name);
		        p = d.parentNode ? d.parentNode : d.parentElement;
		        p.removeChild( d );
		    }
		};

		var ZWDrag = {
		    BIG_Z_INDEX : 10000,
		    LAST_Z_INDEX : 10001,
		    isDragging : false,
		    itemindrag : null,
		    mouseXinit : null,
		    mouseYinit : null,
		    lastdragged : null,

		    makeDraggable : function(draggableitem,itemhandle) {
		        itemhandle.draggableitem   = draggableitem;
		        itemhandle.onmousedown     = ZWDrag.onMouseDown;
		        itemhandle.style["cursor"]="move";

		        initPosition = ZWDragUtils.findPos(draggableitem);
		        draggableitem.initPositionX= initPosition.x;
		        draggableitem.initPositionY= initPosition.y;
		        /*
		        draggableitem.style["position"]="absolute";
		        */
		        draggableitem.style["left"]= initPosition.x+"px";
		        draggableitem.style["top"]= initPosition.y+"px";
		        draggableitem.width = parseInt(draggableitem.style["width"]);
		        draggableitem.height= parseInt(draggableitem.style["height"]);
		        draggableitem.originalZIndex  = draggableitem.style["zIndex"];
		        draggableitem.originalOpacity = draggableitem.style["opacity"];
		    },

		    onMouseDown : function(event) {

		        event = ZWDragUtils.fixEvent(event);
		        targ  = ZWDragUtils.findTarget(event);

		        while (!targ.draggableitem && targ!=null)
		            targ = targ.parentNode ? targ.parentNode : targ.parentElement;
		        if (targ==null) return false;
		        
		        if (ZWDrag.lastdragged)
		            ZWDrag.lastdragged.style["zIndex"]  = ZWDrag.lastdragged.originalZIndex;

		        ditem = targ.draggableitem;
		        ditem.originalLeft = parseInt(ditem.style["left"]);
		        ditem.originalTop  = parseInt(ditem.style["top"]);

		        document.onmousemove = ZWDrag.onMouseMove;
		        document.onmouseup = ZWDrag.onMouseUp;
		        ZWDrag.itemindrag = ditem;
		        ZWDrag.isDragging = true;
		        ZWDrag.mouseXinit = event.clientX;
		        ZWDrag.mouseYinit = event.clientY;

		        return false;
		    },

		    onMouseMove : function(event) {
		        event = ZWDragUtils.fixEvent(event);
		        if (ZWDrag.isDragging)
		        {
		            ditem = ZWDrag.itemindrag;
		            ditem.style["zIndex"]  = ZWDrag.BIG_Z_INDEX;
		            ditem.style["opacity"] = 0.75;
		            ZWDragUtils.repositionItem(event,ditem);
		        }

		        return false;
		    },

		    onMouseUp : function(event) {
		        event = ZWDragUtils.fixEvent(event);

		        if (ZWDrag.itemindrag)
		        {
		            ditem = ZWDrag.itemindrag;
//		            ditem.style["zIndex"] = ditem.originalZIndex;
		            ditem.style["zIndex"] = ZWDrag.LAST_Z_INDEX;
		            ditem.style["opacity"]= ditem.originalOpacity;
		            ZWDrag.lastdragged = ditem;
		        }


		        ZWDrag.itemindrag = null;
		        ZWDrag.isDragging = false;
		        ZWDrag.mouseXinit = null;
		        ZWDrag.mouseYinit = null;
		        document.onmousemove = null;
		        document.onmouseup = null;

		        return false;
		    }

		};

		var ZWDragUtils = {

		    fixEvent : function (e) {
		         return (!e) ? window.event : e;
		    },

		    findPos : function (obj) {
		        var curleft = curtop = 0;
		        if (obj.offsetParent) {
		            curleft = obj.offsetLeft
		            curtop = obj.offsetTop
		            while (obj = obj.offsetParent) {
		                curleft += obj.offsetLeft
		                curtop += obj.offsetTop
		            }
		        }
		        return {"x":curleft, "y":curtop};
		    },

		    findTarget : function (e) {
		        if (e.target) targ = e.target;
		        else if (e.srcElement) targ = e.srcElement;
		        if (targ.nodeType == 3) // defeat Safari bug
		        targ = targ.parentNode;
		        return targ;
		    },

		    repositionItem : function (event,ditem) {
		        eventx    = event.clientX;
		        eventy    = event.clientY;

		        newleft   = (ditem.originalLeft + eventx - ZWDrag.mouseXinit)*1;
		        newtop    = (ditem.originalTop + eventy - ZWDrag.mouseYinit)*1;

		        ditem.style["left"] = newleft+"px";
		        ditem.style["top"]  = newtop+"px";
		    }

		};

		function ZWDOMElement(txt) {
		    var o = new Object();
		    o.elementname   = txt;
		    o.attributes    = new Object();
		    o.innerHTML        = '';
		    o.innerText        = '';
		    o.createElement = function()
		    {
		        var attribute_str = '';
		        var css_style_str = '';
		        for(attribute_name in o.attributes)
		        {
		            if (attribute_name.toLowerCase() != 'style')
		                attribute_str+= attribute_name+"='"+escape(o.attributes[attribute_name])+"'";
		            else
		                css_style_str= o.attributes[attribute_name];
		        }

		        var element;
		        try
		        {
		            element = document.createElement("<"+o.elementname+" "+attribute_str);//IE
		        }
		        catch (e)
		        {
		            element = document.createElement( o.elementname );
		            for(attribute_name in o.attributes)
		                element.setAttribute( attribute_name , o.attributes[attribute_name] );
		        }
		        if (css_style_str.length>0)
		            element.style.cssText=css_style_str;
		        if (o.innerHTML.length>0)
		            element.innerHTML = o.innerHTML;
		        else if (o.innerText.length>0)
		            element.appendChild( document.createTextNode(o.innerText) );
		        return element;
		    }

		    o.setAttribute = function(attribute,attribute_value)
		    {
		        o.attributes[attribute] = attribute_value;
		    }
		    return o;
		}
	
		
		ZWDrag.makeDraggable(revPopupDiv, revPopupTitleLabel);
		
	})();

}
