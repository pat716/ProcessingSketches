package datatypes.shadow.obj;

import datatypes.shadow.ShadowRegion;
import math.Line;
import math.Vector;

import java.util.Set;

/**
 * Created by psweeney on 10/27/16.
 */
public interface ShadowCastObject {
    float getObjectHeight();
    boolean containsPoint(float x, float y);
    Vector getShadowCollisionPoint(float sourceX, float sourceY, float sourceZ, float angle);
    void updateShadowRegions(float sourceX, float sourceY, float sourceZ);
    Set<ShadowRegion> getShadowRegions(float sourceX, float sourceY, float sourceZ);
}
