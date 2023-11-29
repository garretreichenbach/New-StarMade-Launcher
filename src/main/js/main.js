var spawn = require('child_process').spawn;
spawn('java', ['-jar', 'starmade-launcher.jar']);
process.exit(0);