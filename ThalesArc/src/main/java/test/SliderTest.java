package test;

import cs01.ComponentFactory;
import javafx.scene.control.Slider;

import static org.junit.Assert.*;

import org.junit.BeforeClass;
import org.junit.Test;

import com.sun.javafx.application.PlatformImpl;


public class SliderTest  {
	private ComponentFactory componentFactory = new ComponentFactory();
	
    @BeforeClass
    public static void setup() {
    	// we need this in order to use fx components
    	PlatformImpl.startup(() -> {});
    }
    
	@Test
	public void shouldNotBeNull() {
		var slider = componentFactory.createSlider(0, 360, 40, 20, 5, 1);
		
		assertFalse(slider == null);
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
        
		assertTrue(slider.getMin() == correctSlider.getMin());
		assertTrue(slider.getMax() == correctSlider.getMax());
		assertTrue(slider.getValue() == correctSlider.getValue());
		assertTrue(slider.getMajorTickUnit() == correctSlider.getMajorTickUnit());
		assertTrue(slider.getMinorTickCount() == correctSlider.getMinorTickCount());
		assertTrue(slider.getBlockIncrement() == correctSlider.getBlockIncrement());
	}
}
