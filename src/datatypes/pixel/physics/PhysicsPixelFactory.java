package datatypes.pixel.physics;

import datatypes.pixel.CustomPixel;
import datatypes.pixel.physics.dynamic.*;
import datatypes.pixel.physics.fixed.*;
import math.Vector;
import processing.core.PApplet;

/**
 * Created by psweeney on 9/24/16.
 */
public class PhysicsPixelFactory {
    //private static float FIRE_ADD_AMT = 0.25f;
    private static float TEMP_CHANGE_AMT = 5;
    public static void addPhysicsPixel(PhysicsManager manager, int x, int y, PhysicsPixel.PhysicsPixelType type,
                                       boolean ignoreSpecial, int skipIndexMode){
        switch (skipIndexMode){
            case 1:
                if((x + y * manager.getWidth() + (y % 2)) % 2 == 0) return;
                break;
            case 2:
                if((x + y * manager.getWidth() + (y % 2)) % 2 == 1) return;
                break;
            case 3:
                if((x + y * manager.getWidth() + (y % 2) * 2) % 4 > 0) return;
            default:
                break;
        }

        if(type == null || type == PhysicsPixel.PhysicsPixelType.EMPTY){
            manager.setPixel(x, y, null);
            return;
        }

        if(!ignoreSpecial) {
            PhysicsPixel p = manager.getPixel(x, y);
            if(p != null){
                if(p instanceof TemperatureObject && type == PhysicsPixel.PhysicsPixelType.HEAT){
                    ((TemperatureObject) p).addToTemperature(new FirePixel(manager, x, y), TEMP_CHANGE_AMT);
                    return;
                } else if(p instanceof TemperatureObject && type == PhysicsPixel.PhysicsPixelType.COOL){
                    ((TemperatureObject) p).addToTemperature(new FirePixel(manager, x, y), -TEMP_CHANGE_AMT);
                    return;
                } else if(p instanceof ClonePixel && (type == PhysicsPixel.PhysicsPixelType.CLONE_ACTIVATE ||
                        type == PhysicsPixel.PhysicsPixelType.CLONE_DEACTIVATE)){
                    if(type == PhysicsPixel.PhysicsPixelType.CLONE_ACTIVATE)
                        ((ClonePixel) p).setActive(true);
                    else ((ClonePixel) p).setActive(false);
                    return;
                } else {
                    return;
                }
            }
        } else {
            if(manager.getPixel(x, y) != null){
                return;
            }
        }

        switch (type){
            case EMPTY:
                manager.setPixel(x, y, null);
                break;
            case WALL:
                manager.setPixel(x, y, new WallPixel(manager, x, y));
                break;
            case GRASS:
                manager.setPixel(x, y, new GrassPixel(manager, x, y, new Vector(0, 1)));
                break;
            case WATER:
                manager.setPixel(x, y, new WaterPixel(manager, x, y));
                break;
            case FIRE:
                manager.setPixel(x, y, new FirePixel(manager, x, y));
                break;
            case STEAM:
                manager.setPixel(x, y, new SteamPixel(manager, x, y));
                break;
            case GUNPOWDER:
                manager.setPixel(x, y, new GunpowderPixel(manager, x, y));
                break;
            case CLONE:
                manager.setPixel(x, y, new ClonePixel(manager, x, y));
                break;
            case FUSE:
                manager.setPixel(x, y, new FusePixel(manager, x, y));
                break;
            case ICE:
                manager.setPixel(x, y, new IcePixel(manager, x, y));
                break;
            case CO2_SOLID:
                manager.setPixel(x, y, new CO2SolidPixel(manager, x, y));
                break;
            case CO2_GAS:
                manager.setPixel(x, y, new CO2GasPixel(manager, x, y));
                break;
            case ASH:
                manager.setPixel(x, y, new AshPixel(manager, x, y));
                break;
            case METAL:
                manager.setPixel(x, y, new MetalPixel(manager, x, y));
                break;
            case LAVA:
                manager.setPixel(x, y, new LavaPixel(manager, x, y));
                break;
            case URANIUM:
                manager.setPixel(x, y, new UraniumPixel(manager, x, y));
                break;
            default:
                manager.setPixel(x, y, null);
                break;
        }
    }

    public static void addPhysicsPixel(PhysicsManager manager, float x, float y, PhysicsPixel.PhysicsPixelType type,
                                       boolean ignoreSpecial, int skipIndexMode){
        addPhysicsPixel(manager, CustomPixel.convertInt(x), CustomPixel.convertInt(y), type, ignoreSpecial,
                skipIndexMode);
    }

    public static void addPhysicsPixel(PhysicsManager manager, int x, int y, PhysicsPixel.PhysicsPixelType type,
                                       int skipIndexMode){
        addPhysicsPixel(manager, x, y, type, false, skipIndexMode);
    }

    public static void addPhysicsPixel(PhysicsManager manager, float x, float y, PhysicsPixel.PhysicsPixelType type,
                                       int skipIndexMode){
        addPhysicsPixel(manager, x, y, type, false, skipIndexMode);
    }

    public static void addPhysicsPixelsInRadius(PhysicsManager manager, float x, float y, float radius,
                                                PhysicsPixel.PhysicsPixelType type, int skipIndexMode){
        for(int i = 0; i < radius; i++){
            for(int j = 0; j < radius; j++){
                if(PApplet.dist(0, 0, i, j) > radius) break;
                addPhysicsPixel(manager, x + i, y + j, type, skipIndexMode);
                if(j != 0) addPhysicsPixel(manager, x + i, y - j, type, skipIndexMode);
                if(i != 0) addPhysicsPixel(manager, x - i, y + j, type, skipIndexMode);
                if(i != 0 && j != 0) addPhysicsPixel(manager, x - i, y - j, type, skipIndexMode);
            }
        }
    }

    public static void addPhysicsPixelsAlongLine(PhysicsManager manager, float startX, float startY, float endX,
                                                 float endY, float radius, PhysicsPixel.PhysicsPixelType type,
                                                 int skipIndexMode){
        float distance = PApplet.dist(startX, startY, endX, endY), xOffset = endX - startX, yOffset = endY - startY;
        for(float i = 0; i < distance; i++){
            addPhysicsPixelsInRadius(manager, startX + (i/distance) * xOffset, startY + (i/distance) * yOffset,
                    radius, type, skipIndexMode);
        }
    }
}
