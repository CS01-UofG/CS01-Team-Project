const { app, BrowserWindow, ipcMain } = require('electron')
const WebSocket = require('ws');
const express = require('express');

let win;
const CHANNEL_NAME = 'main';

// Create Websocket - Inspiration https://github.com/mafikes/electron-websocket-express
const Config = {
  http_port: '6969',
  socket_port: '3030',
  local_host: '127.0.0.1'
};

// Http server
const server = require('http').Server(express);
server.listen(Config.http_port);

// WSS server
const wss = new WebSocket.Server({port: Config.socket_port});


// Create Electron Window
function createWindow() {
  
  win = new BrowserWindow({
    width: 1920,
    height: 1080,
    // Disables window headers
    frame: true,
    // Disables dev tools
    webPreferences: {
      devTools: false,
      nodeIntegration: true
    },
  });

  /** Open devTools */
  win.webContents.openDevTools();

  /** Load the index.html page */
  win.loadFile('app/index.html');

}

const init = () => {
  /** Create app window */
  createWindow();
  // Can use this place to initialize other things :) 
};

app.on('ready', init);


app.on("window-all-closed", () => {
  if (process.platform !== "darwin") {
    app.quit();
  }
});

app.on("activate", () => {
  if (BrowserWindow.getAllWindows().length === 0) {
    createWindow();
  }
})

/**
 * WEBSOCKET
 */
wss.getUniqueID = function () {
  function s4() {
      return Math.floor((1 + Math.random()) * 0x10000).toString(16).substring(1);
  }

  return s4() + s4() + '-' + s4();
};

var latLong;

wss.on('connection', function connection(ws, req) {

  console.log("Server is open on port", Config.socket_port);

  ws.on('close', function close() {
      console.log('[SERVER]: Client disconnected.');
  });

  ws.on('message', function incoming(recieveData) {
      latLong = recieveData.split(',');
      console.log('[SERVER] Message:', latLong);
      // This sends it to the front end using the channel name whenever it gets data 
      // Resource https://gist.github.com/talyguryn/5c46f26b55ffc6aea1bb3d3b03899a04
      win.webContents.send(CHANNEL_NAME, latLong);
  });
});
