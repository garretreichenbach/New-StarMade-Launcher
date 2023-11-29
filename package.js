const packager = require('electron-packager');
const path = require('path');
const fs = require('fs');

const options = {
    dir: '.',
    out: './release-builds',
    name: 'StarMade Launcher',
    overwrite: true,
    icon: path.join(__dirname, 'assets', 'starmade'),
    asar: true,
}

const platforms = [
    // { platform: 'darwin', arch: 'x64', icon: options.icon + '.icns' }, Todo: This is not working in GitHub Actions, and I can't run it locally since I'm on Windows
    { platform: 'win32', arch: 'ia32', icon: options.icon + '.ico' },
    { platform: 'linux', arch: 'x64', icon: options.icon + '.png' }
]

for (let opts of platforms) {
    packager(Object.assign({}, options, opts), function (error, appPaths) {
        if (error) {
            console.error(`Error packaging app for platform ${opts.platform}: ${error}`);
        } else {
            console.log(`Packaged to ${appPaths}`);
        }
    });
}