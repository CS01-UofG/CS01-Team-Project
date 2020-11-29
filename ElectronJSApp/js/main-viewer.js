const { ipcRenderer } = require("electron");
/** Define channel name */
const CHANNEL_NAME = "main";

function changeTask(s) {
  switch (s) {
    case tasks.TASK1:
      viewer.entities.removeAll();
      setupTask1();
      break;
    case tasks.TASK2:
      viewer.entities.removeAll();
      setupTask2();
      break;
  }
}

var viewer = new Cesium.Viewer("cesiumContainer");

viewer.terrainProvider = Cesium.createWorldTerrain();

viewer.scene.globe.depthTestAgainstTerrain = true;

// in the future this will be received from the sensor
// currently hard coded
// Glasgow Uni 55.873543, 	-4.289058
// var sensorLong = -4.289058;
// var sensorLat = 55.873543;

// mountains north of Glasgow, we have steep slopes so we can properly see the intersection
// with the earth surface
var sensorLong = -3.72;
var sensorLat = 57.06;

// Show user Long and lat
document.getElementById("longitude").innerHTML = sensorLong;
document.getElementById("latitude").innerHTML = sensorLat;

/** Add IPC event listener which enables user to add points */
ipcRenderer.on(CHANNEL_NAME, (event, data) => {
  console.log(data);
  addPoint(data);
});

// converting sensor from degrees to cartesian coordinates i.e. (x, y, z)
var sensorCartesian = Cesium.Cartesian3.fromDegrees(
  sensorLong,
  sensorLat,
  1012
);

viewer.scene.camera.setView({
  destination: Cesium.Cartesian3.fromDegrees(sensorLong, sensorLat, 40000000.0),
  orientation: {
    heading: Cesium.Math.toRadians(0),
  },
});

setupTask1();
