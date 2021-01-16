// This file is required by the index.html file and will
// be executed in the renderer process for that window.
// All of the Node.js APIs are available in this process.

window.api.receive("fromMain", (data) => {
    console.log("[CLIENT]Received Data", data)
    addPoint(data);
});