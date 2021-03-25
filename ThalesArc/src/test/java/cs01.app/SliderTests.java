package Tests;

import com.sun.javafx.application.PlatformImpl;
import cs01.ComponentFactory;
import javafx.scene.control.Slider;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.*;


public class SliderTests {
	private ComponentFactory componentFactory = new ComponentFactory();
	
    @BeforeClass
    public static void setup() {
    	// we need this in order to use fx components
    	PlatformImpl.startup(() -> {});
    }
    
	@Test
	public void shouldNotBeNull() {
		var slider = componentFactory.createSlider(0, 360, 40, 20, 5, 1);

		assertNotNull(slider);
	}
	
	@Test
	public void shouldBeCreatedCorrectly() {
		var slider = componentFactory.createSlider(0, 360, 40, 20, 5, 1);
		
		Slider correctSlider = new Slider();
		correctSlider.setMin(0);
		correctSlider.setMax(360);
		correctSlider.setValue(40);
		correctSlider.setMajorTickUnit(20);
		correctSlider.setMinorTickCount(5);
		correctSlider.setBlockIncrement(1);

		assertEquals(slider.getMin(), correctSlider.getMin(), 0.0);
		assertEquals(slider.getMax(), correctSlider.getMax(), 0.0);
		assertEquals(slider.getValue(), correctSlider.getValue(), 0.0);
		assertEquals(slider.getMajorTickUnit(), correctSlider.getMajorTickUnit(), 0.0);
		assertEquals(slider.getMinorTickCount(), correctSlider.getMinorTickCount());
		assertEquals(slider.getBlockIncrement(), correctSlider.getBlockIncrement(), 0.0);
	}
}
