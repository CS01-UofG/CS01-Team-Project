const assert = require('assert');
const path = require('path');
const Application = require('spectron').Application;
const  electronPath  = require('electron')

// Links to implement
// https://stackoverflow.com/questions/53206680/get-all-test-fails-from-mocha
// https://stackoverflow.com/questions/57505082/would-it-be-safe-to-enable-nodeintegration-in-electron-on-a-local-page-that-is-p


const app = new Application({
  path: electronPath,
  args: [path.join(__dirname, '..')]
});

describe('Electron app tests', function () {

  // Currently causes erorrs which are unresolved 
  
  this.timeout(10000);

  //Start the electron app before each test
  beforeEach(() => {
    return app.start();
  });

  //Stop the electron app after completion of each test
  afterEach(() => {
    if (app && app.isRunning()) {
      return app.stop();
    }
  });

  it('display the electron app window', async () => {
    const count = await app.client.getWindowCount();
    return assert.equal(count, 1);
  });

  
  
});