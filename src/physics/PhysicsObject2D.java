package physics;

import drawing.Shape2D;
import processing.core.PApplet;

/**
 * Created by psweeney on 9/13/16.
 */
public interface PhysicsObject2D {
    public static float gravity = 9.8f/60;

    void update();
}
