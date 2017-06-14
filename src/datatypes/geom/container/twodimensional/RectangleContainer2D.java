package datatypes.geom.container.twodimensional;

import datatypes.geom.Vector;
import datatypes.geom.linear.twodimensional.Line2D;
import datatypes.geom.linear.twodimensional.LineSegment2D;
import datatypes.geom.linear.twodimensional.LinearObject2D;
import display.Color;
import display.DrawOptions;
import processing.core.PGraphics;

import static processing.core.PConstants.CORNER;

/**
 * Created by psweeney on 3/2/17.
 */
public class RectangleContainer2D implements ContainerObject2D {
    public enum InstantiationMode{
        LX_LY_WIDTH_HEIGHT, LX_LY_UX_UY, CX_CY_WIDTH_HEIGHT
    }
    private static InstantiationMode currentInstantiationMode = InstantiationMode.LX_LY_WIDTH_HEIGHT;

    public static void setCurrentInstantiationMode(InstantiationMode newMode){
        currentInstantiationMode = newMode;
    }

    public static InstantiationMode getCurrentInstantiationMode() {
        return currentInstantiationMode;
    }

    private float lx, ly, ux, uy, area;
    private Vector lxly, lxuy, uxly, uxuy;
    private LineSegment2D lxBound, lyBound, uxBound, uyBound;

    public RectangleContainer2D(float arg1, float arg2, float arg3, float arg4){
        switch (currentInstantiationMode){
            case LX_LY_WIDTH_HEIGHT:
                lx = arg1;
                ly = arg2;
                ux = arg1 + arg3;
                uy = arg2 + arg4;
                break;
            case LX_LY_UX_UY:
                lx = arg1;
                ly = arg2;
                ux = arg3;
                uy = arg4;
                break;
            case CX_CY_WIDTH_HEIGHT:
                lx = arg1 - arg3/2;
                ly = arg2 - arg4/2;
                ux = arg1 + arg3/2;
                uy = arg2 + arg4/2;
                break;
        }

        if(lx > ux){
            float tmp = lx;
            lx = ux;
            ux = tmp;
        }

        if(ly > uy){
            float tmp = ly;
            ly = uy;
            uy = tmp;
        }

        lxly = new Vector(lx, ly);
        lxuy = new Vector(lx, uy);
        uxly = new Vector(ux, ly);
        uxuy = new Vector(ux, uy);

        lxBound = new LineSegment2D(lx, ly, lx, uy);
        lyBound = new LineSegment2D(lx, ly, ux, ly);
        uxBound = new LineSegment2D(ux, ly, ux, uy);
        uyBound = new LineSegment2D(lx, uy, ux, uy);

        area = (ux - lx) * (uy - ly);
    }

    public float getLx() {
        return lx;
    }

    public float getLy() {
        return ly;
    }

    public float getUx() {
        return ux;
    }

    public float getUy() {
        return uy;
    }

    @Override
    public float getArea() {
        return area;
    }

    public Vector getLxly() {
        return lxly;
    }

    public Vector getLxuy() {
        return lxuy;
    }

    public Vector getUxly() {
        return uxly;
    }

    public Vector getUxuy() {
        return uxuy;
    }

    public LineSegment2D getLxBound() {
        return lxBound;
    }

    public LineSegment2D getLyBound() {
        return lyBound;
    }

    public LineSegment2D getUxBound() {
        return uxBound;
    }

    public LineSegment2D getUyBound() {
        return uyBound;
    }

    @Override
    public boolean inBounds(Vector v) {
        return v.getX() >= lx && v.getX() <= ux && v.getY() >= ly && v.getY() <= uy;
    }

    @Override
    public LineSegment2D getContainedSegment(LinearObject2D linearObject2D) {
        LineSegment2D bound1 = null, bound2 = null;

        Line2D line;

        if(linearObject2D instanceof LineSegment2D) line = ((LineSegment2D) linearObject2D).getLine();
        else if(linearObject2D instanceof Line2D) line = (Line2D) linearObject2D;
        else return null;


        if(line.isPoint()){
            Vector v = new Vector(line.getXIntercept(), line.getYIntercept());
            if(inBounds(v)) return new LineSegment2D(v, v);
            return null;
        } else if(line.isVertical()){
            bound1 = lyBound;
            bound2 = uyBound;
        } else if(line.getSlope() == 0){
            bound1 = lxBound;
            bound2 = lyBound;
        } else if(line.getSlope() > 0){
            int lxlyComp = line.compareToVectorY(lxly), lxuyComp = line.compareToVectorY(lxuy),
                    uxlyComp = line.compareToVectorY(uxly), uxuyComp = line.compareToVectorY(uxuy);

            if(uxlyComp < 0) return null;
            else if(lxlyComp <= 0) bound1 = lyBound;
            else if(lxuyComp < 0) bound1 = lxBound;
            else return null;

            if(uxuyComp <= 0) bound2 = uxBound;
            else bound2 = uyBound;
        } else {
            int lxlyComp = line.compareToVectorY(lxly), lxuyComp = line.compareToVectorY(lxuy),
                    uxlyComp = line.compareToVectorY(uxly), uxuyComp = line.compareToVectorY(uxuy);

            if(lxlyComp < 0) return null;
            else if(lxuyComp <= 0) bound1 = lxBound;
            else if(uxuyComp < 0) bound1 = uyBound;
            else return null;

            if(uxlyComp <= 0) bound2 = lyBound;
            else bound2 = uxBound;
        }

        if(bound1 == null || bound2 == null) return null;
        Vector v1 = bound1.getIntersection(linearObject2D), v2 = bound2.getIntersection(linearObject2D);

        if(linearObject2D instanceof LineSegment2D){
            if(v1 == null && v2 == null){
                if(inBounds(((LineSegment2D) linearObject2D).getStartVector()) &&
                        inBounds(((LineSegment2D) linearObject2D).getEndVector())){
                    v1 = ((LineSegment2D) linearObject2D).getStartVector();
                    v2 = ((LineSegment2D) linearObject2D).getEndVector();
                } else return null;
            } else if(v1 == null){
                if(inBounds(((LineSegment2D) linearObject2D).getStartVector()))
                    v1 = ((LineSegment2D) linearObject2D).getStartVector();
                else if(inBounds(((LineSegment2D) linearObject2D).getEndVector()))
                    v1 = ((LineSegment2D) linearObject2D).getEndVector();
                else return null;
            } else if(v2 == null){
                if(inBounds(((LineSegment2D) linearObject2D).getStartVector()))
                    v2 = ((LineSegment2D) linearObject2D).getStartVector();
                else if(inBounds(((LineSegment2D) linearObject2D).getEndVector()))
                    v2 = ((LineSegment2D) linearObject2D).getEndVector();
                else return null;
            }
        }

        if(v1 == null || v2 == null) return null;

        return new LineSegment2D(v1, v2);
    }

    @Override
    public void draw(PGraphics canvas, Color.ColorMode colorMode, DrawOptions drawOptions) {
        drawOptions.applyToPGraphics(canvas, colorMode);
        canvas.rectMode(CORNER);
        canvas.rect(lx, ly, ux - lx, uy - ly);
    }
}
