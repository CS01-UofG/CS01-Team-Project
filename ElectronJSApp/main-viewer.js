var viewer = new Cesium.Viewer('cesiumContainer');

// this is 1 meter in degrees
const coef = 0.0000089;

// in the future this will be received from the sensor 
// currently hard coded
var sensorLong = -75.166493;
var sensorLat = 39.9060534

// distance between object of interest and the sensor in meters
var objectDistanceFromSensor = 100;
var sensorAngleEquator = 78; // in degrees
// not sure about the name, this is just how wide the field of view is
var sensorVisionAngle = 10;
var sensorMaxVisibility = 120.0;

// converting sensor from degrees to cartesian coordinates i.e. (x, y, z)
var sensorCartesian = Cesium.Cartesian3.fromDegrees(sensorLong, sensorLat);

var citizensBankPark = viewer.entities.add({
    name: 'Citizens Bank Park',
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
        material: new Cesium.Color(0, 0, 0, 0),
        outline: true,
        outlineColor: Cesium.Color.ORANGE,
    }
});

viewer.zoomTo(fov);
