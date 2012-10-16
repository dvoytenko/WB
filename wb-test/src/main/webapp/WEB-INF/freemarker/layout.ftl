
<#import 'spring.ftl' as spring/>

<#assign USER_EXISTS = Request.User?exists />
<#assign USER = Request.User?if_exists />

<#macro url path><@spring.url path/></#macro>

<#macro root title features=[] bodyClass=''>
<#compress>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">

<head>
	<title>${title?html} - Tealeaf</title>

	<META HTTP-EQUIV="Content-Type" CONTENT="text/html; charset=UTF-8">
	
	<link rel="stylesheet" type="text/css" href="/css/tealeaf.css"/>
	<link rel="stylesheet" type="text/css" href="/css/accounts.css"/>
	
	<link rel="stylesheet" type="text/css" href="/css/smoothness/jquery-ui-1.8.17.custom.css" />	
	<link rel="stylesheet" type="text/css" href="/scripts/tag-it-listedit.css"/>
	
	<script type="text/javascript" src="/scripts/jquery-1.7.1.js"></script>
	<script type="text/javascript" src="/scripts/jquery-ui-1.8.17.custom.min.js"></script>
	<script type="text/javascript" src="/scripts/underscore-1.3.1.js"></script>
	<script type="text/javascript" src="/scripts/backbone-0.9.2-164.js"></script>
	<script type="text/javascript" src="/scripts/radio-link-group.js"></script>
	<script type="text/javascript" src="/scripts/tealeaf.autogrow.js"></script>
	<script type="text/javascript" src="/scripts/tealeaf.js"></script>
	<script type="text/javascript" src="/scripts/tag-it.js"></script>
	
	<#--
	<#if (features?seq_contains('threedub-dnd'))>
		<script type="text/javascript" src="/scripts/jquery.event.drag.js"></script>
		<script type="text/javascript" src="/scripts/jquery.event.drop.js"></script>
	</#if>
	
	<#if (features?seq_contains('raty'))>
		<script type="text/javascript" src="/scripts/jquery.raty.js"></script>
	</#if>
	-->
	
	<script type="text/javascript">
	
		<#--
		<#if (USER_EXISTS)>
			Window.USER = ${JSON.stringify(USER)}
		</#if>
		-->
	
		var WorkspaceRouter = Backbone.Router.extend({
		
			routes: {
				'': 'top',
				's/:query': 'search',  // #s/abc
			},
			
			top: function(query) {
				console.log('search cancelled');
				if (jQuery.isReady) {
					runFilter();
				}
			},
		
			search: function(query) {
				console.log('search intercepted: ' + query);
				var tagids = query ? decodeURIComponent(query).split(',') : null;
				if (!(tagids && tagids.length)) {
					this.top();
				} else {
					console.log('start search for ' + JSON.stringify(tagids));
					console.log('doc ready: ' + jQuery.isReady);
					for (var i = 0; i < tagids.length; i++) {
						var tagid = tagids[i];
						console.log('start search for i ' + tagid);
					}
					if (jQuery.isReady) {
						runFilter();
					}
				}
			}
		
		});
		var router = new WorkspaceRouter();
		Backbone.history.start({pushState: false, silent: false});
		
		function updateFilterUi(filter) {
			console.log('update filter UI');
			console.log(filter);
			var tagit$ = $('#rootSearchInput');
			// TODO ensure sorting
			// var tags = $('#rootSearchInput').tagit('tags');
			if (!filter || !filter.tags || !filter.tags.length) {
				tagit$.tagit('tags', []);
			} else {
				tagit$.tagit('tags', filter.tags);
			}
		}
	
		function changeFilter() {
			var tags = $('#rootSearchInput').tagit('tags');
			if (tags && tags.length) {
				var tagids = _.map(tags, function(t) {return t.tag;});
				console.log('tags via location: ' + tagids.join());
				// router.navigate('s/' + encodeURIComponent(tagids.join()), {trigger: true});
				// window.location.hash = '#s/' + encodeURIComponent(tagids.join());
				window.location = '/#s/' + encodeURIComponent(tagids.join());
			} else {
				console.log('no tags');
				// router.navigate('', {trigger: true});
				window.location = '/#';
			}
		}
		
		function addFilterTag(tag, label) {
			$('#rootSearchInput').tagit('addTag', tag, label);
			changeFilter();
		}
		
		$(document).ready(function()  {
			$('#rootSearchInput').tagit({
				autocomplete_url: '/tags/autocomplete.json',
				addTagMessage: 'Search topics...',
				update: function(tags) {
					changeFilter();
				}
			});
			<#--
			<#if (tags?exists && tags?has_content)>
				<#list tags as t>
					$('#rootSearchInput').tagit('addTag', '${t.tag.stringForm?js_string}', '${t.label?js_string}');
				</#list>
			</#if>
			-->
		});

		$(document).ready(function() {
			var $dropdown = $('#rootHeader .nav-me a.dropdown');
			var $popup = $('#rootHeader .nav-me ul');
			
			var closeCallback = function(e) {
				if (!$(e.target).parents().andSelf().is('#rootHeader .nav-me ul')) {
					$(document).off('click', closeCallback);
					$popup.hide();
				}
			};
			
			$dropdown.click(function(e) {
				if (!$popup.is(':visible')) {
					// open
					$(document).on('click', closeCallback);
					$popup.show();
				} else {
					// close
					$(document).off('click', closeCallback);
					$popup.hide();
				}
				e.stopPropagation();
			});
		});
	</script>
	
</head>

<body <#if (bodyClass?exists && bodyClass?has_content)>class="${bodyClass}"</#if>>
	<#nested/>
</body>
</html>
</#compress>
</#macro>

<#macro rootHeaderContent title search=true features=['tagit'] nav=true bodyClass=''>
	<@root title=title features=features bodyClass=bodyClass>
	
		<div id="rootHeaderOuter">
		
			<div id="rootHeader">
				<h1 id="Logo"><a href="/"><img src="/images/tealeaf3a.png"/></a></h1>
				<#-- Tealeaf -->

				<div id="rootSearchBar">
					<div id="rootSearch">
						<ul id="rootSearchInput" class="tagItInput" style="width: auto;"></ul>
					</div>
					<div id="rootSearchRecent">
						<span>Recent: </span>
						<a href="javascript:void(0);" onclick="addFilterTag('usertag.mylist', 'MyList');">MyList</a>
						<span> | </span>
						<a href="javascript:void(0);" onclick="addFilterTag('usertag.facebook', 'Facebook');">Facebook</a>
					</div>
				</div>

				<div class="sep"></div>

				<div class="nav-me">
					<a class="dropdown" href="javascript:void(0);">me</a>
					<ul>
						<li><a href="/accounts">Accounts</a></li>
						<li><a href="/bm/get-bookmarklet">Bookmarklet</a></li>
						<li><a href="/reports/recap">Digest</a></li>
					</ul>
				</div>

				<div class="sep"></div>
			</div>
			
			<#--
			<li><a href="javascript:void(0);" id="testMenuButton">Tests!</a></li>
			<div id="testMenuPopup">
				<ul>
					<li><a href="/fun">Fun</a></li>
					<li><a href="/sample/twitterui">Twitter UI</a></li>
					<li><a href="/sample/twitterui-recolor">Twitter UI (recolor)</a></li>
					<li><a href="/sample/twitterui-dyn">Twitter UI (dyn)</a></li>
				</ul>
			</div>
			-->
		</div>
		
		<div id="rootContentOuter">
			<div id="rootContent">
				<#--
				<#if (_rootContentMenu?exists)>
					<@menu menu=_rootContentMenu parentStyle='Title' menuStyle='Nav' />
				</#if>
				-->
				<#nested/>
			</div>
		</div>

		<div id="rootFooter">
		</div>
	</@root>
</#macro>

<#macro menu menu parentStyle menuStyle>
	<div class="${parentStyle}">
		<div class="${menuStyle}">
			<ul>
				<#list menu.items as item>
					<li class=" <#if (item.url == menu.selected.url?default(''))> selected</#if>">
						<#t><a href="${item.url}">${item.name?html}</a>
					</li>
				</#list>
			</ul>
		</div>
		<div style="clear: both;"></div>
	</div>
</#macro>

<#macro title title selectors={} select='' basePath=''>
	<div class="Title">
		<h2>${title?html}</h2>
		
		<#if (selectors?exists && selectors?has_content)>
			<div class="Nav">
				<ul>
					<#list selectors?keys as key>
						<li <#if (key == select)>class="selected"</#if>><a href="<@url basePath + '/' + key/>">${selectors[key]?html}</a></li>
					</#list>
				</ul>
			</div>
		</#if>
		<div style="clear: both;"></div>
	</div>
</#macro>

<#macro section title='' width='100%' footer='' closable=false>
	<div class="Section" style="width: ${width};">
		<#if (title?exists && title?has_content)>
			<div class="Header">
				<#if (closable)>
					<div class="HeaderRight">x</div>
				</#if>
				<h2>${title?html}</h2>
			</div>
		</#if>
		<div class="Content">
			<#--
				style="height: 100px;"
			-->
			<#nested/>
		</div>
		<#if (footer?exists && footer?has_content)>
			<div class="Footer">footer</div>
		</#if>
	</div>
</#macro>

<#macro splitScreen>
	<div style="position: relative;">
		<div class="MainContent">
			<#nested 'main'/>
		</div>
		<div class="RightContent">
			<#nested 'right'/>
		</div>
	</div>
</#macro>

<#macro sep styleClass='sep'>
	<div class="${styleClass}"></div>
</#macro>

<#macro clear clear='both'>
	<div style="clear: ${clear};"></div>
</#macro>

<#function icon url def=''>
	<#if (!url?exists || !url?has_content)>
		<#return def/>
	</#if>
	<#if (url == 'me.icon')>
		<#if (USER_EXISTS && USER.pictureUrl?exists && USER.pictureUrl?has_content)>
			<#return USER.pictureUrl/>
		</#if>
	<#else>
		<#return url/>
	</#if>
	<#return def/>
</#function>

