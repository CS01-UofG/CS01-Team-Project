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

//        // Elevation Layer Hidden as it's an optional feature for customer to decided upon demonstations
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
        cameraButton.setOnMouseClicked(e -> updateCameraPosition(500.0, 10.0, 60.0, 0.0));

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

        polylinesToggle.selectedProperty().addListener(e -> polygonLayer.setVisible(polylinesToggle.isSelected()) );
        polylinesToggle.textProperty().bind(Bindings.createStringBinding(() -> polylinesToggle.isSelected() ? "ON" : "OFF", polylinesToggle.selectedProperty()));

        FOVToggle.selectedProperty().addListener(e -> FOVToggle.setVisible(!FOVToggle.isSelected()) );
        FOVToggle.textProperty().bind(Bindings.createStringBinding(() -> FOVToggle.isSelected() ? "ON" : "OFF", FOVToggle.selectedProperty()));

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

    /**
     * Returns the distance from point 1 to point 2
     *
     * @param lat1
     * @param lon1
     * @param lat2
     * @param lon2
     * @return
     */
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

    /**
     * Clears all overlays on the map and throws a confirmations
     */
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

    /**
     * Returns all the targets from Target class
     * @return points
     */
    public ArrayList<Point> getAllTargets(){
        ArrayList<Point> points = new ArrayList<>();
        for(Target target : targetList){
            points.add(target.getTarget());
        }
        return points;
    }

    /**
     * Returns the target for a graphic from the targetList
     *
     * @param graphic
     * @return null if no point found, else targets graphic point
     */
    public Point getGraphicPoint(Graphic graphic){
        for(Target target : targetList){
            Graphic targetGraphic = target.getGraphic();
            if (graphic.equals(targetGraphic)){
                return target.getTarget();
            }
        }
        return null;
    }

    /**
     * Generates a distance from users location
     */
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

    /**
     * Throws a confirmation of the distance from users location and a point
     *
     * @param second
     */
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

    /**
     * Calls the draw polylines function used by a button
     */
    public void createPolylines(){
        drawPolylines(getAllTargets(), polygonLayer);
    }

    /**
     * Draws polylines between each point on the pointsList
     *
     * @param pointLists
     * @param graphicsOverlay
     */
    public void drawPolylines(ArrayList<Point> pointLists, GraphicsOverlay graphicsOverlay){
        int len = pointLists.size();
        for(int i=0;i<len - 1;i++) {
            addPolyline(pointLists.get(i), pointLists.get(i + 1), graphicsOverlay);
        }
    }
    /**
     * Draws a polyline between two locations on the map
     *
     * @param point1 start
     * @param point2 destination
     * @param graphicsOverlay
     */
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

    /**
     * Adds a line of sight between two points
     *
     * @param point1 Start
     * @param point2 Destination
     */
    public void lineOfSight(Point point1, Point point2) {
        LocationLineOfSight lineOfSight = new LocationLineOfSight(point1, point2);
        fovOverlay.getAnalyses().add(lineOfSight);
    }

    /**
     * Moves the user to desired location and clears previous location marker.
     *
     * @param newLocation
     */
    public void moveUser(Point newLocation) {

        userOverlay.getGraphics().clear();
        // create an white (0xFFFF5733) point symbol with a blue (0xFF0063FF) outline symbol
        SimpleMarkerSymbol userMarkerSymbol = new SimpleMarkerSymbol(SimpleMarkerSymbol.Style.CIRCLE, 0xFFFFFFFF, 10);
        SimpleLineSymbol blueOutlineSymbol = new SimpleLineSymbol(SimpleLineSymbol.Style.SOLID, 0xFF0063FF, 2);
        userMarkerSymbol.setOutline(blueOutlineSymbol);

        Graphic pointGraphic = new Graphic(newLocation, userMarkerSymbol);
        userOverlay.getGraphics().add(pointGraphic);
    }

    /**
     * Returns a Sensor object which is parsed through the fastJSON library
     *
     * @param data string
     * @return JSON object
     */
    public Sensor convertJSON(String data){
        return JSON.parseObject(data,Sensor.class);
    }

    /**
     * Adds a target onto the map and list, and builds a Target
     * @param targetPoint
     * @param description
     */
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

    /**
     * Reads a data string, which contains information about the sensor and target locations.
     * This then adds the user location as well as target point of interest.
     *
     * The viewshed is updated as requested for and updates the camera location.
     *
     * @param data
     */
    public void readJSON(String data){
        Sensor sensor = convertJSON(data);
        if (!sensorData.contains(sensor)){
            sensorData.add(sensor);
        }
        // Updates users point and moves user
        user = new Point(sensor.sensor_latitude, sensor.sensor_longitude, sensor.sensor_elevation, SpatialReferences.getWgs84());
        moveUser(user);
        updateUserText();
        // Creates a new target Point and adds it to the map
        Point target = new Point(sensor.target_latitude, sensor.target_longitude, sensor.target_altitude);
        addTarget(target, sensor.target_description);
        PointBuilder pointBuilder = new PointBuilder(user);
        // pointBuilder.setZ(sensor.sensor_elevation);

        if (userViewshed == null){
            addViewshed();
        }

        try {
            userViewshed.setLocation(pointBuilder.toGeometry());
            updateCameraPosition(500.0, 10.0, 60.0, 0.0);

            //Bearing needs to be calculated from the north
            userViewshed.setHeading(360.0 - sensor.sensor_azimuth);
            userViewshed.setMaxDistance(sensor.target_range);
            userViewshed.setPitch(sensor.sensor_altitude);
        } catch (Exception e){
            System.out.println(e);
        }

    }

    /**
     * Imports a predefined viewshed to the analysis layer
      */
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

    /**
     * Sets an initial view point at the defined locations
     */
    public void setInitialViewPoint(double longitude, double latitude){
        Point viewpoint = new Point(longitude, latitude);
        Camera camera = new Camera(viewpoint, 5000.0, 10.0, 60.0, 0.0);

        // set the map views's viewpoint centered on Waterloo and scaled
        sceneView.setViewpointCamera(camera);
    }

    /**
     * Updates user location at the text bar
     */
    public void updateUserText(){
        userTextField.setText("x: " + user.getX() + " y: " +user.getY() + " z: " + user.getZ());
    }

    /**
     * Updates the camera location at a predefined location
     *
     * @param distance distance from camera to user
     * @param heading heading angle
     * @param pitch pitch angle
     * @param roll angle
     */
    public void updateCameraPosition(double distance, double heading, double pitch, double roll){
        Camera camera = new Camera(user, distance, heading, pitch, roll);
        sceneView.setViewpointCamera(camera);
    }

    /**
     * Opens the operating system file chooser
     */
    public static void configureFileChooser( final FileChooser fileChooser) {
            fileChooser.setTitle("Load JSON");
            fileChooser.setInitialDirectory(
                    new File(getUserDirectory())
        );
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("JSON", "*.JSON"),
                new FileChooser.ExtensionFilter("All Files", "*.*")

        );
    }

    /**
     * Returns user directory
     */
    public static String getUserDirectory(){
        return  System.getProperty("user.dir");
    }

    /**
     * Gets the log directory for the project
     */
    public String getLogDir(){
        String currentUserDir = getUserDirectory();
        return currentUserDir + File.separator + "JSONLogs";
    }

    /**
     * Find the number of files in a directory
     *
     * @param directoryName name of the directory
     */
    public int fileCount(String directoryName){
        File directory = new File(directoryName);
        return Objects.requireNonNull(directory.list()).length;
    }

    /**
     * Promts the user to read a file.
     *
     * @param file selected file from the user
     */
    public void openFile(File file) {
        try {
            logfile = file;
            readFile(file);
        } catch (IOException ex) {
            System.out.println(ex);
        }
    }

    /**
     * Reads selected file and adds it to the map
     *
     * @param file selected file from the user
     */
    public void readFile(File file) throws FileNotFoundException {
        Scanner scanner = new Scanner(file);
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            if (line.length() > 0) {
                readJSON(line);
            }
        }
    }

    /**
     * Creates a file for the user and throws a confirmation, else shows the error.
     */
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
            throwErrorAlert("An error occurred.");
            e.printStackTrace();
        }
    }

    /**
     * Appends sensor data to the file
     */
    public void appendFile() throws IOException {
        FileWriter fr = new FileWriter(logfile, true);
        for (Sensor data: sensorData){
            String json = JSON.toJSONString(data);
            fr.write('\n' + json );
        }
        fr.close();
    }

    /**
     * Throws an error alert
     *
     * @param message Message user wants to show
     */
    public void throwErrorAlert(String message){
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setContentText(message);
        a.show();
    }
    /**
     * Throws an confirmation alert
     *
     * @param message Message user wants to show
     */
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


