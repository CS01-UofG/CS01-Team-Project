function setupTask2() {
  viewer.scene.mode = Cesium.SceneMode.SCENE3D;

  var user = viewer.entities.add({
    name: "user",
    position: sensorCartesian,
    point: {
      pixelSize: 5,
      color: Cesium.Color.RED,
      outlineColor: Cesium.Color.WHITE,
      outlineWidth: 2,
    },
  });

  // unit vector in cartesian coordinates, the direction the sensor is looking at
  var direction = new Cesium.Cartesian3(1.0, 8.0, 0.0);
  Cesium.Cartesian3.normalize(direction, direction);
  // this is an infinite line that we can use to get the intersection with the terrain
  var ray = new Cesium.Ray(sensorCartesian, direction);

  var intersection = viewer.scene.globe.pick(ray, viewer.scene);

  viewer.entities.add({
    name: "Intersection point",
    position: intersection,
    point: {
      pixelSize: 15,
      color: Cesium.Color.GREEN,
      outlineColor: Cesium.Color.WHITE,
      outlineWidth: 2,
    },
  });

  // some point along the infinite line to get a better feeling of the direction
  for (i = 0; i < 150; ++i) {
    viewer.entities.add({
      position: Cesium.Ray.getPoint(ray, i * 10.0),
      point: {
        pixelSize: 5,
        color: Cesium.Color.RED,
        outlineColor: Cesium.Color.WHITE,
        outlineWidth: 2,
      },
    });
  }

  viewer.zoomTo(user);
}
