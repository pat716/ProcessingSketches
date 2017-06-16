package datatypes.shape;

import datatypes.geom.Vector;
import display.DrawOptions;
import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PGraphics;

import static processing.core.PConstants.PI;

/**
 * Created by psweeney on 6/12/17.
 */
public class Circle extends Shape {
    private float x, y, radius;

    public Circle(float x, float y, float radius, DrawOptions drawOptions){
        super(drawOptions);
        this.x = x;
        this.y = y;
        this.radius = radius;
    }

    @Override
    public Vector getCenterPoint() {
        return new Vector(x, y);
    }

    @Override
    protected void render(PGraphics canvas, float xOffset, float yOffset) {
        canvas.ellipseMode(PConstants.CENTER);
        canvas.ellipse(x + xOffset, y + yOffset, radius, radius);
    }

    @Override
    public Shape clone(DrawOptions cloneDrawOptions) {
        if(cloneDrawOptions == null)
            return new Circle(x, y, radius, getDrawOptions());
        return new Circle(x, y, radius, cloneDrawOptions);
    }

    @Override
    public Shape clone() {
        return clone(null);
    }

    @Override
    public Shape combine(Shape other, float otherRatio) {
        DrawOptions drawOptions = getDrawOptions().combine(other.getDrawOptions(), otherRatio);
        if(!(other instanceof Circle)){

            if(otherRatio <= 0.5f){
                return clone(drawOptions);
            }
            return other.clone(drawOptions);
        }

        Circle circle = (Circle) other;
        float newX = (1 - otherRatio) * x + otherRatio * circle.x;
        float newY = (1 - otherRatio) * y + otherRatio * circle.y;
        float newWidth = (1 - otherRatio) * radius + otherRatio * circle.radius;

        return new Circle(newX, newY, radius, drawOptions);
    }
}
