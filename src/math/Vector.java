package math;

import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PVector;

import java.util.Comparator;

import static processing.core.PConstants.PATH;
import static processing.core.PConstants.TWO_PI;

/**
 * Created by psweeney on 9/13/16.
 */
public class Vector extends PVector{
    private float magnitude = 0;

    public Vector(){
        super();
        setMagnitude();
    }

    public Vector(float x, float y, float z){
        super(x, y, z);
        setMagnitude();
    }

    public Vector(float x, float y){
        this(x, y, 0);
    }

    public Vector(Vector start, float rAngle, float vAngle, float distance){
        if(start == null){
            start = new Vector(0, 0, 0);
        }

        float heightOffset = PApplet.sin(vAngle) * distance, baseOffset = PApplet.cos(vAngle) * distance;
        float widthOffset = PApplet.cos(rAngle) * baseOffset, depthOffset = PApplet.sin(rAngle) * baseOffset;

        this.x = start.x + widthOffset;
        this.y = start.y + heightOffset;
        this.z = start.z + depthOffset;

        setMagnitude();
    }

    public Vector(Vector other){
        if(other == null){
            this.x = 0;
            this.y = 0;
            this.z = 0;
        } else {
            this.x = other.x;
            this.y = other.y;
            this.z = other.z;
        }

        setMagnitude();
    }

    public float getAngleToVector(Vector other){
        if(other == null){
            return 0;
        }

        //return angleBetween(other, this);

        return (float) Math.atan2(y - other.y, x - other.x);
    }

    public Vector myAdd(Vector other){
        if(other == null) return new Vector(this);
        return new Vector(x + other.x, y + other.y, z + other.z);
    }

    public Vector mySubtract(Vector other){
        if(other == null) return new Vector(this);
        return new Vector(x - other.x, y - other.y, z - other.z);
    }

    public float getAngleFromOrigin(){
        return getAngleToVector(new Vector(0, 0));
    }

    public static float getXForAngle(float angle, float distance){
        return PApplet.cos(angle) * distance;
    }

    public static float getYForAngle(float angle, float distance){
        return PApplet.sin(angle) * distance;
    }

    public static float getMinAngleOffsetForDifferentPixel(float magnitude){
        return (float) Math.PI/(float) Math.pow(2, magnitude);
    }

    public static float getAbsoluteAngleDistance(float angle1, float angle2){
        angle1 %= TWO_PI;
        angle2 %= TWO_PI;
        float d1 = PApplet.abs(angle1 - angle2) % TWO_PI;
        float d2 = PApplet.abs((angle1 + TWO_PI) - angle2) % TWO_PI;
        float d3 = PApplet.abs((angle1 - TWO_PI) - angle2) % TWO_PI;

        return PApplet.min(d1, PApplet.min(d2, d3));
    }

    public static Vector getVectorRelativeToOtherVector(Vector origin, float angle, float distance){
        if(origin == null){
            return new Vector(getXForAngle(angle, distance), getYForAngle(angle, distance));
        }
        return new Vector(origin.x + getXForAngle(angle, distance), origin.y + getYForAngle(angle, distance));
    }

    public static Vector getVectorRelativeToOrigin(float angle, float distance){
        return getVectorRelativeToOtherVector(null, angle, distance);
    }

    public static Comparator<Vector> randomVectorComparator = new Comparator<Vector>() {
        @Override
        public int compare(Vector o1, Vector o2) {
            if(o1 == o2) return 0;
            if(o1 == null) return 1;
            if(o2 == null) return -1;
            if(o1.equals(o2)) return 0;
            if(Math.random() < 0.5) return -1;
            return 1;
        }
    };

    private void setMagnitude(){
        magnitude = PApplet.dist(0, 0, 0, x, y, z);
    }

    public float getMagnitude(){
         return magnitude;
     }
}
