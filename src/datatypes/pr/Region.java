package datatypes.pr;

import datatypes.pixel.CustomPixel;
import math.Line;
import math.Vector;
import processing.core.PApplet;

import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import static processing.core.PConstants.TWO_PI;

/**
 * Created by psweeney on 9/18/16.
 */
public class Region {
    private float lx, ly, width, height, cx, cy;
    private Line leftLine, rightLine, bottomLine, topLine;

    public static enum SubQuadrant{ LX_LY, LX_UY, UX_LY, UX_UY, OUT_OF_BOUNDS }

    public static enum RelativeQuadrant { LX_LY, LX_CY, LX_UY, CX_LY, CX_CY, CX_UY, UX_LY, UX_CY, UX_UY }

    public Region(float lx, float ly, float width, float height){
        this.lx = lx;
        this.ly = ly;
        this.width = width;
        this.height = height;
        cx = lx + width/2;
        cy = ly + height/2;

        leftLine = new Line(lx, ly, lx, ly + height);
        rightLine = new Line(lx + width, ly, lx + width, ly + height);
        bottomLine = new Line(lx, ly, lx + width, ly);
        topLine = new Line(lx, ly + height, lx + width, ly + height);
    }

    public float getLx() {
        return lx;
    }

    public float getLy() {
        return ly;
    }

    public float getWidth() {
        return width;
    }

    public float getHeight() {
        return height;
    }

    public float getCx() {
        return cx;
    }

    public float getCy() {
        return cy;
    }

    public Line getLeftLine() {
        return leftLine;
    }

    public Line getRightLine() {
        return rightLine;
    }

    public Line getBottomLine() {
        return bottomLine;
    }

    public Line getTopLine() {
        return topLine;
    }

    public boolean inBounds(float x, float y){
        return inBounds(new Vector(x, y));
    }

    public Vector getCornerVector(SubQuadrant q){
        switch (q){
            case LX_LY:
                return new Vector(lx, ly);
            case LX_UY:
                return new Vector(lx, ly + height);
            case UX_LY:
                return new Vector(lx + width, ly);
            case  UX_UY:
                return new Vector(lx + width, ly + height);
        }
        return new Vector(cx, cy);
    }

    public boolean inBounds(Line l){
        if(l == null){
            return false;
        }

        Rectangle2D.Float r = new Rectangle2D.Float(lx, ly, width, height);
        return r.intersectsLine(l.getV1().x, l.getV1().y, l.getV2().x, l.getV2().y);
    }

    public boolean inBounds(Vector v){
        if(v == null){
            return false;
        }

        if(v.x < lx || v.x > lx + width || v.y < ly || v.y > ly + height){
            return false;
        }

        return true;
    }

    public boolean intersects(Region other){
        if(other == null) return false;

        float minX1 = lx, maxX1 = lx + width, minY1 = ly, maxY1 = ly + height;
        float minX2 = other.lx, maxX2 = other.lx + other.width, minY2 = other.ly, maxY2 = other.ly + other.height;

        if(maxX1 < minX2 || maxX2 < minX1 || maxY1 < minY2 || maxY2 < minY1) return false;
        return true;
    }

    public SubQuadrant getSubQuadrantForVector(Vector v){
        if(v == null || !inBounds(v)){
            return SubQuadrant.OUT_OF_BOUNDS;
        }

        if(v.x < cx){
            if(v.y < cy){
                return SubQuadrant.LX_LY;
            } else {
                return SubQuadrant.LX_UY;
            }
        } else {
            if(v.y < cy){
                return SubQuadrant.UX_LY;
            } else {
                return SubQuadrant.UX_UY;
            }
        }
    }

    public Set<SubQuadrant> getSubQuadrantsForLine(Line l){
        Set<SubQuadrant> quadrants = new HashSet<>();

        if(l == null || !inBounds(l)){
            quadrants.add(SubQuadrant.OUT_OF_BOUNDS);
            return quadrants;
        }

        for(SubQuadrant q : SubQuadrant.values()){
            if(q == SubQuadrant.OUT_OF_BOUNDS){
                break;
            }
            if(getRegionForQuadrant(q).inBounds(l)) quadrants.add(q);
        }

        return quadrants;
    }

    public static RelativeQuadrant getRelativeQuadrant(float originX, float originY, float otherX, float otherY){
        if(otherX < originX){
            if(otherY < originY){
                return RelativeQuadrant.LX_LY;
            } else if(otherY > originY){
                return RelativeQuadrant.LX_UY;
            } else {
                return RelativeQuadrant.LX_CY;
            }
        } else if(otherX > originX){
            if(otherY < originY){
                return RelativeQuadrant.UX_LY;
            } else if(otherY > originY){
                return RelativeQuadrant.UX_UY;
            } else {
                return RelativeQuadrant.UX_CY;
            }
        } else {
            if(otherY < originY){
                return RelativeQuadrant.CX_LY;
            } else if(otherY > originY){
                return RelativeQuadrant.CX_UY;
            } else {
                return RelativeQuadrant.CX_CY;
            }
        }
    }

    public RelativeQuadrant getRelativeQuadrantForVector(Vector v){
        if(v == null){
            return null;
        }

        if(v.x < lx){
            if(v.y < ly){
                return RelativeQuadrant.LX_LY;
            } else if(v.y >= ly + height){
                return RelativeQuadrant.LX_UY;
            } else {
                return RelativeQuadrant.LX_CY;
            }
        } else if(v.x >= lx + width){
            if(v.y < ly){
                return RelativeQuadrant.UX_LY;
            } else if(v.y >= ly + height){
                return RelativeQuadrant.UX_UY;
            } else {
                return RelativeQuadrant.UX_CY;
            }
        } else {
            if(v.y < ly){
                return RelativeQuadrant.CX_LY;
            } else if(v.y >= ly + height){
                return RelativeQuadrant.CX_UY;
            } else {
                return RelativeQuadrant.CX_CY;
            }
        }
    }

    public Vector getClosestPointWithinRegion(Vector v){
        if(v == null){
            return null;
        }

        RelativeQuadrant rQuad = getRelativeQuadrantForVector(v);

        float x = v.x, y = v.y;

        switch (rQuad){
            case LX_LY:
                x = lx;
                y = ly;
                break;
            case LX_CY:
                x = lx;
                break;
            case LX_UY:
                x = lx;
                y = ly + height;
                break;
            case CX_LY:
                y = ly;
                break;
            case CX_UY:
                y = ly + height;
                break;
            case UX_LY:
                x = lx + width;
                y = ly;
                break;
            case UX_CY:
                x = lx + width;
                break;
            case UX_UY:
                x = lx + width;
                y = ly + height;
                break;
            default:
                break;
        }

        return new Vector(x, y);
    }

    public boolean withinRadiusOfPoint(Vector v, float radius){
        return(v != null && radius >= 0 && v.dist(getClosestPointWithinRegion(v)) <= radius);
    }

    public Region getRegionForQuadrant(SubQuadrant q){
        switch(q){
            case LX_LY: return new Region(lx, ly, width/2, height/2);
            case LX_UY: return new Region(lx, cy, width/2, height/2);
            case UX_LY: return new Region(cx, ly, width/2, height/2);
            case UX_UY: return new Region(cx, cy, width/2, height/2);
            default:    return null;
        }
    }

    public Region getRegionForVector(Vector v){
        return getRegionForQuadrant(getSubQuadrantForVector(v));
    }
}
