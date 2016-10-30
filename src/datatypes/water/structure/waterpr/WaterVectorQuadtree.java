package datatypes.water.structure.waterpr;

import datatypes.water.WaterVector;
import datatypes.water.structure.WaterVectorOrganizer;
import processing.core.PApplet;

import java.util.Collection;

/**
 * Created by psweeney on 10/19/16.
 */
public class WaterVectorQuadtree implements WaterVectorOrganizer {
    private float lx, ly, size;
    private WaterVectorQuadtreeNode root;

    public WaterVectorQuadtree(float lx, float ly, float width, float height){
        this.lx = lx;
        this.ly = ly;
        this.size = PApplet.max(width, height);
        root = WaterVectorWhiteNode.getInstance();
    }

    @Override
    public int size(){
        return root.size();
    }

    @Override
    public void put(WaterVector v) {
        if(v == null) return;

        if(root instanceof WaterVectorWhiteNode) root = new WaterVectorBlackNode(lx, ly, size, v);
        else root = root.put(v);
    }

    @Override
    public WaterVector getNearest(float x, float y) {
        return root.getNearest(x, y, null);
    }

    @Override
    public Collection<WaterVector> getAllWithinRadius(float x, float y, float radius) {
        return root.getAllWithinRadius(x, y, radius);
    }
}
