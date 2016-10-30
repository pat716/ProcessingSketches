package datatypes.water.structure.waterpr;

import datatypes.water.WaterVector;

import java.util.Collection;

/**
 * Created by psweeney on 10/19/16.
 */
public interface WaterVectorQuadtreeNode {
    public int size();
    public WaterVectorQuadtreeNode put(WaterVector v);
    public WaterVector getNearest(float x, float y, WaterVector currentNearest);
    public Collection<WaterVector> getAllWithinRadius(float x, float y, float radius);
}
