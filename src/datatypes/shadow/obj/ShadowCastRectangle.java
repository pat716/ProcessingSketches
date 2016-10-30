package datatypes.shadow.obj;

import datatypes.pr.Region;
import datatypes.shadow.ShadowRegion;
import math.Vector;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by psweeney on 10/28/16.
 */
public class ShadowCastRectangle extends Region implements ShadowCastObject{
    private Region region;
    private float objectHeight;
    private ShadowCastLine leftShadowLine, rightShadowLine, bottomShadowLine, topShadowLine;

    public ShadowCastRectangle(float lx, float ly, float width, float height, float objectHeight){
        super(lx, ly, width, height);
        this.objectHeight = objectHeight;
        leftShadowLine = new ShadowCastLine(lx, ly, lx, ly + height, objectHeight);
        rightShadowLine = new ShadowCastLine(lx + width, ly, lx + width, ly + height, objectHeight);
        bottomShadowLine = new ShadowCastLine(lx, ly, lx + width, ly, objectHeight);
        topShadowLine = new ShadowCastLine(lx, ly + height, lx + width, ly + height, objectHeight);
    }

    @Override
    public float getObjectHeight() {
        return objectHeight;
    }

    @Override
    public boolean containsPoint(float x, float y) {
        return inBounds(x, y);
    }

    private Set<ShadowCastLine> getLinesForQuadrant(float x, float y, float z){
        RelativeQuadrant quadrant = getRelativeQuadrantForVector(new Vector(x, y));
        Set<ShadowCastLine> linesForQuadrant = new HashSet<>();

        if(z <= objectHeight)
            switch (quadrant){
                case LX_LY:
                    linesForQuadrant.add(leftShadowLine);
                    linesForQuadrant.add(bottomShadowLine);
                    break;
                case LX_CY:
                    linesForQuadrant.add(leftShadowLine);
                    break;
                case LX_UY:
                    linesForQuadrant.add(leftShadowLine);
                    linesForQuadrant.add(topShadowLine);
                    break;
                case CX_LY:
                    linesForQuadrant.add(bottomShadowLine);
                    break;
                case CX_UY:
                    linesForQuadrant.add(topShadowLine);
                    break;
                case UX_LY:
                    linesForQuadrant.add(bottomShadowLine);
                    linesForQuadrant.add(rightShadowLine);
                    break;
                case UX_CY:
                    linesForQuadrant.add(rightShadowLine);
                    break;
                case UX_UY:
                    linesForQuadrant.add(rightShadowLine);
                    linesForQuadrant.add(topShadowLine);
                    break;
                default:
                    break;
            }
        else switch (quadrant){
            case LX_LY:
                linesForQuadrant.add(rightShadowLine);
                linesForQuadrant.add(topShadowLine);
                break;
            case LX_CY:
                linesForQuadrant.add(rightShadowLine);
                linesForQuadrant.add(bottomShadowLine);
                linesForQuadrant.add(topShadowLine);
                break;
            case LX_UY:
                linesForQuadrant.add(rightShadowLine);
                linesForQuadrant.add(bottomShadowLine);
                break;
            case CX_LY:
                linesForQuadrant.add(leftShadowLine);
                linesForQuadrant.add(rightShadowLine);
                linesForQuadrant.add(topShadowLine);
                break;
            case CX_UY:
                linesForQuadrant.add(leftShadowLine);
                linesForQuadrant.add(rightShadowLine);
                linesForQuadrant.add(bottomShadowLine);
                break;
            case UX_LY:
                linesForQuadrant.add(leftShadowLine);
                linesForQuadrant.add(topShadowLine);
                break;
            case UX_CY:
                linesForQuadrant.add(leftShadowLine);
                linesForQuadrant.add(bottomShadowLine);
                linesForQuadrant.add(topShadowLine);
                break;
            case UX_UY:
                linesForQuadrant.add(leftShadowLine);
                linesForQuadrant.add(bottomShadowLine);
                break;
            default:
                linesForQuadrant.add(leftShadowLine);
                linesForQuadrant.add(rightShadowLine);
                linesForQuadrant.add(bottomShadowLine);
                linesForQuadrant.add(topShadowLine);
        }

        return linesForQuadrant;
    }

    @Override
    public Vector getShadowCollisionPoint(float sourceX, float sourceY, float sourceZ, float angle) {
        RelativeQuadrant quadrant = getRelativeQuadrantForVector(new Vector(sourceX, sourceY));
        Set<ShadowCastLine> linesForQuadrant = getLinesForQuadrant(sourceX, sourceY, sourceZ);
        if(linesForQuadrant == null || linesForQuadrant.size() <= 0) return null;

        Vector intersection = null;
        for(ShadowCastLine lineToCheck : linesForQuadrant){
            Vector currIntersection = lineToCheck.getShadowCollisionPoint(sourceX, sourceY, sourceZ, angle);
            if(currIntersection == null) continue;
            if(intersection == null || currIntersection.dist(new Vector(sourceX, sourceY)) <
                    intersection.dist(new Vector(sourceX, sourceY)))
                intersection = currIntersection;
        }
        return intersection;
    }

    @Override
    public void updateShadowRegions(float sourceX, float sourceY, float sourceZ) {

    }

    @Override
    public Set<ShadowRegion> getShadowRegions(float sourceX, float sourceY, float sourceZ) {
        Set<ShadowRegion> shadowRegions = new HashSet<>();
        for(ShadowCastLine shadowCastLine : getLinesForQuadrant(sourceX, sourceY, sourceZ)){
            shadowRegions.addAll(shadowCastLine.getShadowRegions(sourceX, sourceY, sourceZ));
        }
        return shadowRegions;
    }
}
