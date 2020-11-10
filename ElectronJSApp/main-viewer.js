var viewer = new Cesium.Viewer("cesiumContainer", {
    sceneMode: Cesium.SceneMode.SCENE2D
});

// in the future this will be received from the sensor 
// currently hard coded
// Glasgow Uni 55.873543, 	-4.289058
var sensorLong =  -4.289058;
var sensorLat = 55.873543;

// Show user Long and lat 
document.getElementById("longitude").innerHTML = sensorLong;
document.getElementById("latitude").innerHTML = sensorLat;

viewer.scene.camera.setView({
    destination: Cesium.Cartesian3.fromDegrees(sensorLong, sensorLat, 40000000.0),
    orientation: {
      heading: Cesium.Math.toRadians(0),
    },
  });
// this is 1 meter in degrees
const coef = 0.0000089;

// distance between object of interest and the sensor in meters
var objectDistanceFromSensor = 150;
var sensorAngleEquator = 280; // in degrees
// not sure about the name, this is just how wide the field of view is
var sensorVisionAngle = 30;
var sensorMaxVisibility = 160.0;

// converting sensor from degrees to cartesian coordinates i.e. (x, y, z)
var sensorCartesian = Cesium.Cartesian3.fromDegrees(sensorLong, sensorLat);

var user = viewer.entities.add({
    name: 'user',
    position: sensorCartesian,
    point: {
        pixelSize: 5,
        color: Cesium.Color.RED,
        outlineColor: Cesium.Color.WHITE,
        outlineWidth: 2
    },
    description: `Equator angle : ${sensorAngleEquator} <br/> 
    Visibility angle : ${sensorVisionAngle} <br/> 
    Max visibility distance : ${sensorMaxVisibility} m. <br/> 
    Lat: ${sensorLat} <br/> Long: ${sensorLong}`,
});

// distance from sensor to object in x and y
deltaX = Math.sin(sensorAngleEquator * Math.PI / 180) * objectDistanceFromSensor;
deltaY = Math.cos(sensorAngleEquator * Math.PI / 180) * objectDistanceFromSensor;

// https://stackoverflow.com/questions/7477003/calculating-new-longitude-latitude-from-old-n-meters
var objectLat = sensorLat + coef * deltaX;
var objectLong = sensorLong + (coef * deltaY) / Math.cos(sensorLat * 0.018)

var newCartesian = Cesium.Cartesian3.fromDegrees(objectLong, objectLat);

viewer.entities.add({
    name: 'Point of interest',
    position: newCartesian,
    point: {
        pixelSize: 5,
        color: Cesium.Color.RED,
        outlineColor: Cesium.Color.WHITE,
        outlineWidth: 2
    },
    description: `Distance from sensor: ${objectDistanceFromSensor} m. <br/> Lat: ${objectLat} <br/> Long: ${objectLong}`,
});


var fov = viewer.entities.add({
    name: 'Field of view',
    position: sensorCartesian,
    ellipsoid: {
        radii: new Cesium.Cartesian3(sensorMaxVisibility, sensorMaxVisibility, sensorMaxVisibility),
        innerRadii: new Cesium.Cartesian3(1, 1, 1),
        minimumClock: Cesium.Math.toRadians(sensorAngleEquator - sensorVisionAngle / 2),
        maximumClock: Cesium.Math.toRadians(sensorAngleEquator + sensorVisionAngle / 2),
        minimumCone: Cesium.Math.toRadians(89.8),
        maximumCone: Cesium.Math.toRadians(90.2),
        material: new Cesium.Color.fromCssColorString('yellow').withAlpha(0.5),
        outline: true,
        outlineColor: Cesium.Color.ORANGE,
    }
});

viewer.zoomTo(user);
