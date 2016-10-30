package datatypes.shadow.obj;

import datatypes.shadow.ShadowRegion;
import math.Line;
import math.Vector;
import processing.core.PApplet;

import java.util.*;

import static processing.core.PConstants.PI;
import static processing.core.PConstants.TWO_PI;

/**
 * Created by psweeney on 10/27/16.
 */
public class ShadowCastLine implements ShadowCastObject {
    private float x1, y1, x2, y2, objectHeight;
    private Line line;
    public ShadowCastLine(float x1, float y1, float x2, float y2, float objectHeight){
        this.objectHeight = objectHeight;
        if(x1 > x2){
            this.x1 = x2;
            this.y1 = y2;
            this.x2 = x1;
            this.y2 = y1;
        } else {
            this.x1 = x1;
            this.y1 = y1;
            this.x2 = x2;
            this.y2 = y2;
        }

        this.line = new Line(this.x1, this.y1, this.x2, this.y2);
    }

    @Override
    public float getObjectHeight() {
        return objectHeight;
    }

    @Override
    public boolean containsPoint(float x, float y) {
        return (line.ptSegDist(new Vector(x, y)) < 1);
    }

    @Override
    public Vector getShadowCollisionPoint(float sourceX, float sourceY, float sourceZ, float angle) {
        Line.PointSlopeLine linePS = line.getPointSlopeLine();
        Line.PointSlopeLine lineFromOrigin = new Line(sourceX, sourceY, sourceX + PApplet.cos(angle),
                sourceY + PApplet.sin(angle)).getPointSlopeLine();

        Vector intersection = lineFromOrigin.getIntersection(linePS);
        if(intersection == null) return null;
        intersection.z = objectHeight;
        if((PApplet.cos(angle) > 0 && intersection.x < sourceX)
                || (PApplet.cos(angle) < 0 && intersection.x > sourceX)
                || (PApplet.sin(angle) > 0 && intersection.y < sourceY)
                || (PApplet.sin(angle) < 0 && intersection.y > sourceY)) return null;

        if(line.ptSegDist(new Vector(intersection.x, intersection.y)) > 1) return null;
        return intersection;
    }

    @Override
    public void updateShadowRegions(float sourceX, float sourceY, float sourceZ) {

    }

    @Override
    public Set<ShadowRegion> getShadowRegions(float sourceX, float sourceY, float sourceZ) {
        Set<ShadowRegion> shadowRegions = new HashSet<>();
        float v1Angle = PApplet.atan2(sourceY - y1, sourceX - x1) % TWO_PI,
                v2Angle = PApplet.atan2(sourceY - y2, sourceX - x2) % TWO_PI;

        Map.Entry<Float, Vector> point1, point2;

        if((PApplet.abs(v1Angle - v2Angle) > PI && v1Angle < v2Angle)
                || (PApplet.abs(v1Angle - v2Angle) <= PI && v1Angle >= v2Angle)){
            point1 = new AbstractMap.SimpleEntry<Float, Vector>(v2Angle, new Vector(x2, y2));
            point2 = new AbstractMap.SimpleEntry<Float, Vector>(v1Angle, new Vector(x1, y1));
        } else {
            point2 = new AbstractMap.SimpleEntry<Float, Vector>(v2Angle, new Vector(x2, y2));
            point1 = new AbstractMap.SimpleEntry<Float, Vector>(v1Angle, new Vector(x1, y1));
        }

        /*
        if(PApplet.abs(v1Angle - v2Angle) > PI) {
            if (v1Angle < v2Angle) {
                point1 = new AbstractMap.SimpleEntry<Float, Vector>(v2Angle, new Vector(x2, y2));
                point2 = new AbstractMap.SimpleEntry<Float, Vector>(v1Angle, new Vector(x1, y1));
            } else {
                point2 = new AbstractMap.SimpleEntry<Float, Vector>(v2Angle, new Vector(x2, y2));
                point1 = new AbstractMap.SimpleEntry<Float, Vector>(v1Angle, new Vector(x1, y1));
            }
        } else {
            if (v1Angle < v2Angle) {
                point2 = new AbstractMap.SimpleEntry<Float, Vector>(v2Angle, new Vector(x2, y2));
                point1 = new AbstractMap.SimpleEntry<Float, Vector>(v1Angle, new Vector(x1, y1));
            } else {
                point1 = new AbstractMap.SimpleEntry<Float, Vector>(v2Angle, new Vector(x2, y2));
                point2 = new AbstractMap.SimpleEntry<Float, Vector>(v1Angle, new Vector(x1, y1));
            }
        }
        */

        shadowRegions.add(new ShadowRegion(this, sourceX, sourceY, point1, point2));

        return shadowRegions;
    }
}
