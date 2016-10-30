package datatypes.water.structure.waterpr;

import datatypes.water.WaterVector;
import processing.core.PApplet;

import java.util.Collection;
import java.util.HashSet;

/**
 * Created by psweeney on 10/19/16.
 */
public class WaterVectorBlackNode implements WaterVectorQuadtreeNode {
    private float lx, ly, size;
    private WaterVector data;

    public WaterVectorBlackNode(float lx, float ly, float size, WaterVector data){
        this.lx = lx;
        this.ly = ly;
        this.size = size;
        this.data = data;
    }

    @Override
    public int size() {
        if(data != null) return 1;
        return 0;
    }

    @Override
    public WaterVectorQuadtreeNode put(WaterVector v) {
        if(v == null) return this;
        if(data == null){
            data = v;
            return this;
        }
        return new WaterVectorGreyNode(lx, ly, size, data, v);
    }

    @Override
    public WaterVector getNearest(float x, float y, WaterVector currentNearest) {
        if(data == null) return currentNearest;
        if(currentNearest == null) return data;
        if(PApplet.dist(x, y, data.x, data.y) < PApplet.dist(x, y, currentNearest.x, currentNearest.y)) return data;
        return currentNearest;
    }

    @Override
    public Collection<WaterVector> getAllWithinRadius(float x, float y, float radius) {
        Collection<WaterVector> vectors = new HashSet<>();
        if(data != null && PApplet.dist(x, y, data.x, data.y) <= radius) vectors.add(data);
        return vectors;
    }
}
