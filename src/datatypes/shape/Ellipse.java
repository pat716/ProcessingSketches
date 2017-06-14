package datatypes.shape;

import datatypes.geom.Vector;
import display.DrawOptions;
import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PGraphics;

/**
 * Created by psweeney on 6/12/17.
 */
public class Ellipse extends Shape {
    private float x, y, width, height, rotation;

    public Ellipse(float x, float y, float width, float height, float rotation, DrawOptions drawOptions){
        super(drawOptions);
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.rotation = rotation;
    }

    public Ellipse(float x, float y, float width, float height, DrawOptions drawOptions){
        this(x, y, width, height, 0, drawOptions);
    }

    public Ellipse(float x, float y, float radius, DrawOptions drawOptions){
        this(x, y, radius, radius, drawOptions);
    }

    @Override
    public Vector getCenterPoint() {
        return new Vector(x, y);
    }

    @Override
    protected void render(PGraphics canvas, float xOffset, float yOffset) {
        canvas.ellipseMode(PConstants.CENTER);

        if(rotation != 0){
            canvas.pushMatrix();
            canvas.translate(x + xOffset, y + yOffset);
            canvas.rotate(rotation);
            canvas.ellipse(0, 0, width, height);
            canvas.popMatrix();
        } else {
            canvas.ellipse(x + xOffset, y + yOffset, width, height);
        }
    }

    @Override
    public Shape clone(DrawOptions cloneDrawOptions) {
        if(cloneDrawOptions == null)
            return new Ellipse(x, y, width, rotation, getDrawOptions());
        return new Ellipse(x, y, width, rotation, cloneDrawOptions);
    }

    @Override
    public Shape clone() {
        return clone(null);
    }

    @Override
    public Shape combine(Shape other, float otherRatio) {
        DrawOptions drawOptions = getDrawOptions().combine(other.getDrawOptions(), otherRatio);
        if(!(other instanceof Ellipse)){

            if(otherRatio <= 0.5f){
                return clone(drawOptions);
            }
            return other.clone(drawOptions);
        }

        Ellipse ellipse = (Ellipse) other;
        float newX = (1 - otherRatio) * x + otherRatio * ellipse.x;
        float newY = (1 - otherRatio) * y + otherRatio * ellipse.y;
        float newWidth = (1 - otherRatio) * width + otherRatio * ellipse.width;
        float newHeight = (1 - otherRatio) * height + otherRatio * ellipse.height;
        float newRotation = (1 - otherRatio) * rotation + otherRatio * ellipse.rotation;

        return new Ellipse(newX, newY, width, height, rotation, drawOptions);
    }
}
