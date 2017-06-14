package datatypes.geom.container.twodimensional;

import datatypes.geom.Vector;
import datatypes.geom.linear.twodimensional.LineSegment2D;
import datatypes.geom.linear.twodimensional.LinearObject2D;
import display.Color;
import display.DrawOptions;
import processing.core.PGraphics;

/**
 * Created by psweeney on 2/16/17.
 */
public interface ContainerObject2D {
    public boolean inBounds(Vector v);
    public float getArea();
    public LineSegment2D getContainedSegment(LinearObject2D linearObject2D);
    public void draw(PGraphics canvas, Color.ColorMode colorMode, DrawOptions drawOptions);
}
