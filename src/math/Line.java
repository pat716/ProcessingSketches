package math;

import processing.core.PApplet;

import java.awt.geom.Line2D;

/**
 * Created by psweeney on 9/23/16.
 */
public class Line {

    public static class PointSlopeLine{
        private float slope, yIntercept;
        private boolean valid = true;
        private float verticalLineXValue = 0;

        private PointSlopeLine(float slope, float yIntercept){
            this.slope = slope;
            this.yIntercept = yIntercept;
        }

        private PointSlopeLine(float x1, float y1, float x2, float y2){
            if(x1 == x2){
                verticalLineXValue = x1;
                slope = 0;
                yIntercept = 0;
                valid = false;
                return;
            } else if(y1 == y2){
                slope = 0;
                yIntercept = y1;
            } else {
                slope = (y2 - y1) / (x2 - x1);
                yIntercept = (slope * -x1) + y1;
            }
        }

        public boolean isValid() {
            return valid;
        }

        public float getSlope() {
            return slope;
        }

        public float getYIntercept() {
            return yIntercept;
        }

        public float getVerticalLineXValue() {
            return verticalLineXValue;
        }

        public float getYForX(float x){
            return yIntercept + (x * slope);
        }

        public Vector getIntersection(PointSlopeLine other){
            if(other == null || (!valid && !other.valid) || slope == other.slope) return null;
            if(!valid) return new Vector(verticalLineXValue, other.getYForX(verticalLineXValue));
            else if(!other.valid) return new Vector(other.verticalLineXValue, getYForX(other.verticalLineXValue));

            if(slope == 0){
                float x = (yIntercept - other.yIntercept)/other.slope;
                return new Vector(x, yIntercept);
            } else if(other.slope == 0){
                float x = (other.yIntercept - yIntercept)/slope;
                return new Vector(x, other.yIntercept);
            }
            float combinedSlope = slope - other.slope, combinedYIntercept = yIntercept - other.yIntercept;
            float x = combinedYIntercept/combinedSlope, y = getYForX(x);
            return new Vector(x, y);
        }
    }

    private Vector v1, v2;
    private float distance;
    private boolean ignoreZ;
    private PointSlopeLine pointSlopeLine;

    public Line(Vector v1, Vector v2){
        this.v1 = v1;
        this.v2 = v2;
        distance = v1.dist(v2);

        if(v1.z == v2.z){
            ignoreZ = true;
        } else {
            ignoreZ = false;
        }

        pointSlopeLine = new PointSlopeLine(v1.x, v1.y, v2.x, v2.y);
    }

    public Line(float x1, float y1, float z1, float x2, float y2, float z2){
        this(new Vector(x1, y1, z1), new Vector(x2, y2, z2));
    }

    public Line(float x1, float y1, float x2, float y2){
        this(x1, y1, 0, x2, y2, 0);
    }

    public PointSlopeLine getPointSlopeLine() {
        return pointSlopeLine;
    }

    public Vector getV1() {
        return v1;
    }

    public Vector getV2() {
        return v2;
    }

    public void swapEndpoints(){
        Vector v = v1;
        this.v1 = v2;
        this.v2 = v;
    }

    public float getDistance() {
        return distance;
    }

    public float ptSegDist(Vector v){
        if(v == null){
            return 0;
        }

        return (float) Line2D.ptSegDist(v1.x, v1.y, v2.x, v2.y, v.x, v.y);
    }

    public Vector getPointOnLine(float progressAmount){
        if(progressAmount < 0){
            return v1;
        } else if(progressAmount > 1){
            return v2;
        }

        float mx = v1.x + (v2.x - v1.x) * progressAmount;
        float my = v1.y + (v2.y - v1.y) * progressAmount;
        float mz = v1.z + (v2.z - v1.z) * progressAmount;

        return new Vector(mx, my, mz);
    }

    public boolean isInLineRegion(float x, float y){
        if((x < v1.x && x < v2.x) || (x > v1.x && x > v2.x) || (y < v1.y && y < v2.y) || (y > v1.y && y > v2.y))
            return false;
        return true;
    }

    public boolean existsOnLine(float x, float y, float errorAmount){
        if(!isInLineRegion(x, y)) return false;

        float distance;

        if(!pointSlopeLine.valid){
            distance = PApplet.abs(x - pointSlopeLine.verticalLineXValue);
        } else {
            distance = PApplet.dist(x, y, x, pointSlopeLine.getYForX(x));
        }

        if(distance > errorAmount) return false;
        return true;
    }

    public boolean ignoreZForComparison(Line other){
        if(other == null){
            return true;
        }

        return ignoreZ && other.ignoreZ && v1.z == other.v1.z;
    }

    public boolean intersects(Line other){
        if(other == null){
            return false;
        }

        if(ignoreZForComparison(other)){

            return Line2D.linesIntersect(v1.x, v1.y, v2.x, v2.y, other.v1.x, other.v1.y, other.v2.x, other.v2.y);
        }

        //TODO 3D line intersection?
        return false;
    }

    public Vector getPhysicsIntersection(Line other){
        if(other == null || !ignoreZForComparison(other) || !intersects(other)){
            return null;
        }

        float progressIncrease = 1/ PApplet.max(distance, other.distance);
        float lastDistance = v1.dist(other.v1);
        for(float i = 0; i < 1; i += progressIncrease){
            Vector c1 = getPointOnLine(i), c2 = other.getPointOnLine(i);
            float distance = c1.dist(c2);
            if(distance <= PApplet.sqrt(2)){
                return c1;
            } else if(distance > lastDistance){
                break;
            }

            lastDistance = distance;
        }
        return null;
    }
}
