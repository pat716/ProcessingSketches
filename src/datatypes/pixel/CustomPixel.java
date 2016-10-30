package datatypes.pixel;

import processing.core.PApplet;

/**
 * Created by psweeney on 9/19/16.
 */
public abstract class CustomPixel {
    private float x, y;
    private int intX, intY;

    public CustomPixel(float x, float y){
        this.x = x;
        this.y = y;
        this.intX = convertInt(x);
        this.intY = convertInt(y);
    }

    public static int convertInt(double i){
        return (int) Math.round(i);
    }

    public float getX() {
        return x;
    }

    public void setX(float x) {
        this.x = x;
        this.intX = convertInt(x);
    }

    public float getY() {
        return y;
    }

    public void setY(float y) {
        this.y = y;
        this.intY = convertInt(y);
    }

    public int getIntX() {
        return intX;
    }

    public int getIntY() {
        return intY;
    }

    public abstract Color getColor();
}
