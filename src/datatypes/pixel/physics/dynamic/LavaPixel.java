package datatypes.pixel.physics.dynamic;

import datatypes.pixel.Color;
import datatypes.pixel.physics.PhysicsManager;
import datatypes.pixel.physics.PhysicsPixel;
import datatypes.pixel.physics.TemperatureObject;
import datatypes.pixel.physics.fixed.MetalPixel;
import math.Vector;
import processing.core.PApplet;

/**
 * Created by psweeney on 10/2/16.
 */
public class LavaPixel extends DynamicPixel implements TemperatureObject{
    public static final float R = 255, G = 155, B = 55;
    public static final float LAVA_MIN_TEMPERATURE = 300;
    public static final float LAVA_MAX_TEMPERATURE = 500;
    private static final float DENSITY = 4;
    private static final float FLUIDITY = 1f;
    private static final float GRAVITY_MULTIPLIER = 2.0f;
    private static final float RANDOM_VELOCITY_AMOUNT = 0.0016f;
    private static final float MAX_VELOCITY_MAGNITUDE = 0.674f;

    private static final float POST_EFFECT_DEC_AMOUNT = 100;

    private static final float GRID_TEMP_AMOUNT = 0.1f;

    protected float temperature;

    public LavaPixel(PhysicsManager manager, int x, int y, float temperature){
        super(manager, x, y, PhysicsPixelType.LAVA);
        this.temperature = temperature;
    }

    public LavaPixel(PhysicsManager manager, int x, int y){
        this(manager, x, y, LAVA_MAX_TEMPERATURE);
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
        if(t instanceof LavaPixel || t instanceof MetalPixel) return 1; else return 0.25f;
    }

    @Override
    public float getBaseTemperatureChangeFactor() {
        return 1;
    }

    @Override
    public void addToTemperature(TemperatureObject source, float amount) {
        temperature += amount;
        if(temperature < LAVA_MIN_TEMPERATURE)
            getManager().setPixel(getIntX(), getIntY(), new MetalPixel(getManager(), getIntX(), getIntY(),
                    temperature));
        else if(temperature > LAVA_MAX_TEMPERATURE) temperature = LAVA_MAX_TEMPERATURE;
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
    public float getGravityMultiplier() {
        return GRAVITY_MULTIPLIER;
    }

    @Override
    public float getRandomVelocityAmount() {
        return RANDOM_VELOCITY_AMOUNT;
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
        Color effectColor = new Color(R - POST_EFFECT_DEC_AMOUNT, G - POST_EFFECT_DEC_AMOUNT, B -
                POST_EFFECT_DEC_AMOUNT, maxAlpha);
        if(previousColor != null) effectColor = previousColor.add(effectColor, Color.BLEND_MODE.NORMAL);
        return effectColor;
    }

    @Override
    public void updateTemperature(){
        TempGridObject tempGridObject = getManager().getTempGridObject(getIntX(), getIntY());
        addToTemperature(tempGridObject, tempGridObject.getTemperature());

        for(PhysicsPixel p : getSurroundingPixels().values()){
            if(!(p instanceof TemperatureObject)) continue;
            TemperatureObject t = (TemperatureObject) p;
            float avgTemp = (temperature + getTemperature())/2;
            float transferMultiplier = (getTemperatureChangeFactor(t, avgTemp - temperature) +
                    t.getTemperatureChangeFactor(this, temperature - avgTemp))/2;
            if(p instanceof LavaPixel) transferMultiplier = 0.5f;
            float distance = PApplet.dist(getIntX(), getIntY(), p.getIntX(), p.getIntY());
            if(distance > 1) transferMultiplier *= 1/distance;

            addToTemperature(t, (avgTemp - temperature) * transferMultiplier);
            t.addToTemperature(this, (avgTemp - t.getTemperature()) * transferMultiplier);
        }
    }

    @Override
    public void update() {
        super.update();
        updateTemperature();
        float vMagnitude = new Vector(getVx(), getVy()).getMagnitude();
        if(vMagnitude > MAX_VELOCITY_MAGNITUDE){
            setVx(getVx() * (MAX_VELOCITY_MAGNITUDE/vMagnitude));
            setVy(getVy() * (MAX_VELOCITY_MAGNITUDE/vMagnitude));
        }
    }
}
