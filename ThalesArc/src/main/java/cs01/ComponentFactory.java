package cs01;

import javafx.scene.control.Slider;

public class ComponentFactory {
    public Slider createSlider(double min, double max, double value, double majorTickUnit, int minorTickCount, double blockIncrement){
        Slider slider = new Slider();
        slider.setMin(min);
        slider.setMax(max);
        slider.setValue(value);
        slider.setShowTickLabels(true);
        slider.setShowTickMarks(true);
        slider.setMajorTickUnit(majorTickUnit);
        slider.setMinorTickCount(minorTickCount);
        slider.setBlockIncrement(blockIncrement);
        
        return slider;
    }
}
