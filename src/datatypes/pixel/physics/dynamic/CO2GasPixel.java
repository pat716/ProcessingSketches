package datatypes.pixel.physics.dynamic;

import datatypes.pixel.Color;
import datatypes.pixel.physics.PhysicsManager;
import datatypes.pixel.physics.PhysicsPixel;
import datatypes.pixel.physics.TemperatureObject;
import math.Vector;
import processing.core.PApplet;

import java.util.Map;

/**
 * Created by psweeney on 9/26/16.
 */
public class CO2GasPixel extends DynamicPixel implements TemperatureObject{
    private static final float R = 150, G = 155, B = 175;
    private static final float DENSITY = 0.01f;
    private static final float FLUIDITY = 0.99f;
    private static final float GRAVITY_MULTIPLIER = 0.6f;
    private static final float RANDOM_VELOCITY_AMOUNT = 0.15f;
    private static final float CONCENTRATION_UPDATE_AMT = 0.005f;
    private static final float MIN_CONCENTRATION = 0.0125f;
    private static final float MAX_SPEED = 0.95f;

    public static final float CO2_TEMPERATURE = -50;

    private float concentration;

    public CO2GasPixel(PhysicsManager manager, int x, int y, float concentration){
        super(manager, x, y, PhysicsPixelType.CO2_GAS);
        this.concentration = concentration;
    }

    public CO2GasPixel(PhysicsManager manager, int x, int y){
        this(manager, x, y, 1);
    }

    @Override
    public float getTemperature() {
        return CO2_TEMPERATURE;
    }

    @Override
    public boolean hasStaticTemp() {
        return true;
    }

    @Override
    public float getTemperatureChangeFactor(TemperatureObject t, float amount) {
        return concentration * 0.25f;
    }

    @Override
    public float getBaseTemperatureChangeFactor() {
        return concentration * 0.25f;
    }

    @Override
    public void addToTemperature(TemperatureObject source, float amount) { }

    private void updateConcentration(){
        float nearbyCO2Total = 0;
        for(PhysicsPixel p : getSurroundingPixels().values()) {
            if (p == null) continue;
            if(p.getType() == PhysicsPixelType.CO2_GAS) nearbyCO2Total += ((CO2GasPixel) p).concentration;
            if(p.getType() == PhysicsPixelType.CO2_SOLID) nearbyCO2Total += 1;
        }

        concentration = CONCENTRATION_UPDATE_AMT * ((nearbyCO2Total / 8) * 0.99f) + (1 - CONCENTRATION_UPDATE_AMT) *
                concentration;
        //concentration -= 1 - 1/(1 + Math.random() * PApplet.dist(0, 0, getVx(), getVy()));
        concentration -= 0.001f * (1 - 1/(1 + Math.random() * PApplet.dist(0, 0, getVx(), getVy())));
    }

    private void attemptFreeze(){
        for(Map.Entry<Vector, PhysicsPixel> entry : getSurroundingPixels().entrySet()){
            PhysicsPixel p = entry.getValue();
            if(p == null || !(p instanceof TemperatureObject)) continue;

            if(getNearbyEventResult(entry.getKey(), 1)) ((TemperatureObject) p).addToTemperature(this, CO2_TEMPERATURE *
                    concentration);
        }
    }

    @Override
    public float getFluidity() {
        return FLUIDITY;
    }

    @Override
    public float getDensity() {
        return DENSITY;
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
        Color baseColor = getBaseColor();
        return new Color(baseColor.getR(), baseColor.getG(), baseColor.getB(), concentration * 255);
    }

    @Override
    public Color getPostEffectColor(Color previousColor, float alphaCutoff, float maxAlpha) {
        Color pixelColor = getColor();
        Color effectColor = new Color(pixelColor.getR(), pixelColor.getG(), pixelColor.getB(),
                PApplet.min(maxAlpha/6, pixelColor.getA() * 4));

        if(previousColor != null){
            if(effectColor.getA() < alphaCutoff) return previousColor;
            return previousColor.add(effectColor, Color.BLEND_MODE.NORMAL);
        }

        if(effectColor.getA() < alphaCutoff) return null;
        return effectColor;
    }

    @Override
    public void updateTemperature() {

    }

    @Override
    public void update() {
        super.update();
        float speed = getLastMoveAmount();
        if(speed > MAX_SPEED){
            setVx(getVx() * (MAX_SPEED/speed));
            setVy(getVy() * (MAX_SPEED/speed));
        }
        updateConcentration();
        attemptFreeze();
        if(concentration < MIN_CONCENTRATION){
            getManager().setPixel(getIntX(), getIntY(), null);
            return;
        }
    }

}
