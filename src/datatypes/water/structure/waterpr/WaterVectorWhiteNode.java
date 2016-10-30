package datatypes.water.structure.waterpr;

import datatypes.water.WaterVector;

import java.util.Collection;
import java.util.HashSet;

/**
 * Created by psweeney on 10/19/16.
 */
public class WaterVectorWhiteNode implements WaterVectorQuadtreeNode {
    private static WaterVectorWhiteNode singleton;
    private WaterVectorWhiteNode(){}

    public static WaterVectorWhiteNode getInstance(){
        if(singleton == null) singleton = new WaterVectorWhiteNode();
        return singleton;
    }

    @Override
    public int size() {
        return 0;
    }

    @Override
    public WaterVectorQuadtreeNode put(WaterVector v) {
        return this;
    }

    @Override
    public WaterVector getNearest(float x, float y, WaterVector currentNearest) {
        return currentNearest;
    }

    @Override
    public Collection<WaterVector> getAllWithinRadius(float x, float y, float radius) {
        return new HashSet<>();
    }
}
