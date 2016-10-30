package datatypes.pixel.physics.dynamic;

import datatypes.pixel.Color;
import datatypes.pixel.physics.PhysicsManager;
import datatypes.pixel.physics.PhysicsPixel;
import datatypes.pixel.physics.TemperatureObject;
import math.Vector;
import processing.core.PVector;

import static processing.core.PConstants.TWO_PI;

/**
 * Created by psweeney on 9/25/16.
 */
public class GunpowderPixel extends DynamicPixel implements TemperatureObject{
    private static final float R = 125, G = 125, B = 125;
    private static final float DENSITY = 0.15f;
    private static final float FLUIDITY = 0.25f;
    private static final float RANDOM_VELOCITY_AMOUNT = 0.3f;
    private static final float GRAVITY_MULTIPLIER = 4f;
    private static final float IGNITE_VELOCITY_MULTIPLIER = 5f;
    private static final int IGNITE_FIRE_COUNTDOWN = 5;
    private static final int IGNITE_RANDOM_DIRECTION_STRENGTH = 2;

    private static final float IGNITE_TEMPERATURE = 25;

    private static final float GRID_TEMP_AMOUNT = 0.05f;

    private float temperature = 0;

    public GunpowderPixel(PhysicsManager manager, int x, int y){
        super(manager, x, y, PhysicsPixelType.GUNPOWDER);
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
        if(amount < 0) return -1; else return 1;
    }

    @Override
    public float getBaseTemperatureChangeFactor() {
        return 0.1f;
    }

    @Override
    public void addToTemperature(TemperatureObject source, float amount) {
        if((temperature < 0 && amount < 0) || temperature >= IGNITE_TEMPERATURE) return;
        temperature += amount;
        if(temperature >= IGNITE_TEMPERATURE){
            if(FirePixel.canIgnite(source)) ignite();
            else temperature = IGNITE_TEMPERATURE;
        }

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

    private void ignite(Vector direction){
        temperature = IGNITE_TEMPERATURE;
        if(direction == null){
            getManager().setPixel(getIntX(), getIntY(), new FirePixel(getManager(), getIntX(), getIntY()));
            return;
        }

        float randomAngle = (float) Math.random() * TWO_PI;
        Vector randomVector = Vector.getVectorRelativeToOrigin(randomAngle, IGNITE_RANDOM_DIRECTION_STRENGTH);

        PVector explosionVelocity = direction.normalize().mult(IGNITE_VELOCITY_MULTIPLIER).add(randomVector);

        getManager().setPixel(getIntX(), getIntY(), new FirePixel(getManager(), getIntX(), getIntY(), explosionVelocity.x,
                explosionVelocity.y, IGNITE_FIRE_COUNTDOWN));
    }

    @Override
    public float getDensity() {
        return DENSITY;
    }

    private void ignite() {
        temperature = IGNITE_TEMPERATURE;
        for(PhysicsPixel p : getSurroundingPixels().values()){
            if(p == null) continue;
            if(p.getType() == PhysicsPixelType.GUNPOWDER && ((TemperatureObject) p).getTemperature() <
                    IGNITE_TEMPERATURE){
                if(getNearbyEventResult(new Vector(p.getIntX(), p.getIntY()), 0.0f)){}
                    //((GunpowderPixel) p).ignite(new Vector(getVx() + (p.getX() - getX()), getVy() + (p.getY() - getY())));
            } else {
                if(p instanceof TemperatureObject){
                    float ratio = 1;
                    if(p instanceof DynamicPixel) ratio /= 2;
                    TemperatureObject.swapTemperatures(this, (TemperatureObject) p, ratio);
                    //TemperatureObject t = (TemperatureObject) p;
                    //t.addToTemperature(FirePixel.MAX_FIRE_TEMPERATURE * t.getTemperatureChangeFactor(this, F));
                }
            }
        }
        ignite(null);
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

            if(temperature > IGNITE_TEMPERATURE) break;
        }
    }

    @Override
    public void update() {
        super.update();
        updateTemperature();
    }

}
