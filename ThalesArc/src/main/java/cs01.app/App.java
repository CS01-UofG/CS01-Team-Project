package cs01.app;
import com.esri.arcgisruntime.concurrent.ListenableFuture;
import com.esri.arcgisruntime.mapping.*;
import cs01.ComponentFactory;

import com.alibaba.fastjson.JSON;
import com.esri.arcgisruntime.ArcGISRuntimeEnvironment;
import com.esri.arcgisruntime.geoanalysis.LocationLineOfSight;
import com.esri.arcgisruntime.geoanalysis.LocationViewshed;
import com.esri.arcgisruntime.geoanalysis.Viewshed;
import com.esri.arcgisruntime.geometry.*;
import com.esri.arcgisruntime.geometry.Point;
import com.esri.arcgisruntime.layers.ArcGISSceneLayer;
import com.esri.arcgisruntime.mapping.view.*;
import com.esri.arcgisruntime.symbology.SimpleLineSymbol;
import com.esri.arcgisruntime.symbology.SimpleMarkerSymbol;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Orientation;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.awt.*;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.DecimalFormat;
import java.util.*;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class App extends Application {
    private ArrayList<Sensor> sensorData = new ArrayList<Sensor>();
    private ArrayList<Target> targetList = new ArrayList<Target>();

    private Point user;
    TextField userTextField = new TextField();
    private LocationViewshed userViewshed;

    private Desktop desktop = Desktop.getDesktop();
    private File logfile;

    private GraphicsOverlay userOverlay;
    private GraphicsOverlay pointsOverlay;
    private GraphicsOverlay polygonLayer;
    private AnalysisOverlay fovOverlay;
    private ComponentFactory componentFactory = new ComponentFactory();
    private SceneView sceneView;
    private AnalysisOverlay viewshedOverlay;
    public TitledPane options = new TitledPane();
    public VBox leftBox = new VBox(options);

    public ListView<Text> pointVisualList = new ListView<Text>();
    public VBox leftBox2 = new VBox(pointVisualList);

    private ToggleButton FOVToggle;
    private ToggleButton polylinesToggle;
    private ToggleButton visibilityToggle;
    private ToggleButton frustumToggle;

    private Button createPolylines;
    private Button openFileButton;
    private Button createFileButton;
    private Button addLogs;
    private Button cameraButton;
    private Button clearAllEntities;

    private ListenableFuture<IdentifyGraphicsOverlayResult> identifyGraphics;



    public static void main(String[] args) {
        Application.launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws IOException {
        String yourApiKey = "AAPKb67d305991d24fa89d69e310a58fa1f8xryw7mKYrKOZF9_A49GmTH_Bqj9GSEiAxI9Rv6Uqdj3ProCQ-S1D1dpYNNhW4mjk";
        ArcGISRuntimeEnvironment.setApiKey(yourApiKey);

        primaryStage.setTitle("ThalesArc");
        //Create central pane and add toggle buttons to open hidden panes on the
        StackPane centre = new StackPane();

        //Create left-hand titled pane for the points list and main it in VBox
        options.setText("Options");

        // create a scene and add a basemap to it
        ArcGISScene scene = new ArcGISScene();
        scene.setBasemap(Basemap.createImagery());

        // add the SceneView to the stack pane
        sceneView = new SceneView();
        sceneView.setArcGISScene(scene);

        // add a scene layer for buildings - Located in Berst France
        final String buildings = "https://tiles.arcgis.com/tiles/P3ePLMYs2RVChkJx/arcgis/rest/services/Buildings_Brest/SceneServer/layers/0";
        ArcGISSceneLayer sceneLayer = new ArcGISSceneLayer(buildings);
        scene.getOperationalLayers().add(sceneLayer);

//        // Elevation Layer
//        ArcGISTiledElevationSource elevationSource = new ArcGISTiledElevationSource("https://elevation3d.arcgis.com/arcgis/rest/services/WorldElevation3D/Terrain3D/ImageServer");
//        Surface surface = new Surface();
//        surface.getElevationSources().add(elevationSource);
//        scene.setBaseSurface(surface);

        // add base surface for elevation data
        Surface surface = new Surface();
        surface.getElevationSources().add(new ArcGISTiledElevationSource("https://elevation3d.arcgis.com/arcgis/rest/services/WorldElevation3D/Terrain3D/ImageServer"));
        scene.setBaseSurface(surface);
        // create an analysis overlay for the line of sight
        fovOverlay = new AnalysisOverlay();
        sceneView.getAnalysisOverlays().add(fovOverlay);
        fovOverlay.setVisible(false);

        // create an analysis overlay for the userViewshed
        viewshedOverlay = new AnalysisOverlay();
        sceneView.getAnalysisOverlays().add(viewshedOverlay);

        // create a graphics overlay and add it to the map view for points and more
        userOverlay = new GraphicsOverlay(GraphicsOverlay.RenderingMode.DYNAMIC);
        userOverlay.getSceneProperties().setSurfacePlacement(LayerSceneProperties.SurfacePlacement.ABSOLUTE);
        sceneView.getGraphicsOverlays().add(userOverlay);

        // create a graphics overlay and add it to the map view for points and more
        pointsOverlay = new GraphicsOverlay(GraphicsOverlay.RenderingMode.DYNAMIC);
        pointsOverlay.getSceneProperties().setSurfacePlacement(LayerSceneProperties.SurfacePlacement.ABSOLUTE);
        sceneView.getGraphicsOverlays().add(pointsOverlay);
        // create a graphics overlay and add polylines to it and set to false
        polygonLayer = new GraphicsOverlay(GraphicsOverlay.RenderingMode.DYNAMIC);
        polygonLayer.getSceneProperties().setSurfacePlacement(LayerSceneProperties.SurfacePlacement.ABSOLUTE);
        sceneView.getGraphicsOverlays().add(polygonLayer);

        ArcGISMap map = new ArcGISMap(Basemap.createStreets());
        // create a view and set map to it
        MapView mapView = new MapView();
        mapView.setMap(map);

        sceneView.addSpatialReferenceChangedListener(src -> throwConfirmationAlert("Scene Loaded in"));

        // Hardcoded to Brest France
        setInitialViewPoint(-4.484419007880914, 48.39127111485687);

        // Settings for Filestorage
        createFileButton = new Button("Create a log file");
        createFileButton.setOnAction(
            new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent actionEvent) {
                    createFile();
                }
            }
        );
        // Settings for Filestorage
        addLogs = new Button("Save to log file");
        addLogs.setOnAction(
                new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent actionEvent) {
                        Boolean sucess = false;
                        try {
                            if (logfile == null) {
                                // create an alert
                                throwErrorAlert("No selected Log file, create new or open.");
                            } else {
                                appendFile();
                                sucess = true;
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        } finally{
                            if (sucess) {
                                throwConfirmationAlert("Saved to log file");
                            }
                        }
                    }
                }
        );
        // Lets user open a file button
        openFileButton = new Button("Open a JSON File");
        final FileChooser fileChooser = new FileChooser();
        openFileButton.setOnAction(
            new EventHandler<ActionEvent>() {
                @Override
                public void handle(final ActionEvent e) {
                    configureFileChooser(fileChooser);
                    File file = fileChooser.showOpenDialog(primaryStage);
                    if (file != null) {
                        openFile(file);
                    }
                }
        });
        // Lets the user clear all entites
        clearAllEntities = new Button("Clear entites off map");
        clearAllEntities.setOnAction(
                new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent actionEvent) {
                        clearAll();
                    }
                }
        );

        leftBox.getChildren().addAll(userTextField, createFileButton,openFileButton, addLogs, clearAllEntities);

        // Sockets implementation and create a new thread
        new Thread(() -> {
            try {
                // Create a server socket
                ServerSocket serverSocket = new ServerSocket(userData.port);

                //append message of the Text Area of UI (GUI Thread)
                Platform.runLater(()
                        ->
                        pointVisualList.getItems().add(new Text("New server started at " + userData.host +' '+ userData.port + '\n')));

                //continous loop
                while (true) {
                    // Listen for a connection request, add new connection to the list
                    Socket socket = serverSocket.accept();
                    String fromClient;
                    BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                    fromClient = in.readLine();
                    System.out.println("received: " + fromClient);

                    Platform.runLater(() -> {
                        readJSON(fromClient);
                    });
                }
            } catch (IOException ex) {
                leftBox.getChildren().add(new Text(ex.toString()));
            }
        }).start();


        sceneView.setOnMouseClicked(e -> {
            try{
                if (e.getButton() == MouseButton.PRIMARY && e.isStillSincePress()) {
                    // create a point from location clicked
                    Point2D mapViewPoint = new Point2D(e.getX(), e.getY());
                    Point mapPoint = sceneView.screenToBaseSurface(mapViewPoint);
//                     identify graphics on the graphics overlay
                    identifyGraphics = sceneView.identifyGraphicsOverlayAsync(pointsOverlay, mapViewPoint, 10, false);

                    identifyGraphics.addDoneListener(() -> Platform.runLater(this::distanceToPoint));

                };
            }
            catch (IOError ex){
                leftBox.getChildren().add(new Text(ex.toString()));
            }

        });

        Label FOV = new Label("Show Field Of View");
        FOVToggle = new ToggleButton("Show FOV");
        Label Polylines = new Label("Show Polylines");
        polylinesToggle = new ToggleButton("Show Polylines");
        Label Viewshed1 = new Label("Viewshed Visibility");
        visibilityToggle = new ToggleButton("visibilityToggle");
        frustumToggle = new ToggleButton("frustumToggle");

        // create a button to update the view
        cameraButton = new Button("Update camera");
        cameraButton.setOnMouseClicked(e -> updateCameraPosition());

        createPolylines = new Button("Create Polylines");
        createPolylines.setOnAction(
            new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent actionEvent) {
                    createPolylines();
                }
            }
        );

        leftBox.getChildren().addAll(Polylines, createPolylines, polylinesToggle, FOV, FOVToggle ,Viewshed1, visibilityToggle ,frustumToggle);
        centre.getChildren().addAll(sceneView, cameraButton);
        StackPane.setAlignment(cameraButton, Pos.TOP_LEFT);
//        StackPane.setAlignment(FOVToggle, Pos.TOP_LEFT);

        polylinesToggle.selectedProperty().addListener(e -> polygonLayer.setVisible(polylinesToggle.isSelected()) );
        polylinesToggle.textProperty().bind(Bindings.createStringBinding(() -> polylinesToggle.isSelected() ? "ON" : "OFF", polylinesToggle.selectedProperty()));

        FOVToggle.selectedProperty().addListener(e -> FOVToggle.setVisible(!FOVToggle.isSelected()) );
        FOVToggle.textProperty().bind(Bindings.createStringBinding(() -> FOVToggle.isSelected() ? "ON" : "OFF", FOVToggle.selectedProperty()));



        // set the user and camera

//        user = new Point(-4.50, 48.4, 50, SpatialReferences.getWgs84());

        // create a userViewshed from the camera
//        userViewshed = new LocationViewshed(user, headingSlider.getValue(), pitchSlider.getValue(),
//                horizontalAngleSlider.getValue(), verticalAngleSlider.getValue(), minDistanceSlider.getValue(),
//                maxDistanceSlider.getValue());
//        // set the colors of the visible and obstructed areas
//        Viewshed.setVisibleColor(0xCC00FF00);
//        Viewshed.setObstructedColor(0xCCFF0000);
//        // set the color and show the frustum outline
//        Viewshed.setFrustumOutlineColor(0xCC0000FF);
//        userViewshed.setFrustumOutlineVisible(true);

        // toggle visibility
//        visibilityToggle.selectedProperty().addListener(e -> userViewshed.setVisible(!visibilityToggle.isSelected()));
//        visibilityToggle.textProperty().bind(Bindings.createStringBinding(() -> visibilityToggle.isSelected() ? "OFF" : "ON", visibilityToggle.selectedProperty()));
//        frustumToggle.selectedProperty().addListener(e -> userViewshed.setFrustumOutlineVisible(!frustumToggle.isSelected()));
//        frustumToggle.textProperty().bind(Bindings.createStringBinding(() -> frustumToggle.isSelected() ? "OFF" : "ON", frustumToggle.selectedProperty()));
//        // heading slider
//        headingSlider.valueProperty().addListener(e -> userViewshed.setHeading(headingSlider.getValue()));
//        // pitch slider
//        pitchSlider.valueProperty().addListener(e -> userViewshed.setPitch(pitchSlider.getValue()));
//        // horizontal angle slider
//        horizontalAngleSlider.valueProperty().addListener(e -> userViewshed.setHorizontalAngle(horizontalAngleSlider.getValue()));
//        // vertical angle slider
//        verticalAngleSlider.valueProperty().addListener(e -> userViewshed.setVerticalAngle(verticalAngleSlider.getValue()));
//        // distance sliders
//        minDistanceSlider.valueProperty().addListener(e -> userViewshed.setMinDistance(minDistanceSlider.getValue()));
//        maxDistanceSlider.valueProperty().addListener(e -> userViewshed.setMaxDistance(maxDistanceSlider.getValue()));
//        // create an analysis overlay to add the userViewshed to the scene view

//        viewshedOverlay.getAnalyses().add(userViewshed);


        SplitPane mainSplit = new SplitPane();
        leftBox.getChildren().add( leftBox2);
        mainSplit.getItems().addAll(leftBox, centre );
        mainSplit.setDividerPosition(0,1/(double)12);
        mainSplit.setDividerPosition(2,11/(double)12);

        SplitPane root = new SplitPane();
        root.setOrientation(Orientation.VERTICAL);
        root.getItems().addAll(mainSplit);
        root.setDividerPosition(0,0.9);
        root.setPrefWidth(1300);
        root.setPrefHeight(750);
        //Start up scene and stage
        Scene scenes = new Scene(root);
        primaryStage.setScene(scenes);
        primaryStage.setMaximized(true);
        primaryStage.show();

        primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent t) {
                Platform.exit();
                System.exit(0);
            }
        });
    }

    public double distance(double lat1, double lon1, double lat2, double lon2) {
        if ((lat1 == lat2) && (lon1 == lon2)) {
            return 0;
        }
        else {
            double theta = lon1 - lon2;
            double dist = Math.sin(Math.toRadians(lat1)) * Math.sin(Math.toRadians(lat2)) + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) * Math.cos(Math.toRadians(theta));
            dist = Math.acos(dist);
            dist = Math.toDegrees(dist);
            dist = dist * 60 * 1.1515;
            dist = dist * 1.609344;
            return (dist);
        }
    }

    //function to clear all layers and list
    public void clearAll(){
        userOverlay.getGraphics().clear();
        pointsOverlay.getGraphics().clear();
        polygonLayer.getGraphics().clear();

        fovOverlay.getInternal().dispose();
        userViewshed.getInternal().dispose();

        pointVisualList.getItems().clear();

        sensorData.clear();

        targetList.clear();

        throwConfirmationAlert("Cleared All Layers");
    }

    // Returns all Targets
    public ArrayList<Point> getAllTargets(){
        ArrayList<Point> points = new ArrayList<>();
        for(Target target : targetList){
            points.add(target.getTarget());
        }
        return points;
    }

    //Finds target point for a given graphic
    public Point getGraphicPoint(Graphic graphic){
        for(Target target : targetList){
            Graphic targetGraphic = target.getGraphic();
            if (graphic.equals(targetGraphic)){
                return target.getTarget();
            }
        }
        return null;
    }
    // Returns the distance to a point from user
    private void distanceToPoint() {
        try {
            List<Graphic> graphics;
            graphics = identifyGraphics.get().getGraphics();
            if (!graphics.isEmpty()) {
                Point retrieved_point = getGraphicPoint(graphics.get(0));
                findDistanceFromSensor(retrieved_point);
            }
        } catch (InterruptedException interruptedException) {
            interruptedException.printStackTrace();
        } catch (ExecutionException executionException) {
            executionException.printStackTrace();
        }
    }

    // added graphic dialog, distance
    private void findDistanceFromSensor(Point second) {
        try {
                fovOverlay.getAnalyses().clear();
                lineOfSight(user, second);

                double distance = distance( user.getY(), user.getX(), second.getY(), second.getX());
                DecimalFormat df = new DecimalFormat("#.00");
                String angleFormated = df.format(distance);

                throwConfirmationAlert("Distance : " + angleFormated + "km");

        } catch (Exception e) {
            // on any error, display the stack trace
            e.printStackTrace();
            throwErrorAlert(e.getMessage());
        }
    }

    // Functions to draw and create polylines
    public void createPolylines(){
        drawPolylines(getAllTargets(), polygonLayer);
    }

    public void drawPolylines(ArrayList<Point> pointLists, GraphicsOverlay graphicsOverlay){
        int len = pointLists.size();
        for(int i=0;i<len - 1;i++) {
            addPolyline(pointLists.get(i), pointLists.get(i + 1), graphicsOverlay);
        }
    }

    //  Given a point it can add it to the map
    public void addTarget(Point targetPoint, String description){
        // create an opaque orange (0xFFFF5733) point symbol with a blue (0xFF0063FF) outline symbol
        SimpleMarkerSymbol simpleMarkerSymbol =
                new SimpleMarkerSymbol(SimpleMarkerSymbol.Style.CIRCLE, 0xFFFF5733, 10);
        SimpleLineSymbol blueOutlineSymbol =
                new SimpleLineSymbol(SimpleLineSymbol.Style.SOLID, 0xFF0063FF, 2);
        simpleMarkerSymbol.setOutline(blueOutlineSymbol);

        // create a graphic with the point zgeometry and symbol
        Graphic pointGraphic = new Graphic(targetPoint, simpleMarkerSymbol);

        //Create a Target Object
        Target target = new Target.TargetBuilder(targetPoint, pointGraphic)
                .description(description)
                .build();

        // Add target to targetList
        targetList.add(target);

        // add the point graphic to the graphics overlay
        pointsOverlay.getGraphics().add(pointGraphic);
        pointVisualList.getItems().add( new Text(target.toString()));

    }
    //Given two points it can add it to the map
    public void addPolyline(Point point1, Point point2, GraphicsOverlay graphicsOverlay){
        // create a point collection with a spatial reference, and add two points to it
        PointCollection polylinePoints = new PointCollection(SpatialReferences.getWgs84());
        polylinePoints.add(point1);
        polylinePoints.add(point2);

        // create a polyline geometry from the point collection
        Polyline polyline = new Polyline(polylinePoints);

        // create a blue line symbol for the polyline
        SimpleLineSymbol polylineSymbol =
                new SimpleLineSymbol(SimpleLineSymbol.Style.SOLID, 0xFF0063FF, 3);

        // create a polyline graphic with the polyline geometry and symbol
        Graphic polylineGraphic = new Graphic(polyline, polylineSymbol);

        // add the polyline graphic to the graphics overlay
        graphicsOverlay.getGraphics().add(polylineGraphic);
    }

    // Function to add lineofsight
    public void lineOfSight(Point point1, Point point2) {
        LocationLineOfSight lineOfSight = new LocationLineOfSight(point1, point2);
        fovOverlay.getAnalyses().add(lineOfSight);
    }

    // Moves the user and fov cone to a new location
    public void moveUser(Point newLocation) {

        userOverlay.getGraphics().clear();

        // create an opaque orange (0xFFFF5733) point symbol with a blue (0xFF0063FF) outline symbol
        SimpleMarkerSymbol userMarkerSymbol = new SimpleMarkerSymbol(SimpleMarkerSymbol.Style.CIRCLE, 0xFFFFFFFF, 10);
        SimpleLineSymbol blueOutlineSymbol = new SimpleLineSymbol(SimpleLineSymbol.Style.SOLID, 0xFF0063FF, 2);
        userMarkerSymbol.setOutline(blueOutlineSymbol);

        Graphic pointGraphic = new Graphic(newLocation, userMarkerSymbol);

        updateUserText();
        userOverlay.getGraphics().add(pointGraphic);
    }

    // function to return JSON object
    public Sensor convertJSON(String data){
        return JSON.parseObject(data,Sensor.class);
    }

    // Adds data through JSON
    public void readJSON(String data){
        Sensor sensor = convertJSON(data);
        if (!sensorData.contains(sensor)){
            sensorData.add(sensor);
        }
        // Add user and move user
        user = new Point(sensor.sensor_latitude, sensor.sensor_longitude, sensor.sensor_elevation, SpatialReferences.getWgs84());
        moveUser(user);
        updateUserText();

        //add point user is looking at
        Point target = new Point(sensor.target_latitude, sensor.target_longitude, sensor.target_altitude);

        addTarget(target, sensor.target_description);
        PointBuilder pointBuilder = new PointBuilder(user);
//        pointBuilder.setZ(sensor.sensor_elevation);

        if (userViewshed == null){
            addViewshed();
        }

        try {
            userViewshed.setLocation(pointBuilder.toGeometry());
            updateCameraPosition();

            //Bearing needs to be calculated from the north
            userViewshed.setHeading(360.0 - sensor.sensor_azimuth);
            userViewshed.setMaxDistance(sensor.target_range);
            userViewshed.setPitch(sensor.sensor_altitude);
        }catch (Exception e){
            System.out.println(e);
        }

    }

    public void addViewshed(){

        userViewshed = new LocationViewshed(user,10.0,10.0,80.0,60.0,2.0,10.0);
        // set the colors of the visible and obstructed areas
        Viewshed.setVisibleColor(0xCC00FF00);
        Viewshed.setObstructedColor(0xCCFF0000);
        // set the color and show the frustum outline
        Viewshed.setFrustumOutlineColor(0xCC0000FF);
        userViewshed.setFrustumOutlineVisible(true);

        viewshedOverlay.getAnalyses().add(userViewshed);
    }

    public void setInitialViewPoint(double longitude, double latitude){
        Point viewpoint = new Point(longitude, latitude);
        Camera camera = new Camera(viewpoint, 5000.0, 10.0, 60.0, 0.0);

        // set the map views's viewpoint centered on Waterloo and scaled
        sceneView.setViewpointCamera(camera);
    }

    // Updates the user location in text bar
    public void updateUserText(){
        double x = user.getX();
        double y = user.getY();
        userTextField.setText("x: " + x + " y: " + y + " z: " + user.getZ());
    }

    // Updates the default camera position to where the user is
    public void updateCameraPosition(){
        Camera camera = new Camera(user, 500.0, 10.0, 60.0, 0.0);
        sceneView.setViewpointCamera(camera);
    }

    // Read / Load And Write files
    public static void configureFileChooser(
        final FileChooser fileChooser) {
            fileChooser.setTitle("Load JSON");
            fileChooser.setInitialDirectory(
                    new File(System.getProperty("user.dir"))
        );
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("JSON", "*.JSON"),
                new FileChooser.ExtensionFilter("All Files", "*.*")

        );
    }
    // Gets working directory set at JSONLogs
    public String getLogDir(){
        String currentUserDir = System.getProperty("user.dir");
        return currentUserDir + File.separator + "JSONLogs";
    }
    // Gets filecount
    public int fileCount(String filename){
        File file = new File(filename);
        // Populates the array with names of files and directories
        return Objects.requireNonNull(file.list()).length;
    }
    //Lets user open file and input data throught JSONLogs
    public void openFile(File file) {
        try {
            logfile = file;
            readFile(file);
        } catch (IOException ex) {
            System.out.println(ex);
        }
    }
    //Read file functionality
    public void readFile(File file) throws FileNotFoundException {
        Scanner scanner = new Scanner(file);
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            // process the line
            if (line.length() > 0) {
                //do lots of stuff to sort the data into lists etc
                System.out.println(line);
                readJSON(line);
            }
        }
    }
    //Lets user create a new log file
    public void createFile(){
        int fileCount = fileCount(getLogDir()) + 1;
        try {
            String pathname = getLogDir() + File.separator +"log" + fileCount + ".json";
            File newFile = new File(pathname);
            if (newFile.createNewFile()) {
                logfile = newFile;
                throwConfirmationAlert("File created: " + newFile.getName());

            }
        } catch (IOException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }

    }
    // Appends data to a file.
    public void appendFile() throws IOException {
        FileWriter fr = new FileWriter(logfile, true);
        for (Sensor data: sensorData){
            String json = JSON.toJSONString(data);
            fr.write('\n' + json );
        }
        fr.close();
    }

    public void throwErrorAlert(String message){
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setContentText(message);
        a.show();
    }

    public void throwConfirmationAlert(String message){
        Alert a = new Alert(Alert.AlertType.CONFIRMATION);
        a.setContentText(message);
        a.show();
    }
    
    /**
     * Stops and releases all resources used in application.
     */
    @Override
    public void stop() {

        if (sceneView != null) {
            sceneView.dispose();
        }
    }
}


