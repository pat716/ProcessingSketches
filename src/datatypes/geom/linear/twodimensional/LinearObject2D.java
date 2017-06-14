package datatypes.geom.linear.twodimensional;

import datatypes.geom.Vector;
import display.Color;
import display.DrawOptions;
import processing.core.PGraphics;

/**
 * Created by psweeney on 2/12/17.
 */
public interface LinearObject2D {
    public Vector getPointForX(float x);
    public Vector getPointForY(float y);
    public Vector getIntersection(LinearObject2D other);
    public Vector getClosestPoint(Vector v);
    public void draw(PGraphics canvas, Color.ColorMode colorMode, DrawOptions drawOptions);
}
