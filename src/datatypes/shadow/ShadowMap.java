package datatypes.shadow;

import datatypes.shadow.obj.ShadowCastObject;
import datatypes.shadow.org.ShadowCastObjectOrganizer;
import datatypes.shadow.org.SimpleShadowCastObjectOrganizer;
import math.Line;
import math.Vector;
import processing.core.PApplet;
import processing.core.PGraphics;
import sketches.ShadowTesting;

import java.util.*;

import static processing.core.PApplet.atan2;
import static processing.core.PApplet.radians;
import static processing.core.PConstants.PI;
import static processing.core.PConstants.TWO_PI;

/**
 * Created by psweeney on 10/27/16.
 */
public class ShadowMap {
    private Vector lightOrigin;
    private Set<ShadowCastObject> shadowCastObjects;
    //private ShadowCastObjectOrganizer shadowCastObjectOrganizer;
    private Set<ShadowRegion> shadowRegions;

    public ShadowMap(Vector lightOrigin){
        this.lightOrigin = lightOrigin;
        //shadowCastObjects = new SimpleShadowCastObjectOrganizer();
        shadowRegions = new HashSet<>();
        shadowCastObjects = new HashSet<>();
    }

    public void addShadowCastObject(ShadowCastObject object){
        if(object != null){
            shadowCastObjects.add(object);
        }
    }

    public void update(){
        shadowRegions.clear();
        for(ShadowCastObject object : shadowCastObjects){
            Set<ShadowRegion> regions = object.getShadowRegions(lightOrigin.x, lightOrigin.y, lightOrigin.z);
            if(regions == null || regions.size() <= 0) continue;
            shadowRegions.addAll(regions);
        }
    }

    public static Vector getShadowEndPointForShadowStartPoint(Vector lightOrigin, Vector shadowStartPoint){
        if(lightOrigin == null || shadowStartPoint == null || lightOrigin.z <= shadowStartPoint.z) return null;
        float angleFromSource = atan2(shadowStartPoint.y - lightOrigin.y, shadowStartPoint.x - lightOrigin.x);
        float startDist = lightOrigin.dist(shadowStartPoint);
        float shadowLength = startDist * (shadowStartPoint.z/(lightOrigin.z - shadowStartPoint.z));
        return new Vector(shadowStartPoint.x + PApplet.cos(angleFromSource) * shadowLength,
                shadowStartPoint.y + PApplet.sin(angleFromSource) * shadowLength);
    }

    public float getLightMultiplier(float x, float y, float lightHeight, float edgeFadeStart){
        float pointDist = lightOrigin.dist(new Vector(x, y));
        float angleFromSource = atan2(lightOrigin.y - y, lightOrigin.x - x);

        for(ShadowCastObject object : shadowCastObjects){
            if(object.containsPoint(x, y)){
                if(object.getObjectHeight() >= lightHeight) return 0;
                float zOffset = PApplet.abs(lightHeight - object.getObjectHeight());
                float lightAngle = PApplet.atan(zOffset/pointDist);
                return PApplet.max(0, PApplet.min(1, 1 - lightAngle/PI));
            }
        }

        float lightAngle = PApplet.atan(lightHeight/pointDist);
        float lightAmount = PApplet.max(0, PApplet.min(1, 1 - lightAngle/PI));

        Map<Vector, Vector> shadowLines = new HashMap<>();

        /*
        Vector shadowStartPoint = null, shadowEndPoint = null;
        float shadowStartDist = -1, shadowEndDist = -1;
        */

        for(ShadowRegion shadowRegion : shadowRegions){
            if(shadowRegion.containsAngle(angleFromSource)){
                Vector shadowStartPoint = shadowRegion.getPointForAngle(angleFromSource);

                if(shadowStartPoint == null) continue;
                Vector shadowEndPoint = getShadowEndPointForShadowStartPoint(lightOrigin, shadowStartPoint);

                shadowLines.put(shadowStartPoint, shadowEndPoint);
            }
        }

        if(shadowLines.size() <= 0) return lightAmount;

        float returnLightAmount = lightAmount;
        for(Map.Entry<Vector, Vector> entry : shadowLines.entrySet()){
            Vector start = entry.getKey(), end = entry.getValue();
            float startDist = lightOrigin.dist(start);
            if(startDist > pointDist) continue;
            if(end == null) return 0;
            float endDist = lightOrigin.dist(end);
            if(endDist >= pointDist){
                float progressRatio = PApplet.max(0, PApplet.min(1, (pointDist - startDist)/(endDist - startDist)));
                if(progressRatio <= edgeFadeStart) return 0;
                returnLightAmount = PApplet.min(returnLightAmount,
                        lightAmount * ((progressRatio - edgeFadeStart)/(1 - edgeFadeStart)));
            }
        }

        return returnLightAmount;
        /*
        if(shadowStartPoint == null) return true;
        shadowStartDist = shadowStartPoint.dist(new Vector(lightOrigin.x, lightOrigin.y, lightHeight));

        if(shadowStartPoint.z < lightHeight){
            float shadowLength = shadowStartDist * (shadowStartPoint.z/(lightHeight - shadowStartPoint.z));
            shadowEndPoint = new Vector(shadowStartPoint.x + PApplet.cos(angleFromSource) * shadowLength,
                    shadowStartPoint.y + PApplet.sin(angleFromSource) * shadowLength);
            shadowEndDist = shadowStartDist + shadowLength;
        }


        if(shadowEndPoint == null){
            return (shadowStartDist < 0 || shadowStartDist > pointDist);
        } else {
            return pointDist < shadowStartDist || pointDist > shadowEndDist;
        } */
    }

}
