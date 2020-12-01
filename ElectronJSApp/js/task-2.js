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

  restaurant("Subway", 55.875270, -4.293983, "Byres Road Subway. Has great student deals");
  coffee("Starbucks Coffee", 55.875333, -4.293415, "Overpriced coffee. What else can I say?");
  coffee("Pret a Manager", 55.875533, -4.293111, "They have great vegetarian sandwiches and wraps.");
  gorcery("Tesco Metro", 55.876036, -4.292530, "Pretty meh when it comes to deals.");
  bank("The Royal Bank of Scotlnad", 55.877033, -4.291973, "Quick and efficient customer service.");
  clothing("Starry Starry Night", 55.875223, -4.294627, "?");
  school("University of Glasgow", 55.872398, -4.289130, "Our school!");

  viewer.zoomTo(user);
}

// A set of functions to add various icons to the map
function restaurant(Name, lat, long, desc) {
  var pinBuilder = new Cesium.pinBuilder();
  var url = Cesium.buildModuleUrl('https://static.thenounproject.com/png/11637-200.png');
  var restaurantPin = Cesium.when(pinBuilder.fromUrl(url, Cesium.Color.RED, 48), function(canvas) {
    return viewer.entities.add({
      name : Name,
      position : Cesium.Cartesian3.fromDegrees(lat, long),
      billboard : {
        image : canvas.toData.URL(),
        verticalOrigin : Cesium.VerticalOrigin.BOTTOM
      },
      description : desc
    })
  })
}

function gorcery(Name, lat, long, desc) {
  var pinBuilder = new Cesium.pinBuilder();
  var url = Cesium.buildModuleUrl('https://www.materialui.co/materialIcons/maps/local_grocery_store_black_192x192.png');
  var groceryPin = Cesium.when(pinBuilder.fromUrl(url, Cesium.Color.GREEN, 48), function(canvas) {
    return viewer.entities.add({
      name : Name,
      position : Cesium.Cartesian3.fromDegrees(lat, long),
      billboard : {
        image : canvas.toData.URL(),
        verticalOrigin : Cesium.VerticalOrigin.BOTTOM
      },
      description : desc
    })
  })
}

function bank(Name, lat, long, desc) {
  var pinBuilder = new Cesium.pinBuilder();
  var url = Cesium.buildModuleUrl('https://cdn.iconscout.com/icon/premium/png-256-thumb/bank-account-banking-building-1-31235.png');
  var bankPin = Cesium.when(pinBuilder.fromUrl(url, Cesium.Color.BLUE, 48), function(canvas) {
    return viewer.entities.add({
      name : Name,
      position : Cesium.Cartesian3.fromDegrees(lat, long),
      billboard : {
        image : canvas.toData.URL(),
        verticalOrigin : Cesium.VerticalOrigin.BOTTOM
      },
      description : desc
    })
  })
}

function clothing(Name, lat, long, desc) {
  var pinBuilder = new Cesium.pinBuilder();
  var url = Cesium.buildModuleUrl('https://icons.iconarchive.com/icons/icons8/windows-8/256/Clothing-T-Shirt-icon.png');
  var clothingPin = Cesium.when(pinBuilder.fromUrl(url, Cesium.Color.PURPLE, 48), function(canvas) {
    return viewer.entities.add({
      name : Name,
      position : Cesium.Cartesian3.fromDegrees(lat, long),
      billboard : {
        image : canvas.toData.URL(),
        verticalOrigin : Cesium.VerticalOrigin.BOTTOM
      },
      description : desc
    })
  })
}

function library(Name, lat, long, desc) {
  var pinBuilder = new Cesium.pinBuilder();
  var url = Cesium.buildModuleUrl('https://static.thenounproject.com/png/79163-200.png');
  var libraryPin = Cesium.when(pinBuilder.fromUrl(url, Cesium.Color.YELLOW, 48), function(canvas) {
    return viewer.entities.add({
      name : Name,
      position : Cesium.Cartesian3.fromDegrees(lat, long),
      billboard : {
        image : canvas.toData.URL(),
        verticalOrigin : Cesium.VerticalOrigin.BOTTOM
      },
      description : desc
    })
  })
}

function coffee(Name, lat, long, desc) {
  var pinBuilder = new Cesium.pinBuilder();
  var url = Cesium.buildModuleUrl('https://www.codester.com/static/uploads/items/5325/icon.png');
  var coffeePin = Cesium.when(pinBuilder.fromUrl(url, Cesium.Color.ORANGE, 48), function(canvas) {
    return viewer.entities.add({
      name : Name,
      position : Cesium.Cartesian3.fromDegrees(lat, long),
      billboard : {
        image : canvas.toData.URL(),
        verticalOrigin : Cesium.VerticalOrigin.BOTTOM
      },
      description : desc
    })
  })
}

function school(Name, lat, long, desc) {
  var pinBuilder = new Cesium.pinBuilder();
  var url = Cesium.buildModuleUrl('https://cdn1.iconfinder.com/data/icons/maps-and-locations/16/school-256.png');
  var schoolPin = Cesium.when(pinBuilder.fromUrl(url, Cesium.Color.INDIGO, 48), function(canvas) {
    return viewer.entities.add({
      name : Name,
      position : Cesium.Cartesian3.fromDegrees(lat, long),
      billboard : {
        image : canvas.toData.URL(),
        verticalOrigin : Cesium.VerticalOrigin.BOTTOM
      },
      description : desc
    })
  })
}