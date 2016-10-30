package datatypes.pixel.physics.dynamic;

import datatypes.pixel.Color;
import datatypes.pixel.physics.PhysicsManager;
import datatypes.pixel.physics.PhysicsPixel;
import datatypes.pixel.physics.TemperatureObject;
import datatypes.pixel.physics.fixed.GrassPixel;
import datatypes.pixel.physics.fixed.IcePixel;
import math.Vector;
import processing.core.PApplet;

import java.util.Map;

/**
 * Created by psweeney on 9/24/16.
 */
public class WaterPixel extends DynamicPixel implements TemperatureObject{
    private static final float R = 25, G = 50, B = 255;
    private static final float DENSITY = 1;
    private static final float FLUIDITY = 0.99f;
    private static final float GRAVITY_MULTIPLIER = 1;
    private static final float RANDOM_VELOCITY_AMOUNT = 0.1f;
    private static final float FREEZE_CHANCE = 0.01f;

    public static final float INIT_WATER_TEMPERATURE = 0;
    public static final float MIN_WATER_TEMPERATURE = -25;
    public static final float MAX_WATER_TEMPERATURE = 25;
    public static final float TEMP_CHANGE_FACTOR = 0.375f;

    private static final float GRID_TEMP_AMOUNT = 0.2f;

    private float temperature;

    public WaterPixel(PhysicsManager manager, int x, int y, float temperature, float vx, float vy){
        super(manager, x, y, PhysicsPixelType.WATER);
        this.temperature = PApplet.max(MIN_WATER_TEMPERATURE, PApplet.min(MAX_WATER_TEMPERATURE, temperature));
        setVx(vx);
        setVy(vy);
    }

    public WaterPixel(PhysicsManager manager, int x, int y, float temperature){
        this(manager, x, y, temperature, 0, 0);
    }

    public WaterPixel(PhysicsManager manager, int x, int y){
        this(manager, x, y, INIT_WATER_TEMPERATURE);
    }

    @Override
    public float getTemperature() {
        return temperature;
    }

    @Override
    public boolean hasStaticTemp() {
        return false;
    }

    @Override
    public float getTemperatureChangeFactor(TemperatureObject t, float amount) {
        return TEMP_CHANGE_FACTOR;
    }

    @Override
    public float getBaseTemperatureChangeFactor() {
        return TEMP_CHANGE_FACTOR;
    }

    @Override
    public void addToTemperature(TemperatureObject source, float amount) {
        temperature += amount;
        if(temperature <= MIN_WATER_TEMPERATURE)
            getManager().setPixel(getIntX(), getIntY(), new IcePixel(getManager(), getIntX(), getIntY(), temperature));
        else if(temperature >= MAX_WATER_TEMPERATURE)
            getManager().setPixel(getIntX(), getIntY(), new SteamPixel(getManager(), getIntX(), getIntY(),
                    temperature, getVx(), getVy()));
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

    public static Color getBaseWaterColor(){ return new Color(R, G, B); }

    @Override
    public float getFluidity() {
        return FLUIDITY;
    }

    @Override
    public float getGravityMultiplier() {
        return GRAVITY_MULTIPLIER;
    }

    @Override
    public float getRandomVelocityAmount() {
        return RANDOM_VELOCITY_AMOUNT;
    }

    @Override
    public float getDensity() {
        return DENSITY;
    }

    @Override
    public void updateTemperature(){
        TempGridObject tempGridObject = getManager().getTempGridObject(getIntX(), getIntY());
        addToTemperature(tempGridObject, tempGridObject.getTemperature());

        for(PhysicsPixel p : getSurroundingPixels().values()){
            if(!(p instanceof TemperatureObject)) continue;
            TemperatureObject t = (TemperatureObject) p;
            if(t instanceof DynamicPixel) TemperatureObject.swapTemperatures(this, t, 0.5f);
            else TemperatureObject.swapTemperatures(this, t, 1);

            if(temperature > MAX_WATER_TEMPERATURE || temperature < MIN_WATER_TEMPERATURE) break;
        }
    }

    @Override
    public void update() {
        super.update();
        updateTemperature();
        Map<Vector, PhysicsPixel> surrounding = getSurroundingPixels();
        float plantX = 0, plantY = 0, plantCount = 0;

        for(PhysicsPixel p : surrounding.values()){
            if(p == null) continue;
            if(p.getType() == PhysicsPixelType.GRASS){
                Vector v = ((GrassPixel) p).getConversionAngle(this);
                if(v != null){
                    plantCount ++;
                    plantX += v.x;
                    plantY += v.y;
                }
            }

        }

        if(plantCount > 0 && Math.random() < ((plantCount)/8) * GrassPixel.getConversionTypeMultiplier(this)){
            getManager().setPixel(getIntX(), getIntY(), new GrassPixel(getManager(), getIntX(), getIntY(),
                    new Vector(plantX, plantY)));
            return;
        }
    }


}
