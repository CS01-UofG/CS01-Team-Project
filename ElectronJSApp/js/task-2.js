function setupTask2() {
  viewer.scene.mode = Cesium.SceneMode.SCENE3D;
}

// A set of functions to add various icons to the map
function restaurant(Name, lat, long) {
  var pinBuilder = new Cesium.pinBuilder();
  var url = Cesium.buildModuleUrl('https://static.thenounproject.com/png/11637-200.png');
  var restaurantPin = Cesium.when(pinBuilder.fromUrl(url, Cesium.Color.RED, 48), function(canvas) {
    return viewer.entities.add({
      name : Name,
      position : Cesium.Cartesian3.fromDegrees(lat, long),
      billboard : {
        image : canvas.toData.URL(),
        verticalOrigin : Cesium.VerticalOrigin.BOTTOM
      }
    })
  })
}

function gorcery(Name, lat, long) {
  var pinBuilder = new Cesium.pinBuilder();
  var url = Cesium.buildModuleUrl('https://www.materialui.co/materialIcons/maps/local_grocery_store_black_192x192.png');
  var groceryPin = Cesium.when(pinBuilder.fromUrl(url, Cesium.Color.GREEN, 48), function(canvas) {
    return viewer.entities.add({
      name : Name,
      position : Cesium.Cartesian3.fromDegrees(lat, long),
      billboard : {
        image : canvas.toData.URL(),
        verticalOrigin : Cesium.VerticalOrigin.BOTTOM
      }
    })
  })
}

function bank(Name, lat, long) {
  var pinBuilder = new Cesium.pinBuilder();
  var url = Cesium.buildModuleUrl('https://cdn.iconscout.com/icon/premium/png-256-thumb/bank-account-banking-building-1-31235.png');
  var bankPin = Cesium.when(pinBuilder.fromUrl(url, Cesium.Color.BLUE, 48), function(canvas) {
    return viewer.entities.add({
      name : Name,
      position : Cesium.Cartesian3.fromDegrees(lat, long),
      billboard : {
        image : canvas.toData.URL(),
        verticalOrigin : Cesium.VerticalOrigin.BOTTOM
      }
    })
  })
}

function clothing(Name, lat, long) {
  var pinBuilder = new Cesium.pinBuilder();
  var url = Cesium.buildModuleUrl('https://icons.iconarchive.com/icons/icons8/windows-8/256/Clothing-T-Shirt-icon.png');
  var clothingPin = Cesium.when(pinBuilder.fromUrl(url, Cesium.Color.PURPLE, 48), function(canvas) {
    return viewer.entities.add({
      name : Name,
      position : Cesium.Cartesian3.fromDegrees(lat, long),
      billboard : {
        image : canvas.toData.URL(),
        verticalOrigin : Cesium.VerticalOrigin.BOTTOM
      }
    })
  })
}