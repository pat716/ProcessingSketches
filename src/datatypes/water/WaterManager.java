package datatypes.water;

import datatypes.water.structure.WaterVectorOrganizer;
import datatypes.water.structure.waterpr.WaterVectorQuadtree;
import math.Vector;
import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PGraphics;
import processing.core.PVector;
import sketches.WaterSketch;

import java.util.*;

/**
 * Created by psweeney on 10/19/16.
 */
public class WaterManager {
    public static final float VZ_SHARE_MULTIPLIER = 0.001f;
    public static final float VZ_UPDATE_MULTIPLIER = 0.999f;
    public static final PVector SCREEN_NORMAL = new Vector(0, 0, -1);

    private float width;
    private float height;
    private float polygonEdgeLength, triangleHeight;

    private WaterVectorOrganizer vectorOrganizer;
    private Set<WaterVector> vectors;
    private Set<WaterPolygon> polygons;

    public WaterManager(float width, float height, float polygonEdgeLength){
        this.width = width;
        this.height = height;
        this.polygonEdgeLength = polygonEdgeLength;
        triangleHeight = (polygonEdgeLength/2) * PApplet.sqrt(3);

        this.vectors = new HashSet<>();
        this.polygons = new HashSet<>();
        vectorOrganizer = new WaterVectorQuadtree(-polygonEdgeLength, -triangleHeight,
                width + polygonEdgeLength * 2, height + triangleHeight * 2);

        initialize();

        System.out.println("Vectors size: " + vectors.size());
        System.out.println("Organizer size: " + vectorOrganizer.size());
    }

    public Set<WaterPolygon> getPolygons() {
        return polygons;
    }

    private void initialize(){
        boolean yOffsetModifier = false;

        for(float y = -triangleHeight; y <= height + triangleHeight; y += triangleHeight){
            for(float x = -polygonEdgeLength; x <= width + polygonEdgeLength; x += polygonEdgeLength) {
                WaterVector waterVector;
                if (yOffsetModifier) waterVector = new WaterVector(x + polygonEdgeLength / 2, y, 0);
                else waterVector = new WaterVector(x, y, 0);

                vectors.add(waterVector);
                vectorOrganizer.put(waterVector);
            }

            yOffsetModifier = !yOffsetModifier;
        }

        Comparator<WaterPolygon> polygonComparator = new Comparator<WaterPolygon>() {
            @Override
            public int compare(WaterPolygon o1, WaterPolygon o2) {
                if(o1 == o2) return 0;
                if(o1 == null) return 1;
                if(o2 == null) return -1;
                if(o1.equals(o2)) return 0;

                Vector o1c = o1.getCenter(), o2c = o2.getCenter();
                if(o1c.x < o2c.x) return -1;
                else if(o1c.x > o2c.x) return 1;

                if(o1c.y < o2c.y) return -1;
                else if(o1c.y > o2c.y) return 1;

                return 0;
            }
        };

        SortedSet<WaterPolygon> tempPolygons = new TreeSet<>(polygonComparator);

        for(WaterVector v1 : vectors){
            Collection<WaterVector> nearbyVectors = vectorOrganizer.getAllWithinRadius(v1.x, v1.y,
                    polygonEdgeLength * WaterSketch.SQRT_2);
            nearbyVectors.remove(v1);
            for(WaterVector v2 : nearbyVectors){
                for(WaterVector v3 : nearbyVectors){
                    if(v2 == v3) continue;

                    if(v2.dist(v3) <= polygonEdgeLength * WaterSketch.SQRT_2){
                        WaterPolygon p = new WaterPolygon(v1, v2, v3);
                        if(p.isValid()) tempPolygons.add(p);
                    }
                }
            }
        }

        polygons.addAll(tempPolygons);
    }

    private void moveZPixels(float x, float y, float radius, float pushAmount){
        Collection<WaterVector> vectorsWithinRadius = vectorOrganizer.getAllWithinRadius(x, y, radius);
        for(WaterVector v : vectorsWithinRadius){
            float vDist = PApplet.dist(x, y, v.x, v.y);
            float vRatio = vDist/radius;
            v.z += pushAmount * (1 - vRatio);
        }
    }

    public void push(float x, float y, float radius, float amount){
        moveZPixels(x, y, radius, amount);
    }

    public void pull(float x, float y, float radius, float amount){
        moveZPixels(x, y, radius, -amount);
    }

    public void update(){
        for(WaterVector v : vectors){
            v.updateVelocity();
        }

        for(WaterVector v : vectors){
            v.updatePosition();
        }

        /*
        canvas.blendMode(PConstants.ADD);
        canvas.beginShape(PConstants.TRIANGLES);
        for(WaterPolygon p : polygons){
            canvas.fill(p.getFillColor(canvas));
            WaterVector v1 = p.getV1(), v2 = p.getV2(), v3 = p.getV3();
            canvas.vertex(v1.x, v1.y);
            canvas.vertex(v2.x, v2.y);
            canvas.vertex(v3.x, v3.y);
        }
        canvas.endShape(PConstants.TRIANGLES);
        */
    }
}
