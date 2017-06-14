package datatypes.geom.linear.twodimensional;

import datatypes.geom.Vector;
import display.Color;
import display.DrawOptions;
import processing.core.PApplet;
import processing.core.PGraphics;

/**
 * Created by psweeney on 3/2/17.
 */
public class LineSegment2D implements LinearObject2D {
    private float x1, y1, x2, y2;
    Vector startVector, endVector;
    private Line2D line;

    public LineSegment2D(float x1, float y1, float x2, float y2){
        this.x1 = x1;
        this.y1 = y1;
        this.x2 = x2;
        this.y2 = y2;
        line = new Line2D(x1, y1, x2, y2);

        if(x1 < x2){
            startVector = new Vector(x1, y1);
            endVector = new Vector(x2, y2);
        } else if(x1 > x2){
            startVector = new Vector(x2, y2);
            endVector = new Vector(x1, y1);
        } else {
            if(y1 < y2){
                startVector = new Vector(x1, y1);
                endVector = new Vector(x2, y2);
            } else {
                startVector = new Vector(x2, y2);
                endVector = new Vector(x1, y1);
            }
        }
    }

    public LineSegment2D(Vector v1, Vector v2){
        this(v1.getX(), v1.getY(), v2.getX(), v2.getY());
    }

    public Line2D getLine() {
        return line;
    }

    public Vector getStartVector(){
        return startVector;
    }

    public Vector getEndVector(){
        return endVector;
    }

    public boolean isOnLine(Vector v, float errorAmount){
        Vector closestPoint = getClosestPoint(v);
        return closestPoint != null && closestPoint.dist(v) <= errorAmount;
    }

    public boolean isOnLine(Vector v){
        return isOnLine(v, 0);
    }

    @Override
    public Vector getPointForX(float x) {
        if(line.isVertical() || x < startVector.getX() || x > endVector.getX()) return null;
        return line.getPointForX(x);
    }

    @Override
    public Vector getPointForY(float y) {
        if(line.isVertical()){
            if(y < startVector.getY() || y > endVector.getY()) return null;
            return new Vector(line.getXIntercept(), y);
        } else if(line.getSlope() == 0) return null;

        Vector linePoint = line.getPointForY(y);
        if(linePoint.getX() < startVector.getX() || linePoint.getX() > endVector.getX()) return null;
        return linePoint;
    }

    @Override
    public Vector getIntersection(LinearObject2D other) {
        if(other instanceof Line2D){
            Vector lineIntersection = line.getIntersection(other);
            if(lineIntersection == null || !isOnLine(lineIntersection)) return null;
            return lineIntersection;
        } else if(other instanceof LineSegment2D){
            Vector lineIntersection = line.getIntersection(((LineSegment2D) other).line);
            if(lineIntersection == null || !isOnLine(lineIntersection) ||
                    !((LineSegment2D) other).isOnLine(lineIntersection)) return null;
            return lineIntersection;
        }
        return null;
    }

    @Override
    public Vector getClosestPoint(Vector v) {
        Vector lineVector = line.getClosestPoint(v);
        if(line.isVertical()){
            if(lineVector.getY() < startVector.getY()) return startVector;
            else if(lineVector.getY() > endVector.getY()) return endVector;
        } else {
            if(lineVector.getX() < startVector.getX()) return startVector;
            else if(lineVector.getX() > endVector.getX()) return endVector;
        }
        return lineVector;
    }

    @Override
    public void draw(PGraphics canvas, Color.ColorMode colorMode, DrawOptions drawOptions) {
        drawOptions.applyToPGraphics(canvas, colorMode);
        canvas.line(startVector.getX(), startVector.getY(), endVector.getX(), endVector.getY());
    }


    /*
    private Line2D line;
    private float startValue, endValue;

    public LineSegment2D(Line2D line, float startValue, float endValue){
        this.line = line;
        this.startValue = PApplet.min(startValue, endValue);
        this.endValue = PApplet.max(startValue, endValue);
    }

    public LineSegment2D(float x1, float y1, float x2, float y2){
        line = new Line2D(x1, y1, x2, y2);
        if(line.isVertical()){
            startValue = PApplet.min(y1, y2);
            endValue = PApplet.max(y1, y2);
        } else {
            startValue = PApplet.min(x1, x2);
            endValue = PApplet.max(x1, x2);
        }
    }

    public LineSegment2D(Vector v1, Vector v2){
        this(v1.getX(), v1.getY(), v2.getX(), v2.getY());
    }

    public LineSegment2D(Vector startVector, float angle, float magnitude){
        this(startVector.getX(), startVector.getY(), startVector.getX() + PApplet.cos(angle) * magnitude,
                startVector.getY() + PApplet.sin(angle) * magnitude);
    }

    public Line2D getLine() {
        return line;
    }

    public Vector getStartVector(){
        if(line.isVertical()) return line.getPointForY(startValue);
        return line.getPointForX(startValue);
    }

    public Vector getEndVector(){
        if(line.isVertical()) return line.getPointForY(endValue);
        return line.getPointForX(endValue);
    }

    public boolean isOnLine(Vector v, float errorAmount){
        Vector testVector = null;
        if(line.isVertical()){
            if(v.getY() < startValue) testVector = getStartVector();
            else if(v.getY() > endValue) testVector = getEndVector();
            else testVector = getPointForY(v.getY());
        } else {
            if(v.getX() < startValue) testVector = getStartVector();
            else if(v.getX() > endValue) testVector = getEndVector();
            else testVector = getPointForX(v.getX());
        }

        return testVector != null && testVector.dist(v) <= errorAmount;
    }

    public boolean isOnLine(Vector v){
        return isOnLine(v, 0);
    }

    @Override
    public Vector getPointForX(float x) {
        if(line.isVertical() || x < startValue || x > endValue) return null;
        return line.getPointForX(x);
    }

    @Override
    public Vector getPointForY(float y) {
        if(line.isVertical()){
            if(y < startValue || y > endValue) return null;
            return new Vector(line.getXIntercept(), y);
        }
        if (line.getSlope() == 0) return null;

        Vector v = line.getPointForY(y);

        if(v == null || v.getX() < startValue || v.getX() > endValue) return null;
        return v;
    }

    @Override
    public Vector getIntersection(LinearObject2D other) {
        Vector lineIntersection = null;
        if(other instanceof LineSegment2D){
            Line2D otherLine = ((LineSegment2D) other).getLine();

            if(line.isVertical()){
                if(otherLine.isVertical()) return null;
                else if(otherLine.getSlope() == 0){
                    if(otherLine.getYIntercept() < startValue || otherLine.getYIntercept() > endValue ||
                            line.getXIntercept() < ((LineSegment2D) other).startValue ||
                            line.getXIntercept() > ((LineSegment2D) other).endValue) return null;
                    return new Vector(line.getXIntercept(), otherLine.getYIntercept());
                }
                Vector v = otherLine.getPointForX(line.getXIntercept());
                if(v.getX() < ((LineSegment2D) other).startValue || v.getX() > ((LineSegment2D) other).endValue ||
                        v.getY() < startValue || v.getY() > endValue) return null;
                return v;
            } else if(line.getSlope() == 0){
                if(otherLine.isVertical()){
                    if(otherLine.getXIntercept() < startValue || otherLine.getXIntercept() > endValue ||
                            line.getYIntercept() < ((LineSegment2D) other).startValue ||
                            line.getYIntercept() > ((LineSegment2D) other).endValue) return null;
                    return new Vector(otherLine.getXIntercept(), line.getYIntercept());
                } else if(otherLine.getSlope() == 0) return null;
                Vector v = otherLine.getPointForY(line.getYIntercept());
                if(v.getX() < startValue || v.getX() > endValue || v.getX() < ((LineSegment2D) other).startValue ||
                        v.getX() > ((LineSegment2D) other).endValue) return null;
                return v;
            }

            if(otherLine.isVertical()){
                Vector v = line.getPointForX(otherLine.getXIntercept());
                if(v.getX() < startValue || v.getX() > endValue || v.getY() < ((LineSegment2D) other).startValue ||
                        v.getY() > ((LineSegment2D) other).endValue) return null;
                return v;
            } else if(otherLine.getSlope() == 0){
                Vector v = line.getPointForY(otherLine.getYIntercept());
                if(v.getX() < startValue || v.getX() > endValue || v.getX() < ((LineSegment2D) other).startValue ||
                        v.getX() > ((LineSegment2D) other).endValue) return null;
                return v;
            }
            lineIntersection = line.getIntersection(((LineSegment2D) other).line);
            if(lineIntersection == null || !isOnLine(lineIntersection) || !((LineSegment2D) other).isOnLine(lineIntersection))
                return null;
        } else if(other instanceof Line2D){
            lineIntersection = line.getIntersection(other);
            if(lineIntersection == null || !isOnLine(lineIntersection)) return null;
        }
        return lineIntersection;
    }

    @Override
    public Vector getClosestPoint(Vector v) {
        if(isOnLine(v)) return v;
        Vector lineClosestPoint = line.getClosestPoint(v);
        if(isOnLine(lineClosestPoint)) return lineClosestPoint;

        if(line.isVertical()){
            if(lineClosestPoint.getY() < startValue) return getStartVector();
            return getEndVector();
        }

        if(lineClosestPoint.getX() < startValue) return getStartVector();
        return getEndVector();
    }

    @Override
    public void draw(PGraphics canvas, Color.ColorMode colorMode, DrawOptions drawOptions) {
        drawOptions.applyToPGraphics(canvas, colorMode);
        Vector start = getStartVector(), end = getEndVector();
        canvas.line(start.getX(), start.getY(), end.getX(), end.getY());
    }
    */
}
