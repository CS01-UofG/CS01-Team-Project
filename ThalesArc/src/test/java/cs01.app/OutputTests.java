package cs01.app;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class OutputTests {
    String producedOutput = App.getUserText(1.0, 2.0, 3.0);
    String correctOutput = "x: " + 1.0 + " y: " + 2.0 + " z: " + 3.0;

	@Test
    public void shouldProduceCorrectUserText() {

        assertEquals(producedOutput, correctOutput);
    }
}
