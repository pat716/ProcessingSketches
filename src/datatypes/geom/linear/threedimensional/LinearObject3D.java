package datatypes.geom.linear.threedimensional;

import datatypes.geom.Vector;

/**
 * Created by psweeney on 3/7/17.
 */
public interface LinearObject3D {
    public Vector getPointForX(float x);
    public Vector getPointForY(float y);
    public Vector getPointForZ(float z);
    public Vector getIntersection(LinearObject3D other);
}
