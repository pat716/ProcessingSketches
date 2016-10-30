package datatypes.pixel.physics.fixed;

import datatypes.pixel.Color;
import datatypes.pixel.physics.PhysicsManager;
import datatypes.pixel.physics.PhysicsPixel;
import datatypes.pixel.physics.PhysicsPixelFactory;
import datatypes.pr.Region;
import math.Vector;

import java.util.Map;

/**
 * Created by psweeney on 9/25/16.
 */
public class ClonePixel extends PhysicsPixel {
    private static final float R = 255, G = 255, B = 0;
    private PhysicsPixelType type;
    private boolean active = true;

    public ClonePixel(PhysicsManager manager, int x, int y){
        super(manager, x, y, PhysicsPixelType.CLONE);
        type = null;
    }

    private static boolean cloneable(PhysicsPixelType type){
        if(type == null) return false;

        switch (type){
            case CLONE: return false;
            case EMPTY: return false;
            case WALL: return false;
        }

        return true;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
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
        if(active) {
            if (type == null) {
                for (PhysicsPixel p : getSurroundingPixels().values()) {
                    if (p != null && cloneable(p.getType())) {
                        type = p.getType();
                    }
                }
            } else {
                for (Map.Entry<Vector, PhysicsPixel> entry : getSurroundingPixels().entrySet()) {
                    Vector key = entry.getKey();
                    if (key == null) continue;

                    PhysicsPixelFactory.addPhysicsPixel(getManager(), key.x, key.y, type, false, 0);
                }
            }
        }

        getManager().swapPixels(getIntX(), getIntY(), getIntX(), getIntY());
    }
}
