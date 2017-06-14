package datatypes.geom;

import display.Color;
import display.DrawOptions;
import processing.core.PApplet;
import processing.core.PGraphics;

/**
 * Created by psweeney on 2/7/17.
 */
public class Vector {
    private float x, y, z;

    public Vector(float x, float y, float z){
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public Vector(float rotation, float pitch, float magnitude, Vector offsetVector){
        float xzMagnitude = PApplet.cos(pitch) * magnitude;
        x = PApplet.cos(rotation) * xzMagnitude;
        y = PApplet.sin(pitch) * magnitude;
        z = PApplet.sin(rotation) * xzMagnitude;

        if(offsetVector != null){
            x += offsetVector.x;
            y += offsetVector.y;
            z += offsetVector.z;
        }
    }

    public Vector(float x, float y){
        this(x, y, 0);
    }

    public Vector(Vector other){
        this(other.x, other.y, other.z);
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public float getZ() {
        return z;
    }

    public Vector mult(float scalar){
        return new Vector(x * scalar, y * scalar, z * scalar);
    }

    public Vector add(Vector other){
        return new Vector(x + other.x, y + other.y, z + other.z);
    }

    public float dist(Vector other){
        return PApplet.dist(x, y, z, other.x, other.y, other.z);
    }

    public float angleToOrigin2D(){
        return PApplet.atan2(y, x);
    }

    public float angleToOrigin2D(Vector effectiveCenter){
        Vector calibrated = new Vector(x - effectiveCenter.x, y - effectiveCenter.y);
        return calibrated.angleToOrigin2D();
    }

    public static float getAngle(Vector vertex, Vector p1, Vector p2){
        float vp1 = vertex.dist(p1), vp2 = vertex.dist(p2), p1p2 = p1.dist(p2);

        return PApplet.acos(
                (PApplet.sq(vp1) + PApplet.sq(vp2) - PApplet.sq(p1p2)) / (2 * vp1 * vp2)
        );
    }

    public void draw(PGraphics canvas, Color.ColorMode colorMode, DrawOptions drawOptions, boolean useZ){
        drawOptions.applyToPGraphics(canvas, colorMode);
        if(useZ) canvas.point(x, y, z);
        else canvas.point(x, y);
    }

    public void draw(PGraphics canvas, Color.ColorMode colorMode, DrawOptions drawOptions) {
        draw(canvas, colorMode, drawOptions, false);
    }
}
