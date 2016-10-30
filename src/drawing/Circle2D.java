package drawing;

import processing.core.PApplet;
import processing.core.PConstants;

/**
 * Created by psweeney on 9/13/16.
 */
public class Circle2D extends Shape2D {
    private float x, y, radius;

    public Circle2D(PApplet applet, DrawFlags flags, float x, float y, float radius){
        this.applet = applet;
        this.flags = flags;
        this.x = x;
        this.y = y;
        this.radius = radius;
    }

    public float getX() {
        return x;
    }

    public void setX(float x) {
        this.x = x;
    }

    public float getY() {
        return y;
    }

    public void setY(float y) {
        this.y = y;
    }

    public float getRadius() {
        return radius;
    }

    public void setRadius(float radius) {
        this.radius = radius;
    }

    @Override
    protected void callDraw() {
        applet.ellipseMode(PConstants.CENTER);
        applet.ellipse(x, y, radius * 2, radius * 2);
    }

    @Override
    protected void callDraw(float xOffset, float yOffset) {
        applet.ellipseMode(PConstants.CENTER);
        applet.ellipse(x + xOffset, y + yOffset, radius * 2, radius * 2);
    }

    @Override
    protected void callDraw(float xOffset, float yOffset, float rotation) {
        callDraw(xOffset, yOffset);
    }
}
