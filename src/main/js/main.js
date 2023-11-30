var spawn = require('child_process').spawn;
spawn('java', ['-jar', 'starmade-launcher.jar']);
setTimeout(function() {
    process.exit();
}, 5000);