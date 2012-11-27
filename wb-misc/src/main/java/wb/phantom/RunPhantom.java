package wb.phantom;

public class RunPhantom {
	
	// phantomjs.exe s1.js 
	
	/*

console.log('loading');

var page = require('webpage').create();
console.log('page created');

page.settings.userName = 'guest';
page.settings.password = 'board!';

page.onConsoleMessage = function(msg) {
    //console.log('page: ' + msg);
};

page.onError = function(msg, trace) {
    console.log('ERROR: ' + msg);
};

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

page.open('http://localhost:8080/wb/wb-test.html', function () {
    console.log('loaded');
    page.evaluate(function() {
	stepScript('script3', {iniSteps: 10});
    });
    window.setTimeout(function() {
        page.render('wb1.png');
	console.log('req control: ' + reqControl);
        phantom.exit();
    }, 5000);
    page.render('wb2.png');
    //phantom.exit();
});

	 */
	
}
