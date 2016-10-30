package datatypes.pixel.physics.dynamic;

import datatypes.pixel.Color;
import datatypes.pixel.physics.PhysicsManager;
import datatypes.pixel.physics.PhysicsPixel;
import datatypes.pixel.physics.TemperatureObject;
import datatypes.pixel.physics.fixed.FusePixel;
import datatypes.pixel.physics.fixed.MetalPixel;
import math.Vector;
import processing.core.PApplet;
import processing.core.PVector;

import java.util.Map;

import static datatypes.pixel.physics.PhysicsPixel.PhysicsPixelType.FIRE;
import static datatypes.pixel.physics.fixed.FusePixel.FUSE_IGNITE_TEMP;

/**
 * Created by psweeney on 9/25/16.
 */

public class FirePixel extends DynamicPixel implements TemperatureObject{
    private static final float R = 255, G = 100, B = 15;
    private static final float DENSITY = 0.5f;
    private static final float FLUIDITY = 0.85f;
    private static final float GRAVITY_MULTIPLIER = -2.5f;
    private static final float RANDOM_VELOCITY_AMOUNT = 0.375f;
    private static final float MIN_FIRE_TEMPERATURE = 50;
    public static final float MAX_FIRE_TEMPERATURE = 100;
    private static final float TEMP_RATIO_POW = 0.5f;

    private static final float POST_EFFECT_DEC_AMOUNT = 70;

    private static final float NEARBY_INCREASE_MULTIPLIER = 0.125f;
    private static final float UPDATE_DECREASE_MULTIPLIER = 8.5f;

    public float temperature;
    private float selfTempMultiplier;
    private int maintainTempCountdown;

    public FirePixel(PhysicsManager manager, int x, int y){
        super(manager, x, y, FIRE);
        //temperature = (float) Math.random() * (MAX_FIRE_TEMPERATURE - MIN_FIRE_TEMPERATURE) + MIN_FIRE_TEMPERATURE;
        temperature = MAX_FIRE_TEMPERATURE;
        maintainTempCountdown = 0;
        selfTempMultiplier = (float) Math.random() * 1.875f + 0.625f;
    }

    public FirePixel(PhysicsManager manager, int x, int y, float vx, float vy, int maintainTempCountdown){
        this(manager, x, y);
        temperature = MAX_FIRE_TEMPERATURE;
        this.maintainTempCountdown = maintainTempCountdown;
        setVx(vx);
        setVy(vy);
    }

    public static boolean canIgnite(Object obj){
        if(obj instanceof TempGridObject) return true;
        if(obj != null && obj instanceof PhysicsPixel)
        switch (((PhysicsPixel) obj).getType()){
            case FIRE: return true;
            case LAVA: return true;
            case URANIUM: return true;
        }

        return false;
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
        return PApplet.min(1, PApplet.max(0, 0.125f * selfTempMultiplier));
        //return PApplet.min(0.5f, PApplet.max(0, 0.125f * selfTempMultiplier));
        //return -1f;
        //if(amount > 0) return 1/(1 + PApplet.abs(amount)); else return 1;
    }

    @Override
    public float getBaseTemperatureChangeFactor() {
        return -1;
    }

    @Override
    public void addToTemperature(TemperatureObject source, float amount) {
        temperature += amount;
        if(temperature > MAX_FIRE_TEMPERATURE) temperature = MAX_FIRE_TEMPERATURE;
        else if(temperature < MIN_FIRE_TEMPERATURE)
            getManager().setPixel(getIntX(), getIntY(), null);
    }

    public float getTempRatio(){
        return PApplet.max(0, PApplet.min(1, PApplet.pow((temperature - MIN_FIRE_TEMPERATURE) / (MAX_FIRE_TEMPERATURE -
                MIN_FIRE_TEMPERATURE), TEMP_RATIO_POW)));
    }

    public static Color getFireColorForRatio(float ratio){
        if(ratio < 0) return null;
        return new Color(R + R  * (ratio - 0.5f), G + G  * (ratio - 0.5f), B + B  * (ratio - 0.5f), ratio * 255);
        //return new Color(R * ratio, G * ratio, B * ratio, ratio * 255);
    }

    public Color getBaseColor() { return new Color(R, G, B); }

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
    public Color getColor() {
        float tempRatio = 2 * getTempRatio();
        return getFireColorForRatio(tempRatio);
    }

    @Override
    public Color getPostEffectColor(Color previousColor, float alphaCutoff, float maxAlpha) {
        float alpha = getTempRatio() * maxAlpha;
        Color effectColor = new Color(R - POST_EFFECT_DEC_AMOUNT, G - POST_EFFECT_DEC_AMOUNT, B -
                POST_EFFECT_DEC_AMOUNT, alpha);
        if(previousColor != null) effectColor = previousColor.add(effectColor, Color.BLEND_MODE.NORMAL);
        return effectColor;
    }

    private static float getTemperatureIncreaseAmount(PhysicsPixel p){
        if(p == null) return (float) Math.random() * 6 * NEARBY_INCREASE_MULTIPLIER;

        switch (p.getType()){
            case FIRE: return (float) Math.random() * 7f * NEARBY_INCREASE_MULTIPLIER;
            case LAVA: return (float) Math.random() * -1f * NEARBY_INCREASE_MULTIPLIER;
            default: return (float) Math.random() * -7f * NEARBY_INCREASE_MULTIPLIER;
        }
    }

    @Override
    public void updateTemperature(){
        TempGridObject tempGridObject = getManager().getTempGridObject(getIntX(), getIntY());
        addToTemperature(tempGridObject, tempGridObject.getTemperature());

        if(maintainTempCountdown > 0){
            maintainTempCountdown--;
            temperature = MAX_FIRE_TEMPERATURE;
            return;
        }

        float maxPlusAmount = temperature - MIN_FIRE_TEMPERATURE;
        //maxPlusAmount *= 1/(1 + 0.25 * Math.random() * PApplet.dist(0, 0, getVx(), getVy()) * selfTempMultiplier);
        maxPlusAmount *= 1/(1 + 0.25 * Math.random() * selfTempMultiplier);
        temperature = MIN_FIRE_TEMPERATURE + maxPlusAmount;
        temperature -= Math.random() * UPDATE_DECREASE_MULTIPLIER * selfTempMultiplier;
        Map<Vector, PhysicsPixel> surrounding = getSurroundingPixels();

        for(Map.Entry<Vector, PhysicsPixel> entry : surrounding.entrySet()){
            temperature += getTemperatureIncreaseAmount(entry.getValue());
        }
    }

    private void inflameNearby(){
        for(Map.Entry<Vector, PhysicsPixel> entry : getSurroundingPixels().entrySet()){
            PhysicsPixel p = entry.getValue();
            if(p != null && p instanceof TemperatureObject){
                if(p instanceof FirePixel){
                    FirePixel f = (FirePixel) p;
                    float avgTemp = (temperature + f.getTemperature())/2;
                    f.temperature = f.temperature * 0.25f + avgTemp * 0.75f;
                    temperature = temperature * 0.25f + avgTemp * 0.75f;
                    continue;
                } else if(p instanceof LavaPixel){
                    if(temperature < 0) continue;

                    LavaPixel l = (LavaPixel) p;
                    float addAmount = temperature * 0.25f;
                    l.temperature += addAmount;
                    this.temperature -= addAmount;
                    continue;
                } else if(p instanceof MetalPixel){
                    float transferAmount = PApplet.min(temperature, MetalPixel.FIRE_PIXEL_ADD_AMOUNT);
                    ((MetalPixel) p).addToTemperature(this, transferAmount);
                    temperature -= transferAmount;
                    continue;
                }
                TemperatureObject t = (TemperatureObject) p;
                PVector direction = new Vector(getIntX() - p.getIntX(), getIntY() - p.getIntY()).normalize();
                PVector gravityDirection = new Vector(getVx(), getVy());

                float distMultiplier =  ((direction.dist(gravityDirection)) / 2);
                if(getNearbyEventResult(new Vector(p.getIntX(), p.getIntY()), distMultiplier)){
                    if(t instanceof FusePixel)
                        t.addToTemperature(this, FUSE_IGNITE_TEMP - t.getTemperature());

                    TemperatureObject.swapTemperatures(this, t, 0.25f);
                    //t.addToTemperature(temperature * t.getTemperatureChangeFactor(this, temperature));
                }

                /*
                float flammability = getTempRatio() * (getFlammability(entry.getValue()) /
                        (2 * PApplet.dist(getIntX(), getIntY(), p.getIntX(), p.getIntY())));
                flammability *= (1 - distMultiplier);
                if(getNearbyEventResult(new Vector(p.getIntX(), p.getIntY()), flammability)) t.addToTemperature(temperature);
                */
            }
        }
    }

    @Override
    public float getDensity() {
        return DENSITY;
    }

    @Override
    public void update() {
        super.update();
        updateTemperature();
        inflameNearby();

        if(temperature <= MIN_FIRE_TEMPERATURE){
            getManager().setPixel(getIntX(), getIntY(), null);
            return;
        }
    }

}


