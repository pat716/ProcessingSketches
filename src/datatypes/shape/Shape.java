package datatypes.shape;

import datatypes.geom.Vector;
import display.Color;
import display.DrawOptions;
import processing.core.PGraphics;

/**
 * Created by psweeney on 6/12/17.
 */
public abstract class Shape {
    private DrawOptions drawOptions;

    public Shape(DrawOptions drawOptions){
        this.drawOptions = drawOptions;
    }

    public void setDrawOptions(DrawOptions drawOptions) {
        this.drawOptions = drawOptions;
    }

    public DrawOptions getDrawOptions() {
        return drawOptions;
    }

    protected abstract void render(PGraphics canvas, float xOffset, float yOffset);

    protected void render(PGraphics canvas){
        render(canvas, 0, 0);
    }

    public void drawShape(PGraphics canvas, Color.ColorMode colorMode, float xOffset, float yOffset){
        drawOptions.applyToPGraphics(canvas, colorMode);
        render(canvas, xOffset, yOffset);
    }

    public abstract float getStrokeWeightForSimpleRendering();
    public abstract Vector getCenterPoint();
    public abstract Shape clone(DrawOptions drawOptions);
    public abstract Shape clone();
    public abstract Shape combine(Shape other, float otherRatio);

    public void drawShape(PGraphics canvas, Color.ColorMode colorMode){
        drawShape(canvas, colorMode, 0, 0);
    }
}
