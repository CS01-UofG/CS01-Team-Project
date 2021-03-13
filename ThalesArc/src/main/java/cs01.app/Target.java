package cs01.app;
import com.esri.arcgisruntime.geometry.Point;
import com.esri.arcgisruntime.mapping.view.Graphic;

public class Target {
    // All Final Attributes
    private final Point target;
    private final Graphic graphic;
    private final String description;

    public Target(TargetBuilder builder){
        this.target = builder.target;
        this.graphic = builder.graphic;
        this.description = builder.description;
    }

    // Getters
    public Point getTarget(){
        return target;
    }

    public Graphic getGraphic(){
        return graphic;
    }

    public String getDescription(){
        return description;
    }

    @Override
    public String toString(){
        return "Target : " + target.getX() + ", " + target.getY() + ", " + target.getZ() + " Description" + description;
    }

    public static class  TargetBuilder{
        private final Point target;
        private final Graphic graphic;
        private String description;

        public TargetBuilder(Point target, Graphic graphic){
            this.target = target;
            this.graphic = graphic;
        }

        public TargetBuilder description(String description){
            this.description = description;
            return this;
        }
        // Return constructed object
        public Target build(){
            return new Target(this);
        }
    }



}
