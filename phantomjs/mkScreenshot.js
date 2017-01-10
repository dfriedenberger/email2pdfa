var system = require('system');
var args = system.args;

var address = args[1];
var imagefile = args[2];

var page = require('webpage').create();

//viewportSize being the actual size of the headless browser
page.viewportSize = { width: 1024, height: 768 };

//the clipRect is the portion of the page you are taking a screenshot of
//page.clipRect = { top: 0, left: 0, width: 1024, height: 768 };


console.log('The default user agent is ' + page.settings.userAgent);
page.settings.userAgent = 'SpecialAgent';

console.log('open ' + address);
page.open(address, function (status) {
    console.log('Open status=' +  status);

    if (status !== 'success') {
        console.log('Unable to load the address!');
        phantom.exit();
    } else {
    	
    	//Set white background if page is transparent
    	page.evaluate(function() {
              console.log('Evaluate Page');
    		  var style = document.createElement('style'),
    		      text = document.createTextNode('body { background: #fff }');
    		  style.setAttribute('type', 'text/css');
    		  style.appendChild(text);
    		  document.head.insertBefore(style, document.head.firstChild);
    		});
    	
        window.setTimeout(function () {
            console.log('render ' +  imagefile);

			//page.render(imagefile, { format: 'jpeg', quality: '100'} );
			page.render(imagefile );
            phantom.exit();
        }, 100); // Change timeout as required to allow sufficient time 
    }
});

