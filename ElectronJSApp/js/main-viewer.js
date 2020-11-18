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

setupTask1();
