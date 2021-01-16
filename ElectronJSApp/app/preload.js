// // https://stackoverflow.com/questions/57807459/how-to-use-preload-js-properly-in-electron
// const {
//     contextBridge,
//     ipcRenderer
// } = require("electron");

// // Expose protected methods that allow the renderer process to use

// // the ipcRenderer without exposing the entire object
// contextBridge.exposeInMainWorld(
//     "not_api", {
//         send: (channel, data) => {
//             // whitelist channels
//             let validChannels = ["main"];
//             console.log(channel)
//             if (validChannels.includes(channel)) {
//                 ipcRenderer.send(channel, data);
//             }
//         },
//         get: (channel, func) => {
//             let validChannels = ["fromMain"];
//             if (validChannels.includes(channel)) {
//                 // Deliberately strip event as it includes `sender` 
//                 ipcRenderer.on(channel, (event, ...args) => func(...args));
//             }
//         }
//     }
// );
