// Function to add new points on map.
function addPoint(data)
{
  addPointToList(data);

  var newPointCartasian = Cesium.Cartesian3.fromDegrees(
    data[0],
    data[1],
    1012
  );

  viewer.entities.add({
    name: "Point of interest",
    position: newPointCartasian,
    point: {
      pixelSize: 5,
      color: Cesium.Color.YELLOW,
      outlineColor: Cesium.Color.WHITE,
      outlineWidth: 2,
    },
  });
}

// Adds point to the list
function addPointToList(data){
    var node = document.createElement("LI");
    var textnode = document.createTextNode(data[0].concat(data[1]));
    node.appendChild(textnode);
    node.className = "list-group-item";
    document.getElementById("pointsLog").appendChild(node);
  }