package datatypes.geom.linear.twodimensional;

import datatypes.geom.Vector;
import display.Color;
import display.DrawOptions;
import processing.core.PApplet;
import processing.core.PGraphics;

/**
 * Created by psweeney on 2/28/17.
 */
public class Line2D implements LinearObject2D{
    private float slope, invSlope, xIntercept, yIntercept;
    private boolean isVertical, isPoint;

    public Line2D(float x1, float y1, float x2, float y2){
        if(x1 == x2 && y1 == y2){
            isPoint = true;
            slope = 0;
            invSlope = 0;
            xIntercept = x1;
            yIntercept = y1;
            return;
        }

        isPoint = false;

        if(x1 == x2){
            isVertical = true;

            slope = 0;
            invSlope = 0;
            xIntercept = x1;
            yIntercept = 0;
            return;
        }

        isVertical = false;

        if(y1 == y2){
            slope = 0;
            invSlope = 0;
            xIntercept = 0;
            yIntercept = y1;
            return;
        }

        slope = (y2 - y1)/(x2 - x1);
        invSlope = -1/slope;
        yIntercept = y1 - x1 * slope;
        xIntercept = x1 + y1 * invSlope;
    }

    public Line2D(float x, float y, float slope, boolean isVertical){
        this(x, y, x + 1, y + slope);
        if(isVertical){
            this.isVertical = true;
            this.slope = 0;
            this.invSlope = 0;
            this.xIntercept = x;
            this.yIntercept = 0;
        }
    }

    public Line2D(float x, float y, float angle){
        this(x, y, x + PApplet.cos(angle), y + PApplet.sin(angle));
    }

    public boolean isPoint() {
        return isPoint;
    }

    public boolean isVertical() {
        return isVertical;
    }

    public float getSlope() {
        return slope;
    }

    public float getInvSlope() {
        return invSlope;
    }

    public float getYIntercept() {
        return yIntercept;
    }

    public float getXIntercept() {
        return xIntercept;
    }

    @Override
    public Vector getPointForX(float x){
        if(isPoint) {
            if (x == xIntercept) return new Vector(xIntercept, yIntercept);
            return null;
        }

        if(isVertical) return null;
        if(slope == 0) return new Vector(x, yIntercept);
        return new Vector(x, yIntercept + x * slope);
    }

    @Override
    public Vector getPointForY(float y){
        if(isPoint){
            if(y == yIntercept) return new Vector(xIntercept, yIntercept);
            return null;
        }

        if(isVertical) return new Vector(xIntercept, y);
        else if(slope == 0) return null;

        return new Vector(xIntercept - y * invSlope, y);
    }

    public int compareToVectorY(Vector v){
        if(isPoint || slope == 0){
            if(yIntercept < v.getY()) return -1;
            else if(yIntercept > v.getY()) return 1;
            return 0;
        } else if(isVertical) return 0;

        Vector vt = getPointForX(v.getX());
        if(vt.getY() < v.getY()) return -1;
        else if(vt.getY() > v.getY()) return 1;
        return 0;
    }

    public int compareToVectorX(Vector v){
        if(isPoint || isVertical){
            if(xIntercept < v.getX()) return -1;
            else if(xIntercept > v.getX()) return 1;
            return 0;
        } else if(slope == 0) return 0;

        Vector vt = getPointForY(v.getY());
        if(vt.getX() < v.getX()) return -1;
        else if(vt.getX() > v.getX()) return 1;
        return 0;
    }


    @Override
    public Vector getIntersection(LinearObject2D other) {
        if(!(other instanceof Line2D)) return other.getIntersection(this);
        Line2D otherLine2D = (Line2D) other;

        if(isPoint){
            if(otherLine2D.isPoint){
                if(xIntercept == otherLine2D.xIntercept && yIntercept == otherLine2D.yIntercept)
                    return new Vector(xIntercept, yIntercept);
                return null;
            } else if(otherLine2D.isVertical){
                if(otherLine2D.xIntercept == xIntercept) return new Vector(xIntercept, yIntercept);
                return null;
            } else if(otherLine2D.slope == 0){
                if(otherLine2D.yIntercept == yIntercept) return new Vector(xIntercept, yIntercept);
                return null;
            }
            Vector v = otherLine2D.getPointForX(xIntercept);
            if(v.dist(new Vector(xIntercept, yIntercept)) > 0) return null;
            return v;
        } else if(isVertical){
            if(otherLine2D.isPoint){
                if(xIntercept == otherLine2D.xIntercept) return new Vector(xIntercept, otherLine2D.yIntercept);
                return null;
            } else if(otherLine2D.isVertical) return null;
            return otherLine2D.getPointForX(xIntercept);
        } else if(slope == 0){
            if(otherLine2D.isPoint){
                if(yIntercept == otherLine2D.yIntercept) return new Vector(otherLine2D.xIntercept, yIntercept);
                return null;
            } else if(otherLine2D.isVertical) return new Vector(otherLine2D.xIntercept, yIntercept);
            else if(otherLine2D.slope == 0) return null;
            return other.getPointForY(yIntercept);
        } else {
            if(otherLine2D.isPoint){
                Vector v = getPointForX(otherLine2D.getXIntercept());
                if(v.dist(new Vector(otherLine2D.xIntercept, otherLine2D.yIntercept)) > 0) return null;
                return v;
            } else if(otherLine2D.isVertical) return getPointForX(otherLine2D.xIntercept);
            else if(otherLine2D.slope == 0) return getPointForY(otherLine2D.yIntercept);
            float x = (otherLine2D.yIntercept - yIntercept)/(slope - otherLine2D.slope);
            return getPointForX(x);
        }
    }

    @Override
    public Vector getClosestPoint(Vector v) {
        float x = v.getX(), y = v.getY();

        if(isPoint){
            return new Vector(xIntercept, yIntercept);
        } else if(isVertical){
            return new Vector(xIntercept, v.getY());
        } else if(slope == 0){
            return new Vector(v.getX(), yIntercept);
        }

        Line2D perpendicularLine = new Line2D(v.getX(), v.getY(), invSlope, false);
        return getIntersection(perpendicularLine);
    }

    @Override
    public void draw(PGraphics canvas, Color.ColorMode colorMode, DrawOptions drawOptions) {
        drawOptions.applyToPGraphics(canvas, colorMode);

        Vector v1, v2;
        if(isVertical){
            v1 = getPointForY(0);
            v2 = getPointForY(canvas.height);
            canvas.line(v1.getX(), v1.getY(), v2.getX(), v2.getY());
        } else {
            v1 = getPointForX(0);
            v2 = getPointForX(canvas.width);
        }

        if(v1 != null && v2 != null) canvas.line(v1.getX(), v1.getY(), v2.getX(), v2.getY());
    }
}
