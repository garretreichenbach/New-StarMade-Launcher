/**
 * All we have to do is run the StarMade launcher jar. The only reason we are using js is for the custom icon.
 */
var spawn = require('child_process').spawn;
var child = spawn('java', ['-jar', 'starmade-launcher.jar']);
