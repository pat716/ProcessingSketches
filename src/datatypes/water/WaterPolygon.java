package datatypes.water;

import math.Vector;
import processing.core.PApplet;
import processing.core.PGraphics;
import processing.core.PVector;

/**
 * Created by psweeney on 10/19/16.
 */
public class WaterPolygon {
    private WaterVector v1, v2, v3;
    private Vector center;
    private boolean pointsTowardBottom;

    public WaterPolygon(WaterVector v1, WaterVector v2, WaterVector v3){
        float minX = PApplet.min(v1.x, PApplet.min(v2.x, v3.x));
        float maxX = PApplet.max(v1.x, PApplet.max(v2.x, v3.x));

        if(v1.x == minX){
            if(v2.x == maxX){
                this.v1 = v1;
                this.v2 = v3;
                this.v3 = v2;
            } else {
                this.v1 = v1;
                this.v2 = v2;
                this.v3 = v3;
            }
        } else if(v1.x == maxX){
            if(v2.x == minX){
                this.v1 = v2;
                this.v2 = v3;
                this.v3 = v1;
            } else {
                this.v1 = v3;
                this.v2 = v2;
                this.v3 = v1;
            }
        } else {
            if(v2.x == minX){
                this.v1 = v2;
                this.v2 = v1;
                this.v3 = v3;
            } else {
                this.v1 = v3;
                this.v2 = v1;
                this.v3 = v2;
            }
        }

        if(this.v1.y > this.v2.y) pointsTowardBottom = false; else pointsTowardBottom = true;

        center = new Vector((v1.x + v2.x + v3.x) / 3f, (v1.y + v2.y + v3.y) / 3f);
        v1.addConnectedVector(v2);
        v1.addConnectedVector(v3);
        v2.addConnectedVector(v1);
        v2.addConnectedVector(v3);
        v3.addConnectedVector(v1);
        v3.addConnectedVector(v2);

    }

    public WaterVector getV1() {
        return v1;
    }

    public WaterVector getV2() {
        return v2;
    }

    public WaterVector getV3() {
        return v3;
    }

    public Vector getCenter() {
        return center;
    }

    public boolean pointsTowardBottom() {
        return pointsTowardBottom;
    }

    public boolean isValid(){
        if(v1 == null || v2 == null || v3 == null || center == null) return false;
        return true;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj == null || !(obj instanceof WaterPolygon)) return false;
        if(this == obj) return true;
        WaterPolygon wObj = (WaterPolygon) obj;

        if(v1 == wObj.v1){
            if(v2 == wObj.v2){
                if(v3 == wObj.v3){
                    return true;
                }
            } else if(v2 == wObj.v3){
                if(v3 == wObj.v2){
                    return true;
                }
            }
        } else if(v1 == wObj.v2){
            if(v2 == wObj.v3){
                if(v3 == wObj.v1){
                    return true;
                }
            } else if(v2 == wObj.v1){
                if(v3 == wObj.v3){
                    return true;
                }
            }
        } else if(v1 == wObj.v3){
            if(v2 == wObj.v1){
                if(v3 == wObj.v2){
                    return true;
                }
            } else if(v2 == wObj.v2){
                if(v3 == wObj.v1){
                    return true;
                }
            }
        }
        return false;
    }

    private PVector getNormalVector(){
        /*
        PVector v2mv1 = new PVector(v2.x - v1.x, v2.y - v1.y, v2.z - v1.z);
        PVector v3mv1 = new PVector(v3.x - v1.x, v3.y - v1.y, v3.z - v1.z);
        */

        PVector v2mv1 = v2.mySubtract(v1);
        PVector v3mv1 = v3.mySubtract(v1);

        PVector normal = v2mv1.cross(v3mv1);
        if(normal.z < 0) normal = normal.mult(-1);
        return normal.normalize();
    }

    public int getFillColor(PGraphics canvas){
        PVector normal = getNormalVector();
        float normalDist = normal.dist(WaterManager.SCREEN_NORMAL);
        float ratio = PApplet.max(0, PApplet.min(1, normalDist/4));
        return canvas.color((1 - ratio) * 255);
    }
}
