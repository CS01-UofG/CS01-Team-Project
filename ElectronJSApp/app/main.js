// Modules to control application life and create native browser window
const path = require('path')
const WebSocket = require('ws');
const express = require('express');
const {app, BrowserWindow, ipcMain} = require('electron')

var mainWindow = null;

function createWindow () {
  // Create the browser window.
  mainWindow = new BrowserWindow({
    width: 1920,
    height: 1080,

    // Disables dev tools
    webPreferences: {
      devTools: true,
      nodeIntegration: false,
      contextIsolation: true, //required to be false for unit testing to work
      preload: path.join(__dirname, 'preload.js')
    },
  });

  // and load the index.html of the app.
  mainWindow.loadURL('file://' + __dirname + '/index.html')

  // Open the DevTools.
  // mainWindow.webContents.openDevTools()
}

// This method will be called when Electron has finished
// initialization and is ready to create browser windows.
// Some APIs can only be used after this event occurs.
app.whenReady().then(() => {
  createWindow()
  
  app.on('activate', function () {
    // On macOS it's common to re-create a window in the app when the
    // dock icon is clicked and there are no other windows open.
    if (BrowserWindow.getAllWindows().length === 0) createWindow()
  })
})

// Quit when all windows are closed, except on macOS. There, it's common
// for applications and their menu bar to stay active until the user quits
// explicitly with Cmd + Q.
app.on('window-all-closed', function () {
  if (process.platform !== 'darwin') app.quit()
})

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

      mainWindow.webContents.send("fromMain", latLong);
      console.log("getsData");
  });
});
