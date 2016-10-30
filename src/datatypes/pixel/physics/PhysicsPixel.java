package datatypes.pixel.physics;

import datatypes.pixel.Color;
import datatypes.pixel.CustomPixel;
import datatypes.pixel.physics.dynamic.*;
import datatypes.pixel.physics.fixed.*;
import datatypes.pr.Region;
import math.Vector;
import processing.core.PApplet;
import sketches.FallingSand;

import java.util.*;

/**
 * Created by psweeney on 9/24/16.
 */
public abstract class PhysicsPixel extends CustomPixel{
    public enum PhysicsPixelType{
        EMPTY, WALL, GRASS, WATER, FIRE, STEAM, GUNPOWDER, CLONE, CLONE_DEACTIVATE, CLONE_ACTIVATE, FUSE, ICE, CO2_SOLID, CO2_GAS, ASH, METAL, LAVA,
        HEAT, HEAT_GRID, COOL, COOL_GRID, TEMP_GRID_ERASE, URANIUM
    }

    public static final float DEFAULT_STATIC_DENSITY = 2;

    public static String getTextForPixelType(PhysicsPixelType type){
        switch (type){
            case WALL: return "Wall";
            case GRASS: return "Grass";
            case WATER: return "H2O (Liquid)";
            case FIRE: return "Fire";
            case STEAM: return "H2O (Gas)";
            case GUNPOWDER: return "Gunpowder";
            case CLONE: return "Clone";
            case CLONE_DEACTIVATE: return "Clone deactivate";
            case CLONE_ACTIVATE: return "Clone activate";
            case FUSE: return "Fuse";
            case ICE: return "H2O (Solid)";
            case CO2_SOLID: return "CO2 (Solid)";
            case CO2_GAS: return "CO2 (Gas)";
            case ASH: return "Ash";
            case METAL: return "Metal";
            case LAVA: return "Lava";
            case HEAT: return "Heat";
            case HEAT_GRID: return "Heat grid";
            case COOL: return "Cool";
            case COOL_GRID: return "Cool grid";
            case TEMP_GRID_ERASE: return "Temp grid erase";
            case URANIUM: return "Uranium";
            default: return "Erase";
        }
    }

    public static boolean isStaticPixelWithPostUpdate(PhysicsPixel p){
        if(p == null) return false;
        switch (p.getType()){
            case METAL:
            case ICE:
                return true;
            default:
                return false;
        }
    }

    private PhysicsPixelType type;
    private PhysicsManager manager;
    private int lastUpdateFrame = -1;

    public PhysicsPixel(PhysicsManager manager, float x, float y, PhysicsPixelType type){
        super(x, y);
        this.type = type;
        this.manager = manager;
    }

    public int getLastUpdateFrame(){
        return lastUpdateFrame;
    }

    public void setLastUpdateFrame(int lastUpdateFrame) {
        this.lastUpdateFrame = lastUpdateFrame;
    }

    public int getIndex(){
        return getIntY() * manager.getWidth() + getIntX();
    }

    public Vector getVectorForQuadrant(Region.RelativeQuadrant quadrant){
        switch (quadrant){
            case LX_LY:
                return new Vector(getIntX() - 1, getIntY() - 1);
            case LX_CY:
                return new Vector(getIntX() - 1, getIntY());
            case LX_UY:
                return new Vector(getIntX() - 1, getIntY() + 1);
            case CX_LY:
                return new Vector(getIntX(), getIntY() - 1);
            case CX_CY:
                return new Vector(getIntX(), getIntY());
            case CX_UY:
                return new Vector(getIntX(), getIntY() + 1);
            case UX_LY:
                return new Vector(getIntX() + 1, getIntY() - 1);
            case UX_CY:
                return new Vector(getIntX() + 1, getIntY());
            case UX_UY:
                return new Vector(getIntX() + 1, getIntY() + 1);
            default:
                return null;
        }
    }

    public void attemptMoveRandom(Set<PhysicsPixelType> swappable, float chance){
        for(Map.Entry<Vector, PhysicsPixel> entry : getSurroundingPixels().entrySet()){
            Vector key = entry.getKey();
            PhysicsPixel value = entry.getValue();
            if((value == null || value.getType() == PhysicsPixelType.EMPTY || swappable.contains(value.getType())) &&
                    Math.random() < chance){
                getManager().swapPixels(getIntX(), getIntY(), CustomPixel.convertInt(key.x),
                        CustomPixel.convertInt(key.y));
                return;
            }
        }
    }

    public boolean getNearbyEventResult(Vector otherLocation, float initialProbability){
        if(otherLocation == null) return false;

        float probability = initialProbability;
        float distance = otherLocation.dist(new Vector(getIntX(), getIntY()));
        if(distance == 0) return false;
        else if(distance <= 1) return true;

        probability *= 1 - 1/(2 * FallingSand.sqrt2);
        return Math.random() < probability;
    }

    private Comparator<Region.RelativeQuadrant> randomQuadrantComparator = new Comparator<Region.RelativeQuadrant>() {
        @Override
        public int compare(Region.RelativeQuadrant o1, Region.RelativeQuadrant o2) {
            if(o1 == o2) return 0;
            if(o1 == null) return -1;
            if(o2 == null) return 1;
            if(Math.random() < 0.5) return -1;
            return 1;
        }
    };

    public Map<Vector, PhysicsPixel> getSurroundingPixels(){
        //int cx = getIntX(), cy = getIntY();

        Map<Vector, PhysicsPixel> surrounding = new TreeMap<>(Vector.randomVectorComparator);

        for(Region.RelativeQuadrant q : Region.RelativeQuadrant.values()){
            Vector v = getVectorForQuadrant(q);
            surrounding.put(v, getManager().getPixel(getVectorForQuadrant(q)));
        }

        /*
        surrounding.put(Region.RelativeQuadrant.LX_LY, getManager().getPixel(cx - 1, cy - 1));
        surrounding.put(Region.RelativeQuadrant.LX_CY, getManager().getPixel(cx - 1, cy));
        surrounding.put(Region.RelativeQuadrant.LX_UY, getManager().getPixel(cx - 1, cy + 1));
        surrounding.put(Region.RelativeQuadrant.CX_LY, getManager().getPixel(cx, cy - 1));
        surrounding.put(Region.RelativeQuadrant.CX_UY, getManager().getPixel(cx, cy + 1));
        surrounding.put(Region.RelativeQuadrant.UX_LY, getManager().getPixel(cx + 1, cy - 1));
        surrounding.put(Region.RelativeQuadrant.UX_CY, getManager().getPixel(cx + 1, cy));
        surrounding.put(Region.RelativeQuadrant.UX_UY, getManager().getPixel(cx + 1, cy + 1));
        */

        return surrounding;
    }

    public PhysicsPixelType getType() {
        return type;
    }

    public PhysicsManager getManager() {
        return manager;
    }

    public static Color getColorForPixelType(PhysicsManager manager, PhysicsPixelType type){
        switch (type){
            case ASH: return new AshPixel(manager, 0, 0).getBaseColor();
            case CO2_GAS: return new CO2GasPixel(manager, 0, 0).getBaseColor();
            case FIRE: return new FirePixel(manager, 0, 0).getBaseColor();
            case GUNPOWDER: return new GunpowderPixel(manager, 0, 0).getBaseColor();
            case LAVA: return new LavaPixel(manager, 0, 0).getBaseColor();
            case STEAM: return new SteamPixel(manager, 0, 0).getBaseColor();
            case URANIUM: return new UraniumPixel(manager, 0, 0).getBaseColor();
            case WATER: return new WaterPixel(manager, 0, 0).getBaseColor();
            case CLONE:
            case CLONE_DEACTIVATE:
            case CLONE_ACTIVATE: return new ClonePixel(manager, 0, 0).getBaseColor();
            case CO2_SOLID: return new CO2SolidPixel(manager, 0, 0).getBaseColor();
            case FUSE: return new FusePixel(manager, 0, 0).getBaseColor();
            case GRASS: return new GrassPixel(manager, 0, 0, new Vector(0, -1)).getBaseColor();
            case ICE: return new IcePixel(manager, 0, 0).getBaseColor();
            case METAL: return new MetalPixel(manager, 0, 0).getBaseColor();
            case WALL: return new WallPixel(manager, 0, 0).getBaseColor();
            case HEAT: return new Color(255, 175, 125);
            case HEAT_GRID: return TemperatureObject.TempGridObject.getHeatColor();
            case COOL: return new Color(175, 175, 255);
            case COOL_GRID: return TemperatureObject.TempGridObject.getCoolColor();
            default: return new Color(255);
        }
    }

    public abstract Color getBaseColor();
    public abstract Color getPostEffectColor(Color previousColor, float alphaCutoff, float maxAlpha);
    public abstract void update();
}
