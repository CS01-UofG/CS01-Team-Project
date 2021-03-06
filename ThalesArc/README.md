# ThalesArc
This application hosts the 3D concept and works in accordance with the [ThalesArcSimulationScript](https://stgit.dcs.gla.ac.uk/tp3-2020-CS01/cs01-main/-/tree/master/ThalesArcSimulationScript) which also replaces the previously maintained CesiumJS based application contained within ElectronJS.. It is based on the JavaFX API which enables us to cross-platform desktop applications with the added support of ArcGIS Java SDK which powers the map provided in the application. 

## Sample Logs 
Sample JSON logs can be opened up from the JSONLogs directory to test the application.
 

## Instructions

### IntelliJ IDEA

1. Open IntelliJ IDEA and select _File > Open..._.
2. Choose the ThalesArc directory and click _OK_.
3. Select _File > Project Structure..._ and ensure that the Project SDK and language level are set to use Java 11.
4. Open the Maven view with _View > Tool Windows > Maven_.
5. In the Maven view, under _Plugins > dependency_, double-click the `dependency:unpack` goal. This will unpack the native libraries into $USER_HOME/.arcgis.
6. In the Maven view, run the `compile` phase under _Lifecycle_ and then the `exec:java` goal to run the app.

### Eclipse

1. Open Eclipse and select _File > Import_.
2. In the import wizard, choose _Maven > Existing Maven Projects_, then click _Next_.
3. Select the ThalesArc as the project root directory.
4. Click _Finish_ to complete the import.
5. Select _Project > Properties_ . In _Java Build Path_, ensure that under the Libraries tab, _Modulepath_ is set to JRE System Library (JavaSE-11). In _Java Compiler_, ensure that the _Use compliance from execution environment 'JavaSE-11' on the 'Java Build Path'_ checkbox is selected.
6. Right-click the project in the Project Explorer or Package Explorer and choose _Run As > Maven Build..._. In the _Edit Configuration_ dialog, create a new configuration with name `unpack`. In the _Goals_ field, enter `dependency:unpack`. Click _Run_ to run the goal. This will unpack the native libraries into $USER_HOME/.arcgis.
7. Again, create a new run configuration with name `run`. In the _Goals_ field, enter `compile exec:java`. Click _Run_ to run the goal. The app should compile and launch the JavaFX window.

### Command Line - Linux 
1. Make sure Maven and Java has been installed previously and in path
2. Run `mvn dependency:unpack`
3. Run `mvn compile exec:java` 
