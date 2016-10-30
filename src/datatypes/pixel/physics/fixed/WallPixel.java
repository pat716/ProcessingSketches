package datatypes.pixel.physics.fixed;

import datatypes.pixel.Color;
import datatypes.pixel.CustomPixel;
import datatypes.pixel.physics.PhysicsManager;
import datatypes.pixel.physics.PhysicsPixel;

/**
 * Created by psweeney on 9/24/16.
 */
public class WallPixel extends PhysicsPixel {
    private static final float R = 100, G = 100, B = 100;

    public WallPixel(PhysicsManager manager, float x, float y){
        super(manager, x, y, PhysicsPixelType.WALL);
    }

    @Override
    public Color getBaseColor() {
        return new Color(R, G, B);
    }

    @Override
    public Color getColor() {
        return getBaseColor();
    }

    @Override
    public Color getPostEffectColor(Color previousColor, float alphaCutoff, float maxAlpha) {
        if(previousColor != null) return previousColor;
        return null;
    }

    @Override
    public void update() {

    }
}
