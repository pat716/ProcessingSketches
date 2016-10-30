package datatypes.shadow.org;

import datatypes.shadow.obj.ShadowCastObject;
import math.Vector;

import java.util.Collection;

/**
 * Created by psweeney on 10/29/16.
 */
public interface ShadowCastObjectOrganizer {
    void addShadowCastObject(ShadowCastObject obj);
    Collection<ShadowCastObject> getObjectsForLightAngle(Vector lightStart, float angle);
}
