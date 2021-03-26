package cs01.app;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class DistanceTests {

    @Test
    public void shouldReturnZero() {
        assertEquals(App.distance(25.299819047685062, 55.37830597919699, 25.299819047685062, 55.37830597919699),0, 0);
    }
    
    @Test
    public  void shouldProduceCorrectDistance() {
    	double producedOutput = App.distance(25.299819047685062, 55.37830597919699, 27.299819047685062, 57.37830597919699);
    	double correctOutput = 298.64559893547863;
        assertEquals("", correctOutput, producedOutput, 0.01);
    }
}
