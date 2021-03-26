package cs01.app;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class OutputTests {
    @Test
    public void shouldNotBeNull() {
    	String producedOutput = cs01.app.App.getUserText(1.0, 2.0, 3.0);
    	String correctOutput = "x: " + 1.0 + " y: " + 2.0 + " z: " + 3.0;
        assertEquals(producedOutput, correctOutput);
    }
}
