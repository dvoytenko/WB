
console.log('loading');

var page = require('webpage').create();
console.log('page created');

page.settings.userName = 'guest';
page.settings.password = 'board!';

//page.onConsoleMessage = function(msg) {
//    console.log('PAGE: ' + msg);
//};

page.onError = function(msg, trace) {
    console.log('ERROR: ' + msg);
};

/*
var reqControl = 0;
page.onResourceRequested = function(request) {
    //console.log('Request ' + JSON.stringify(request, undefined, 4));
    reqControl++;
    //console.log('req: ' + reqControl);
};
page.onResourceReceived = function(response) {
    //console.log('Receive ' + JSON.stringify(response, undefined, 4));
    //console.log('res: ' + reqControl);
    reqControl--;
};
*/

function waitFor(testFx, onReady, timeOutMillis) {
    var maxtimeOutMillis = timeOutMillis ? timeOutMillis : 3600000; //< Default Max Timout is 3s
    var start = new Date().getTime();
    var condition = false;
    var interval = setInterval(function() {
		if ((new Date().getTime() - start < maxtimeOutMillis) && !condition) {
			// If not time-out yet and condition not yet fulfilled
			condition = (typeof(testFx) === "string" ? eval(testFx) : testFx()); //< defensive code
		} else {
			if(!condition) {
			    // If condition still not fulfilled (timeout but condition is 'false')
				console.log("'waitFor()' timeout");
			    phantom.exit(1);
			} else {
			    // Condition fulfilled (timeout and/or condition is 'true')
				console.log("'waitFor()' finished in " + (new Date().getTime() - start) + "ms.");
				typeof(onReady) === "string" ? eval(onReady) : onReady(); //< Do what it's supposed to do once the condition is fulfilled
				clearInterval(interval); //< Stop this interval
			}
		}
    }, 1000); //< repeat check every 1s
};

page.open('http://localhost:8080/wb/wb-test.html', function () {
	console.log('loaded');
    
	page.evaluate(function() {
		recordScript('script3');
	});
    
	waitFor(
		function() {
			return page.evaluate(function() {
				return $("#done").is(":visible");
			});
		}, 
		function() {
			console.log("looks like finished");
			phantom.exit();
		});
});
