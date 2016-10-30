package datatypes.pixel.physics.dynamic;

import datatypes.pixel.Color;
import datatypes.pixel.physics.PhysicsManager;

/**
 * Created by psweeney on 9/27/16.
 */
public class AshPixel extends DynamicPixel {
    private static final float R = 175, G = 175, B = 175;
    private static final float DENSITY = 0.05f;
    private static final float FLUIDITY = 0.05f;
    private static final float RANDOM_VELOCITY_AMOUNT = 0.4f;
    private static final float GRAVITY_MULTIPLIER = 3f;

    public AshPixel(PhysicsManager manager, int x, int y){
        super(manager, x, y, PhysicsPixelType.ASH);
    }

    @Override
    public float getDensity() {
        return DENSITY;
    }

    @Override
    public float getFluidity() {
        return FLUIDITY;
    }

    @Override
    public float getRandomVelocityAmount() {
        return RANDOM_VELOCITY_AMOUNT;
    }

    @Override
    public float getGravityMultiplier() {
        return GRAVITY_MULTIPLIER;
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
}
