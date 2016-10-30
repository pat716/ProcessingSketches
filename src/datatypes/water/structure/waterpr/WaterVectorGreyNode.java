package datatypes.water.structure.waterpr;

import datatypes.pr.Region;
import datatypes.water.WaterVector;
import math.Vector;
import processing.core.PApplet;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

/**
 * Created by psweeney on 10/19/16.
 */
public class WaterVectorGreyNode implements WaterVectorQuadtreeNode {
    private float lx, ly, size;
    private Map<Region.SubQuadrant, WaterVectorQuadtreeNode> subNodes;

    public WaterVectorGreyNode(float lx, float ly, float size, WaterVector v1, WaterVector v2){
        this.lx = lx;
        this.ly = ly;
        this.size = size;

        subNodes = new HashMap<>();
        subNodes.put(Region.SubQuadrant.LX_LY, WaterVectorWhiteNode.getInstance());
        subNodes.put(Region.SubQuadrant.LX_UY, WaterVectorWhiteNode.getInstance());
        subNodes.put(Region.SubQuadrant.UX_LY, WaterVectorWhiteNode.getInstance());
        subNodes.put(Region.SubQuadrant.UX_UY, WaterVectorWhiteNode.getInstance());

        put(v1);
        put(v2);
    }

    @Override
    public int size() {
        int total = 0;
        for(WaterVectorQuadtreeNode node : subNodes.values()){
            total += node.size();
        }
        return total;
    }

    @Override
    public WaterVectorQuadtreeNode put(WaterVector v) {
        if(v == null || v.x < lx || v.x >= lx + size || v.y < ly || v.y >= ly + size) return this;

        Region.SubQuadrant destQuadrant;
        WaterVectorQuadtreeNode destNode;

        if(v.x < lx + size/2){
            if(v.y < ly + size/2){
                destQuadrant = Region.SubQuadrant.LX_LY;
            } else {
                destQuadrant = Region.SubQuadrant.LX_UY;
            }
        } else {
            if(v.y < ly + size/2){
                destQuadrant = Region.SubQuadrant.UX_LY;
            } else {
                destQuadrant = Region.SubQuadrant.UX_UY;
            }
        }

        destNode = subNodes.get(destQuadrant);

        if(destNode instanceof WaterVectorWhiteNode){
            float nlx = lx, nly = ly, nsize = size/2;
            switch (destQuadrant){
                case LX_LY:
                    nlx = lx;
                    nly = ly;
                    break;
                case LX_UY:
                    nlx = lx;
                    nly = ly + size/2;
                    break;
                case UX_LY:
                    nlx = lx + size/2;
                    nly = ly;
                    break;
                case UX_UY:
                    nlx = lx + size/2;
                    nly = ly + size/2;
                    break;
            }
            subNodes.put(destQuadrant, new WaterVectorBlackNode(nlx, nly, nsize, v));
        } else {
            subNodes.put(destQuadrant, destNode.put(v));
        }

        return this;
    }

    @Override
    public WaterVector getNearest(float x, float y, WaterVector currentNearest) {
        WaterVector tempNearest = currentNearest;

        Region region = new Region(lx, ly, size, size);
        Vector closestPoint = region.getClosestPointWithinRegion(new Vector(x, y));
        if(currentNearest != null && PApplet.dist(x, y, currentNearest.x, currentNearest.y) <
                PApplet.dist(x, y, closestPoint.x, closestPoint.y)) return currentNearest;
        for(WaterVectorQuadtreeNode node : subNodes.values()){
            tempNearest = node.getNearest(x, y, tempNearest);
        }
        return tempNearest;
    }

    @Override
    public Collection<WaterVector> getAllWithinRadius(float x, float y, float radius) {
        Collection<WaterVector> vectors = new HashSet<>();

        Region region = new Region(lx, ly, size, size);
        Vector closestPoint = region.getClosestPointWithinRegion(new Vector(x, y));

        if(PApplet.dist(x, y, closestPoint.x, closestPoint.y) > radius) return vectors;

        for(WaterVectorQuadtreeNode node : subNodes.values()){
            vectors.addAll(node.getAllWithinRadius(x, y, radius));
        }

        return vectors;
    }
}
