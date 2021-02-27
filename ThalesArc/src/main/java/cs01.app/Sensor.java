package cs01.app;

public class Sensor {

    public double sensor_latitude;
    public double sensor_longitude;
    public double sensor_altitude;
    public double sensor_azimuth;
    public double sensor_elevation;

    public double target_latitude;
    public double target_longitude;
    public double target_altitude;
    public double target_range;


    public Sensor(Double sensor_latitude, Double sensor_longitude, Double sensor_altitude, Double sensor_azimuth,
                  Double sensor_elevation, Double target_latitude, Double target_longitude, Double target_altitude,
                  Double target_range){

        this.sensor_latitude = sensor_latitude;
        this.sensor_longitude = sensor_longitude;
        this.sensor_altitude = sensor_altitude;
        this.sensor_azimuth = sensor_azimuth;
        this.sensor_elevation = sensor_elevation;
        this.target_latitude = target_latitude;
        this.target_longitude = target_longitude;
        this.target_altitude = target_altitude;
        this.target_range = target_range;
    }
}
