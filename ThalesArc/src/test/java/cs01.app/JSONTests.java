package cs01.app;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class JSONTests {

    public String data = "{\"id\": 29, \"sensor_latitude\": -4.4892047, " +
            "\"sensor_longitude\": 48.397533, \"sensor_altitude\": 90," +
            " \"sensor_azimuth\": -58.511074478830814, \"sensor_elevation\": 74, " +
            "\"target_latitude\": -4.4895891778112125, \"target_longitude\": 48.39802252713922, " +
            "\"target_altitude\": 109, \"target_range\": 100, \"target_description\": \"\"}";
   // public Sensor sensorObj = App.convertJSON(data);

    @Test
    public void shouldNotBeNull() {
    	Sensor sensorObj = App.convertJSON(data);
        assertEquals(-4.4892047, sensorObj.sensor_latitude, 0);
        assertEquals(29, sensorObj.id,0);
        assertEquals(100, sensorObj.target_range, 0);
    }
}
