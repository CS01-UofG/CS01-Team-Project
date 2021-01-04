const { app, BrowserWindow, ipcMain } = require('electron')
const WebSocket = require('ws');
const express = require('express');

const CHANNEL_NAME = 'main';
// Keep a global reference of the window object. If you don't, the window will
// be closed automatically when the JavaScript object is garbage collected.
var mainWindow = null;

// This method will be called when Electron has finished
// initialization and is ready to create browser windows.
app.on('ready', function() {
    // Create the browser window
    mainWindow = new BrowserWindow({
      width: 1920,
      height: 1080,
      // Disables window headers
      frame: true,
      // Disables dev tools
      webPreferences: {
        devTools: false,
        nodeIntegration: false,  //required to be false for unit testing to work
      },
    });
  

    // and load the index.html of the app.
    mainWindow.loadURL('file://' + __dirname + '/index.html');

    mainWindow.webContents.openDevTools();

    // Returned when the window is closed.
    mainWindow.on('closed', function() {
        // Dereference the window object. Usually you would store windows
        // in an array if your app supports multi windows. This is the time
        // when you should delete the corresponding element.
        mainWindow = null;
    });

    // On a PC, the app will quit when we close all windows.
    // On a Mac, applications must be explicitly closed.
    app.on('window-all-closed', function() {
        if (process.platform != 'darwin') {
            app.quit();
        }
    });
});

/**
 * WEBSOCKET
 */

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

      mainWindow.webContents.send(CHANNEL_NAME, latLong);
  });
});
