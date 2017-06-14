package datatypes.geom.container.twodimensional;

import datatypes.geom.Vector;
import datatypes.geom.linear.twodimensional.Line2D;
import datatypes.geom.linear.twodimensional.LineSegment2D;
import datatypes.geom.linear.twodimensional.LinearObject2D;
import display.Color;
import display.DrawOptions;
import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PGraphics;

/**
 * Created by psweeney on 3/8/17.
 */
public class TriangleContainer2D implements ContainerObject2D {
    private static final float IN_BOUNDS_AREA_ERROR_ALLOWANCE = 0.05f;

    float x1, y1, x2, y2, x3, y3, area;
    private LineSegment2D v1v2, v1v3, v2v3;

    public TriangleContainer2D(float x1, float y1, float x2, float y2, float x3, float y3){
        this.x1 = x1;
        this.y1 = y1;
        this.x2 = x2;
        this.y2 = y2;
        this.x3 = x3;
        this.y3 = y3;

        area = getTriangleArea(x1, y1, x2, y2, x3, y3);

        v1v2 = new LineSegment2D(x1, y1, x2, y2);
        v1v3 = new LineSegment2D(x1, y1, x3, y3);
        v2v3 = new LineSegment2D(x2, y2, x3, y3);

    }

    private TriangleContainer2D(Vector v1, Vector v2, Vector v3){
        this(v1.getX(), v1.getY(), v2.getX(), v2.getY(), v3.getX(), v3.getY());
    }

    public float getArea(){
        return area;
    }

    private boolean inBounds(float x, float y){
        float areaV12 = getTriangleArea(x, y, x1, y1, x2, y2),
                areaV13 = getTriangleArea(x, y, x1, y1, x3, y3),
                areaV23 = getTriangleArea(x, y, x2, y2, x3, y3);

        if(PApplet.abs(area - (areaV12 + areaV13 + areaV23)) <= IN_BOUNDS_AREA_ERROR_ALLOWANCE) return true;
        return false;
    }

    @Override
    public boolean inBounds(Vector v) {
        return inBounds(v.getX(), v.getY());
    }

    public static float getTriangleArea(float x1, float y1, float x2, float y2, float x3, float y3){
        return PApplet.abs((x1*(y2-y3) + x2*(y3-y1)+ x3*(y1-y2))/2f);
    }

    @Override
    public LineSegment2D getContainedSegment(LinearObject2D linearObject2D) {
        if(linearObject2D instanceof LineSegment2D && inBounds(((LineSegment2D) linearObject2D).getStartVector()) &&
                inBounds(((LineSegment2D) linearObject2D).getEndVector()))
            return new LineSegment2D(((LineSegment2D) linearObject2D).getStartVector(),
                    ((LineSegment2D) linearObject2D).getEndVector());

        Vector v12 = v1v2.getIntersection(linearObject2D), v13 = v1v3.getIntersection(linearObject2D),
                v23 = v2v3.getIntersection(linearObject2D);
        if(v12 != null && v13 != null) return new LineSegment2D(v12, v13);
        else if(v12 != null && v23 != null) return new LineSegment2D(v12, v23);
        else if(v13 != null && v23 != null) return new LineSegment2D(v13, v23);

        if(!(linearObject2D instanceof LineSegment2D)) return null;
        Vector startPoint = ((LineSegment2D) linearObject2D).getStartVector(),
                endPoint = ((LineSegment2D) linearObject2D).getEndVector();

        if(v12 == null && v13 == null && v23 == null){
            if(inBounds(startPoint) && inBounds(endPoint)) return new LineSegment2D(startPoint, endPoint);
            return null;
        } else if(v12 != null){
            if(inBounds(startPoint)) return new LineSegment2D(v12, startPoint);
            else if(inBounds(endPoint)) return new LineSegment2D(v12, endPoint);
            return null;
        } else if(v13 != null){
            if(inBounds(startPoint)) return new LineSegment2D(v13, startPoint);
            else if(inBounds(endPoint)) return new LineSegment2D(v13, endPoint);
            return null;
        }
        if(inBounds(startPoint)) return new LineSegment2D(v23, startPoint);
        else if(inBounds(endPoint)) return new LineSegment2D(v23, endPoint);
        return null;
    }

    @Override
    public void draw(PGraphics canvas, Color.ColorMode colorMode, DrawOptions drawOptions) {
        drawOptions.applyToPGraphics(canvas, colorMode);
        canvas.beginShape(PConstants.TRIANGLES);
        canvas.vertex(x1, y1);
        canvas.vertex(x2, y2);
        canvas.vertex(x3, y3);
        canvas.endShape();
    }
}
