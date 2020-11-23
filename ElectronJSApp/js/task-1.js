function setupTask1() {
  viewer.scene.mode = Cesium.SceneMode.SCENE2D;

  // distance between object of interest and the sensor in meters
  var objectDistanceFromSensor = 140.0;
  var sensorAngleEquator = 270.0; // in degrees
  // not sure about the name, this is just how wide the field of view is
  var sensorVisionAngle = 30.0;
  var sensorMaxVisibility = 160.0;

  var user = viewer.entities.add({
    name: "user",
    position: sensorCartesian,
    point: {
      pixelSize: 5,
      color: Cesium.Color.RED,
      outlineColor: Cesium.Color.WHITE,
      outlineWidth: 2,
    },
    description: `Equator angle : ${sensorAngleEquator} <br/> 
          Visibility angle : ${sensorVisionAngle} <br/> 
          Max visibility distance : ${sensorMaxVisibility} m. <br/> 
          Lat: ${sensorLat} <br/> Long: ${sensorLong}`,
  });

  // distance from sensor to object in x and y
  deltaY =
    Math.sin((sensorAngleEquator * Math.PI) / 180.0) * objectDistanceFromSensor;
  deltaX =
    Math.cos((sensorAngleEquator * Math.PI) / 180.0) * objectDistanceFromSensor;

  // https://stackoverflow.com/questions/7477003/calculating-new-longitude-latitude-from-old-n-meters
  var objectLat = sensorLat + coef * deltaY;
  var objectLong =
    sensorLong + (coef * deltaX) / Math.cos((sensorLat * Math.PI) / 180.0);

  var newCartesian = Cesium.Cartesian3.fromDegrees(objectLong, objectLat);

  viewer.entities.add({
    name: "Point of interest",
    position: newCartesian,
    point: {
      pixelSize: 5,
      color: Cesium.Color.RED,
      outlineColor: Cesium.Color.WHITE,
      outlineWidth: 2,
    },
    description: `Distance from sensor: ${objectDistanceFromSensor} m. <br/> Lat: ${objectLat} <br/> Long: ${objectLong}`,
  });

  var fov = viewer.entities.add({
    name: "Field of view",
    position: sensorCartesian,
    ellipsoid: {
      radii: new Cesium.Cartesian3(
        sensorMaxVisibility,
        sensorMaxVisibility,
        sensorMaxVisibility
      ),
      innerRadii: new Cesium.Cartesian3(0.1, 0.1, 0.1),
      minimumClock: Cesium.Math.toRadians(
        sensorAngleEquator - sensorVisionAngle / 2.0
      ),
      maximumClock: Cesium.Math.toRadians(
        sensorAngleEquator + sensorVisionAngle / 2.0
      ),
      minimumCone: Cesium.Math.toRadians(90.19),
      maximumCone: Cesium.Math.toRadians(90.2),
      material: new Cesium.Color.fromCssColorString("yellow").withAlpha(0.5),
      outline: true,
      outlineColor: Cesium.Color.ORANGE,
    },
  });

  viewer.zoomTo(user);
}
