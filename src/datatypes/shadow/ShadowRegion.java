package datatypes.shadow;

import datatypes.shadow.obj.ShadowCastObject;
import math.Line;
import math.Vector;
import processing.core.PApplet;

import java.util.Comparator;
import java.util.Iterator;
import java.util.Map;
import java.util.SortedSet;

import static processing.core.PConstants.PI;
import static processing.core.PConstants.TWO_PI;

/**
 * Created by psweeney on 10/29/16.
 */
public class ShadowRegion {
    public static Comparator<Map.Entry<Float, Vector>> entryComparator = new Comparator<Map.Entry<Float, Vector>>() {
        @Override
        public int compare(Map.Entry<Float, Vector> o1, Map.Entry<Float, Vector> o2) {
            if(o1 == o2) return 0;
            if(o1 == null) return 1;
            if(o2 == null) return -1;

            if(o1.getKey() < o2.getKey()) return -1;
            else if(o1.getKey() > o2.getKey()) return 1;
            return 0;
        }
    };

    private boolean reverse;
    private float sourceX, sourceY;
    private ShadowCastObject castObject;
    private Map.Entry<Float, Vector> startPoint, endPoint;

    public ShadowRegion(ShadowCastObject castObject, float sourceX, float sourceY, Map.Entry<Float, Vector> startPoint,
                        Map.Entry<Float, Vector> endPoint){
        this.castObject = castObject;
        this.sourceX = sourceX;
        this.sourceY = sourceY;
        this.startPoint = startPoint;
        this.endPoint = endPoint;

        if(startPoint.getKey() % TWO_PI > endPoint.getKey() % TWO_PI){
            this.startPoint = endPoint;
            this.endPoint = startPoint;
        }

        if(PApplet.abs(startPoint.getKey() - endPoint.getKey()) > PI) reverse = true;
        else reverse = false;
    }

    public boolean containsAngle(float angle){
        if(reverse){
            return angle % TWO_PI >= endPoint.getKey() || angle % TWO_PI <= startPoint.getKey();
        }
        return (angle % TWO_PI >= startPoint.getKey() && angle % TWO_PI <= endPoint.getKey());
    }

    public Vector getPointForAngle(float angle){
        float modAngle = angle % TWO_PI;
        if(!containsAngle(modAngle)) return null;

        if(startPoint == null || endPoint == null) return null;

        /*
        if(angle == 0){
            if(sourceX > )
        } else if(angle == PI/2){

        } else if(angle == PI){

        } else if(angle == 3 * (PI/2)){

        } */

        Line pointsConnectingLine = new Line(startPoint.getValue().x, startPoint.getValue().y,
                endPoint.getValue().x, endPoint.getValue().y);
        Line angleLine = new Line(sourceX, sourceY, sourceX + PApplet.cos(modAngle), sourceY + PApplet.sin(modAngle));

        Vector collision = pointsConnectingLine.getPointSlopeLine().getIntersection(angleLine.getPointSlopeLine());
        if(collision == null) return null;
        collision.z = castObject.getObjectHeight();
        if((collision.x > sourceX && PApplet.cos(modAngle) < 0)
                || (collision.x < sourceX && PApplet.cos(modAngle) > 0)
                || (collision.y > sourceY && PApplet.sin(modAngle) < 0)
                || (collision.y < sourceY && PApplet.sin(modAngle) > 0)) return collision;
        return null;
    }
}
