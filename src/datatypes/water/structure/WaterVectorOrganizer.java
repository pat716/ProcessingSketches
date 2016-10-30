package datatypes.water.structure;

import datatypes.water.WaterVector;

import java.util.Collection;

/**
 * Created by psweeney on 10/19/16.
 */
public interface WaterVectorOrganizer {
    public int size();
    public void put(WaterVector v);
    public WaterVector getNearest(float x, float y);
    public Collection<WaterVector> getAllWithinRadius(float x, float y, float radius);
}
