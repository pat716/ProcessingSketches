package datatypes.shadow.org;

import datatypes.shadow.obj.ShadowCastObject;
import math.Vector;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by psweeney on 10/29/16.
 */
public class SimpleShadowCastObjectOrganizer implements ShadowCastObjectOrganizer{
    private Set<ShadowCastObject> objectSet;

    public SimpleShadowCastObjectOrganizer(){
        this.objectSet = new HashSet<>();
    }

    @Override
    public void addShadowCastObject(ShadowCastObject obj) {
        if(obj != null) objectSet.add(obj);
    }

    @Override
    public Collection<ShadowCastObject> getObjectsForLightAngle(Vector lightStart, float angle) {
        return objectSet;
    }
}
