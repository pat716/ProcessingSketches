package datatypes.water;

import math.Vector;
import processing.core.PApplet;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by psweeney on 10/19/16.
 */
public class WaterVector extends Vector {
    private float vz;
    private Set<WaterVector> connectedVectors, updatedVectors;

    public WaterVector(float x, float y, float z){
        super(x, y, z);
        vz = 0;
        connectedVectors = new HashSet<>();
        updatedVectors = new HashSet<>();
    }

    public void addConnectedVector(WaterVector v){
        connectedVectors.add(v);
    }

    public void updateVelocity(){
        vz -= z * WaterManager.VZ_SHARE_MULTIPLIER;
        for(WaterVector v : connectedVectors){
            if(v == this || updatedVectors.contains(v)) continue;
            updatedVectors.add(v);
            //v.updatedVectors.add(this);

            float zOffset = v.z - z;
            if(PApplet.abs(zOffset) <= 0) continue;
            float vChangeAmount = (zOffset) * WaterManager.VZ_SHARE_MULTIPLIER;
            vz += vChangeAmount;
        }
        vz *= WaterManager.VZ_UPDATE_MULTIPLIER;
    }

    public void updatePosition(){
        updatedVectors.clear();
        z += vz;
    }
}
