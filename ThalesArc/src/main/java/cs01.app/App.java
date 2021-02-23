package cs01.app;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.ExecutionException;
import java.util.stream.Stream;

import com.esri.arcgisruntime.concurrent.ListenableFuture;
import com.esri.arcgisruntime.geoanalysis.LocationLineOfSight;
import com.esri.arcgisruntime.geometry.*;
import com.esri.arcgisruntime.mapping.view.Graphic;
import com.esri.arcgisruntime.mapping.view.GraphicsOverlay;
import com.esri.arcgisruntime.symbology.SimpleLineSymbol;
import com.esri.arcgisruntime.symbology.SimpleMarkerSymbol;
import com.esri.arcgisruntime.ArcGISRuntimeEnvironment;
import com.esri.arcgisruntime.geoanalysis.LocationViewshed;
import com.esri.arcgisruntime.layers.ArcGISSceneLayer;
import com.esri.arcgisruntime.mapping.view.AnalysisOverlay;
import com.esri.arcgisruntime.mapping.view.Camera;
import com.esri.arcgisruntime.mapping.view.SceneView;
import javafx.beans.binding.Bindings;
import javafx.event.EventHandler;
import javafx.geometry.Point2D;
import javafx.scene.control.Slider;
import javafx.scene.control.ToggleButton;

import com.esri.arcgisruntime.geoanalysis.Viewshed;
import com.esri.arcgisruntime.mapping.ArcGISScene;
import com.esri.arcgisruntime.mapping.ArcGISTiledElevationSource;
import com.esri.arcgisruntime.mapping.Basemap;
import com.esri.arcgisruntime.mapping.Surface;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Orientation;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public class App extends Application {
    private ArrayList<Point> pointLists = new ArrayList<Point>();
    Point user;

    private SceneView sceneView;

    public TitledPane bigLeftBox = new TitledPane();
    public VBox leftBox = new VBox(bigLeftBox);

    public TitledPane options = new TitledPane();
    public VBox leftBox1 = new VBox(options);

    public ListView pointVisualList = new ListView();
    public VBox leftBox2 = new VBox(pointVisualList);

    private ToggleButton FOVToggle;
    private ToggleButton polylinesToggle;
    private ToggleButton visibilityToggle;
    private ToggleButton frustumToggle;
    private Slider headingSlider;
    private Slider pitchSlider;
    private Slider horizontalAngleSlider;
    private Slider verticalAngleSlider;
    private Slider minDistanceSlider;
    private Slider maxDistanceSlider;

    public static void main(String[] args) {
        Application.launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
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

        // create a graphics overlay and add it to the map view for points and more
        GraphicsOverlay graphicsOverlay = new GraphicsOverlay();
        sceneView.getGraphicsOverlays().add(graphicsOverlay);
        // create a graphics overlay and add polylines to it and set to false
        GraphicsOverlay polygonLayer = new GraphicsOverlay();
        sceneView.getGraphicsOverlays().add(polygonLayer);
        polygonLayer.setVisible(false);

        // add a scene layer for buildings
        final String buildings = "https://tiles.arcgis.com/tiles/P3ePLMYs2RVChkJx/arcgis/rest/services/Buildings_Brest/SceneServer/layers/0";
        ArcGISSceneLayer sceneLayer = new ArcGISSceneLayer(buildings);
        scene.getOperationalLayers().add(sceneLayer);

        // Elevation Layer
        ArcGISTiledElevationSource elevationSource = new ArcGISTiledElevationSource("https://elevation3d.arcgis.com/arcgis/rest/services/WorldElevation3D/Terrain3D/ImageServer");
        Surface surface = new Surface();
        surface.getElevationSources().add(elevationSource);
        scene.setBaseSurface(surface);

        // create an analysis overlay for the userViewshed
        AnalysisOverlay viewshedOverlay = new AnalysisOverlay();
        sceneView.getAnalysisOverlays().add(viewshedOverlay);
        // create an analysis overlay for the line of sight
        AnalysisOverlay fovOverlay = new AnalysisOverlay();
        sceneView.getAnalysisOverlays().add(fovOverlay);
        fovOverlay.setVisible(false);

        // Sockets implementation
        //create a new thread
        new Thread(() -> {
            try {
                // Create a server socket
                ServerSocket serverSocket = new ServerSocket(userData.port);

                //append message of the Text Area of UI (GUI Thread)
                Platform.runLater(()
                        ->
                        leftBox.getChildren().add(new Text("New server started at " + new Date() + '\n')));

                //continous loop
                while (true) {
                    // Listen for a connection request, add new connection to the list
                    Socket socket = serverSocket.accept();
                    String fromClient;
                    BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));


                    fromClient = in.readLine();
                    System.out.println("received: " + fromClient);

                    double[] arr = Stream.of(fromClient.split(","))
                            .mapToDouble (Double::parseDouble)
                            .toArray();
                    //append message of the Text Area of UI (GUI Thread)
                    Platform.runLater(() -> {
                        Point sample = new Point(arr[0], arr[1],50);
                        pointLists.add(sample);
                        System.out.println(pointLists);
                        addPoint(sample, graphicsOverlay, pointVisualList);
                        drawPolylines(pointLists, polygonLayer);
                        showLineOfSight(user, pointLists, fovOverlay);
                    });
                }
            } catch (IOException ex) {
                leftBox.getChildren().add(new Text(ex.toString()));
            }
        }).start();

        Label FOV = new Label("Show Field Of View");
        FOVToggle = new ToggleButton("Show FOV");
        Label Polylines = new Label("Show Polylines");
        polylinesToggle = new ToggleButton("Show Polylines");
        Label Viewshed1 = new Label("Viewshed Visibility");
        visibilityToggle = new ToggleButton("visibilityToggle");
        frustumToggle = new ToggleButton("frustumToggle");

        Label headingLabel = new Label("Heading");
        headingSlider = new Slider();
        headingSlider.setMin(0);
        headingSlider.setMax(360);
        headingSlider.setValue(40);
        headingSlider.setShowTickLabels(true);
        headingSlider.setShowTickMarks(true);
        headingSlider.setMajorTickUnit(20);
        headingSlider.setMinorTickCount(5);
        headingSlider.setBlockIncrement(1);

        Label pitchLabel = new Label("Pitch Angle");
        pitchSlider = new Slider();
        pitchSlider.setMin(0);
        pitchSlider.setMax(180);
        pitchSlider.setValue(100);
        pitchSlider.setShowTickLabels(true);
        pitchSlider.setShowTickMarks(true);
        pitchSlider.setMajorTickUnit(50);
        pitchSlider.setMinorTickCount(5);
        pitchSlider.setBlockIncrement(10);

        Label horizontalLabel = new Label("Horizontal Angle");
        horizontalAngleSlider = new Slider();
        horizontalAngleSlider.setMin(0);
        horizontalAngleSlider.setMax(180);
        horizontalAngleSlider.setValue(50);
        horizontalAngleSlider.setShowTickLabels(true);
        horizontalAngleSlider.setShowTickMarks(true);
        horizontalAngleSlider.setMajorTickUnit(50);
        horizontalAngleSlider.setMinorTickCount(5);
        horizontalAngleSlider.setBlockIncrement(10);

        Label verticalAngle = new Label("Vertical Angle");
        verticalAngleSlider = new Slider();
        verticalAngleSlider.setMin(0);
        verticalAngleSlider.setMax(180);
        verticalAngleSlider.setValue(70);
        verticalAngleSlider.setShowTickLabels(true);
        verticalAngleSlider.setShowTickMarks(true);
        verticalAngleSlider.setMajorTickUnit(50);
        verticalAngleSlider.setMinorTickCount(5);
        verticalAngleSlider.setBlockIncrement(10);

        Label minDistance = new Label("Minimum Distance ");
        minDistanceSlider = new Slider();
        minDistanceSlider.setMin(0);
        minDistanceSlider.setMax(100);
        minDistanceSlider.setValue(05);
        minDistanceSlider.setShowTickLabels(true);
        minDistanceSlider.setShowTickMarks(true);
        minDistanceSlider.setMajorTickUnit(50);
        minDistanceSlider.setMinorTickCount(5);
        minDistanceSlider.setBlockIncrement(10);

        Label maxDistance = new Label("Max Distance ");
        maxDistanceSlider = new Slider();
        maxDistanceSlider.setMin(0);
        maxDistanceSlider.setMax(1000);
        maxDistanceSlider.setValue(80);
        maxDistanceSlider.setShowTickLabels(true);
        maxDistanceSlider.setShowTickMarks(true);
        maxDistanceSlider.setMajorTickUnit(50);
        maxDistanceSlider.setMinorTickCount(5);
        maxDistanceSlider.setBlockIncrement(10);

        leftBox1.getChildren().addAll(Polylines,polylinesToggle, FOV, FOVToggle ,Viewshed1, visibilityToggle ,frustumToggle, headingLabel, headingSlider, pitchLabel, pitchSlider, horizontalLabel, horizontalAngleSlider, verticalAngle, verticalAngleSlider, minDistance, minDistanceSlider, maxDistance, maxDistanceSlider);

        centre.getChildren().addAll(sceneView);
//        StackPane.setAlignment(polylinesButton, Pos.TOP_LEFT);
//        StackPane.setAlignment(FOVToggle, Pos.TOP_LEFT);

        polylinesToggle.selectedProperty().addListener(e -> polygonLayer.setVisible(polylinesToggle.isSelected()) );
        polylinesToggle.textProperty().bind(Bindings.createStringBinding(() -> polylinesToggle.isSelected() ? "ON" : "OFF", polylinesToggle.selectedProperty()));

        FOVToggle.selectedProperty().addListener(e -> polygonLayer.setVisible(FOVToggle.isSelected()) );
        FOVToggle.textProperty().bind(Bindings.createStringBinding(() -> FOVToggle.isSelected() ? "ON" : "OFF", FOVToggle.selectedProperty()));

        // set the user and camera
        user = new Point(-4.50, 48.4, 50, SpatialReferences.getWgs84());
        Camera camera = new Camera(user, 500.0, 10.0, 80.0, 0.0);
        sceneView.setViewpointCamera(camera);
        // Add User Point
        SimpleMarkerSymbol userMarkerSymbol = new SimpleMarkerSymbol(SimpleMarkerSymbol.Style.CIRCLE, 0xFFFFFFFF, 10);
        SimpleLineSymbol blueOutlineSymbol = new SimpleLineSymbol(SimpleLineSymbol.Style.SOLID, 0xFF0063FF, 2);
        userMarkerSymbol.setOutline(blueOutlineSymbol);
        // create a graphic with the point geometry and symbol
        Graphic pointGraphic = new Graphic(user, userMarkerSymbol);
        // add the point graphic to the graphics overlay
        graphicsOverlay.getGraphics().add(pointGraphic);

        // create a userViewshed from the camera
        LocationViewshed userViewshed = new LocationViewshed(user, headingSlider.getValue(), pitchSlider.getValue(),
                horizontalAngleSlider.getValue(), verticalAngleSlider.getValue(), minDistanceSlider.getValue(),
                maxDistanceSlider.getValue());
        // set the colors of the visible and obstructed areas
        Viewshed.setVisibleColor(0xCC00FF00);
        Viewshed.setObstructedColor(0xCCFF0000);
        // set the color and show the frustum outline
        Viewshed.setFrustumOutlineColor(0xCC0000FF);
        userViewshed.setFrustumOutlineVisible(true);

        // create a listener to update the viewshed location when the mouse moves
        EventHandler<MouseEvent> mouseMoveEventHandler = new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                var point2D = new Point2D(event.getX(), event.getY());
                ListenableFuture<Point> pointFuture = sceneView.screenToLocationAsync(point2D);
                pointFuture.addDoneListener(() -> {
                    try {
                        Point point = pointFuture.get();
                        PointBuilder pointBuilder = new PointBuilder(point);
                        pointBuilder.setZ(point.getZ());
                        userViewshed.setLocation(pointBuilder.toGeometry());
                        // add listener back
                        sceneView.setOnMouseMoved(this);
                    } catch (InterruptedException | ExecutionException e) {
                        e.printStackTrace();
                    }
                });
                // disable listener until location is updated (for performance)
                sceneView.setOnMouseMoved(null);
            }
        };
        // remove the default listener for mouse move events
        sceneView.setOnMouseMoved(null);

        // click to start/stop moving viewshed with mouse
        sceneView.setOnMouseClicked(event -> {
            if (event.isStillSincePress() && event.getButton() == MouseButton.PRIMARY) {
                if (sceneView.getOnMouseMoved() == null) {
                    sceneView.setOnMouseMoved(mouseMoveEventHandler);
                } else {
                    sceneView.setOnMouseMoved(null);
                }
            }
        });

        // toggle visibility
        visibilityToggle.selectedProperty().addListener(e -> userViewshed.setVisible(!visibilityToggle.isSelected()));
        visibilityToggle.textProperty().bind(Bindings.createStringBinding(() -> visibilityToggle.isSelected() ? "OFF" : "ON", visibilityToggle.selectedProperty()));
        frustumToggle.selectedProperty().addListener(e -> userViewshed.setFrustumOutlineVisible(!frustumToggle.isSelected()));
        frustumToggle.textProperty().bind(Bindings.createStringBinding(() -> frustumToggle.isSelected() ? "OFF" : "ON", frustumToggle.selectedProperty()));
        // heading slider
        headingSlider.valueProperty().addListener(e -> userViewshed.setHeading(headingSlider.getValue()));
        // pitch slider
        pitchSlider.valueProperty().addListener(e -> userViewshed.setPitch(pitchSlider.getValue()));
        // horizontal angle slider
        horizontalAngleSlider.valueProperty().addListener(e -> userViewshed.setHorizontalAngle(horizontalAngleSlider.getValue()));
        // vertical angle slider
        verticalAngleSlider.valueProperty().addListener(e -> userViewshed.setVerticalAngle(verticalAngleSlider.getValue()));
        // distance sliders
        minDistanceSlider.valueProperty().addListener(e -> userViewshed.setMinDistance(minDistanceSlider.getValue()));
        maxDistanceSlider.valueProperty().addListener(e -> userViewshed.setMaxDistance(maxDistanceSlider.getValue()));
        // create an analysis overlay to add the userViewshed to the scene view

        viewshedOverlay.getAnalyses().add(userViewshed);


        SplitPane mainSplit = new SplitPane();
//        leftBox.getChildren().add(leftBox1);
        leftBox.getChildren().add( leftBox1);
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
    }

    public void showLineOfSight(Point user, ArrayList<Point> pointLists, AnalysisOverlay viewshedOverlay){
        int len = pointLists.size();
        for (Point list : pointLists) {
            lineOfSight(user, list, viewshedOverlay);
        }
    }

    public void drawPolylines(ArrayList<Point> pointLists, GraphicsOverlay graphicsOverlay){
        int len = pointLists.size();
        for(int i=0;i<len - 1;i++) {
            addPolyline(pointLists.get(i), pointLists.get(i + 1), graphicsOverlay);
        }
    }

    //  Given a point it can add it to the map
    public void addPoint(Point point, GraphicsOverlay graphicsOverlay, ListView list){
        // create an opaque orange (0xFFFF5733) point symbol with a blue (0xFF0063FF) outline symbol
        SimpleMarkerSymbol simpleMarkerSymbol =
                new SimpleMarkerSymbol(SimpleMarkerSymbol.Style.CIRCLE, 0xFFFF5733, 10);
        SimpleLineSymbol blueOutlineSymbol =
                new SimpleLineSymbol(SimpleLineSymbol.Style.SOLID, 0xFF0063FF, 2);

        simpleMarkerSymbol.setOutline(blueOutlineSymbol);
        // create a graphic with the point zgeometry and symbol
        Graphic pointGraphic = new Graphic(point, simpleMarkerSymbol);

        // add the point graphic to the graphics overlay
        graphicsOverlay.getGraphics().add(pointGraphic);
        list.getItems().add( new Text("Point located at: " +  point.toString()));

    }
    //Given two points it can add it to the map
    public void addPolyline(Point point1, Point point2, GraphicsOverlay graphicsOverlay){
        // create a point collection with a spatial reference, and add three points to it
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
    //        Function to add lineofsight
    public void lineOfSight(Point point1, Point point2, AnalysisOverlay viewshedOverlay) {
        LocationLineOfSight lineOfSight = new LocationLineOfSight(point1, point2);
        viewshedOverlay.getAnalyses().add(lineOfSight);
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
