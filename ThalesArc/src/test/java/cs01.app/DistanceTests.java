package cs01.app;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class DistanceTests {

    @Test
    public void shouldNotBeNull() {
        assertEquals(cs01.app.App.distance(25.29949410502984, 55.37761396928376, 25.301133359089594, 55.380950637237476),0.38, 1);
        assertEquals(cs01.app.App.distance(25.299819047685062, 55.37830597919699, 25.299819047685062, 55.37830597919699),0, 0);
    }


}
