const { app, BrowserWindow } = require('electron')
const WebSocket = require('ws');
const express = require('express');

// Create Websocket - Inspiration https://github.com/mafikes/electron-websocket-express
const Config = {
  http_port: '6969',
  socket_port: '3030',
  local_host: '127.0.0.1'
};

// Http server
const _app = express();
const server = require('http').Server(_app);
server.listen(Config.http_port);

// WSS server
const wss = new WebSocket.Server({port: Config.socket_port});


// Create Electron Window
function createWindow() {
  const win = new BrowserWindow({
    width: 1200,
    height: 1000,
    // Disables window headers
    frame: true,
    // Disables dev tools
    webPreferences: {
      devTools: false,
    },
  });

  win.loadFile("index.html");
  win.webContents.openDevTools();
}

app.whenReady().then(createWindow);

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

// More Websockets Stuff 
/**
 * WEBSOCKET
 */
wss.getUniqueID = function () {
  function s4() {
      return Math.floor((1 + Math.random()) * 0x10000).toString(16).substring(1);
  }

  return s4() + s4() + '-' + s4();
};

wss.on('connection', function connection(ws, req) {

  console.log("Server is open on port", Config.socket_port);

  ws.on('close', function close() {
      console.log('[SERVER]: Client disconnected.');
  });

  ws.on('message', function incoming(recieveData) {
      console.log('[SERVER] Message:', recieveData);
  });
});