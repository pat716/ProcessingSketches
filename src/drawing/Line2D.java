package drawing;

import processing.core.PApplet;

/**
 * Created by psweeney on 9/13/16.
 */
public class Line2D extends Shape2D {
    private float x1, y1, x2, y2;

    public Line2D(PApplet applet, DrawFlags flags, float x1, float y1, float x2, float y2){
        this.applet = applet;
        this.flags = flags;
        this.x1 = x1;
        this.y1 = y1;
        this.x2 = x2;
        this.y2 = y2;
    }

    public float getX1() {
        return x1;
    }

    public void setX1(float x1) {
        this.x1 = x1;
    }

    public float getY1() {
        return y1;
    }

    public void setY1(float y1) {
        this.y1 = y1;
    }

    public float getX2() {
        return x2;
    }

    public void setX2(float x2) {
        this.x2 = x2;
    }

    public float getY2() {
        return y2;
    }

    public void setY2(float y2) {
        this.y2 = y2;
    }

    @Override
    protected void callDraw() {
        applet.line(x1, y1, x2, y2);
    }

    @Override
    protected void callDraw(float xOffset, float yOffset) {
        applet.line(x1 + xOffset, y1 + yOffset, x2 + xOffset, y2 + yOffset);
    }

    @Override
    protected void callDraw(float xOffset, float yOffset, float rotation) {
        float centerX = (x1 + x2)/2, centerY = (y1 + y2)/2;
        float x1Adjusted = x1 - centerX, y1Adjusted = y1 - centerY, x2Adjusted = x2 - centerX, y2Adjusted = y2 -
                centerY;

        applet.pushMatrix();
        applet.translate(centerX + xOffset, centerY + yOffset);
        applet.rotate(rotation);
        applet.line(x1Adjusted, y1Adjusted, x2Adjusted, y2Adjusted);
        applet.popMatrix();
    }
}
