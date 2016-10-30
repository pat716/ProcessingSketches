package datatypes.pixel.physics.dynamic;

import datatypes.pixel.Color;
import datatypes.pixel.physics.PhysicsManager;
import datatypes.pixel.physics.PhysicsPixel;
import datatypes.pixel.physics.TemperatureObject;
import math.Vector;
import processing.core.PApplet;
import sketches.FallingSand;

import java.util.Map;
import java.util.Set;

/**
 * Created by psweeney on 10/6/16.
 */
public class UraniumPixel extends DynamicPixel implements TemperatureObject{
    private static final float MAX_R = 230, MAX_G = 255, MAX_B = 150;
    private static final float MIN_R = 125, MIN_G = 175, MIN_B = 75;

    private static final float DENSITY = 8;
    private static final float FLUIDITY = 0.5f;
    private static final float GRAVITY_MULTIPLIER = 7.5f;
    private static final float RANDOM_VELOCITY_AMOUNT = 0.65f;

    private static final float VELOCITY_SLOW_FACTOR = 0.15f;
    private static final float MAX_VELOCITY = 0.125f;

    private static final float MIN_TEMPERATURE = -5;
    private static final float MAX_TEMPERATURE = 5;

    private static final float POST_EFFECT_DEC_AMOUNT = 70;

    private static final float BASE_IRRADIATION_CHANCE = 0.5f;

    private float temperature;

    public UraniumPixel(PhysicsManager manager, int x, int y, float temperature){
        super(manager, x, y, PhysicsPixelType.URANIUM);
        this.temperature = temperature;
    }

    public UraniumPixel(PhysicsManager manager, int x, int y){
        this(manager, x, y, MAX_TEMPERATURE);
    }

    public static float getRadiationMoveChance(PhysicsPixel p){
        if(p == null) return 0;
        switch (p.getType()){
            case ASH: return 0.1f;
            case CO2_GAS: return 0.1f;
            case FIRE: return 0.5f;
            case GUNPOWDER: return 0.1f;
            case LAVA: return 0.375f;
            case STEAM: return 0.75f;
            case WATER: return 0.625f;
            case CO2_SOLID: return 0.01f;
            case FUSE: return 0.05f;
            case GRASS: return 0.3f;
            case ICE: return 0.125f;
            case METAL: return 0.5f;
            default: return 0;
        }
    }

    @Override
    public float getTemperature() {
        return temperature;
    }

    @Override
    public float getBaseTemperatureChangeFactor() {
        return 0.75f;
    }

    @Override
    public float getTemperatureChangeFactor(TemperatureObject t, float amount) {
        if(t instanceof UraniumPixel) return 1;
        return getBaseTemperatureChangeFactor();
    }

    @Override
    public boolean hasStaticTemp() {
        return false;
    }

    @Override
    public void addToTemperature(TemperatureObject source, float amount) {
        temperature += amount;
        if(temperature > MAX_TEMPERATURE) temperature = MAX_TEMPERATURE;
        else if(temperature < MIN_TEMPERATURE) temperature = MIN_TEMPERATURE;
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

    private float getTempRatio(){
        return PApplet.max(0, PApplet.min(1, (temperature - MIN_TEMPERATURE)/(MAX_TEMPERATURE - MIN_TEMPERATURE)));
    }

    @Override
    public Color getBaseColor() {
        return new Color((MAX_R + MIN_R)/2, (MAX_G + MIN_G)/2, (MAX_B + MIN_B)/2);
    }

    @Override
    public Color getColor() {
        float tempRatio = getTempRatio();
        return new Color(tempRatio * MAX_R + (1 - tempRatio) * MIN_R, tempRatio * MAX_G + (1 - tempRatio) * MIN_G,
                tempRatio * MAX_B + (1 - tempRatio) * MIN_B, 255);
    }

    @Override
    public Color getPostEffectColor(Color previousColor, float alphaCutoff, float maxAlpha) {
        float alpha = (getTempRatio()/2 + 0.5f) * maxAlpha;
        Color particleColor = getColor();
        Color effectColor = new Color(particleColor.getR() - POST_EFFECT_DEC_AMOUNT, particleColor.getG() -
                POST_EFFECT_DEC_AMOUNT, particleColor.getB() - POST_EFFECT_DEC_AMOUNT, alpha);
        if(previousColor != null){
            if(alpha < alphaCutoff) return previousColor;
            return previousColor.add(effectColor, Color.BLEND_MODE.NORMAL);
        }

        if(alpha < alphaCutoff) return null;
        return effectColor;
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
        }
    }

    private void processAdditionalIrradiationEffects(PhysicsPixel p){
        if(p == null) return;
        float chanceMultiplier = 1;
        if(PApplet.dist(getIntX(), getIntY(), p.getIntX(), p.getIntY()) > 1){
            chanceMultiplier = 1 / FallingSand.sqrt2;
        }
        switch (p.getType()) {
            case GRASS:
                if(Math.random() < 0.00001 * chanceMultiplier){
                    getManager().setPixel(p.getIntX(), p.getIntY(), new AshPixel(p.getManager(), p.getIntX(),
                            p.getIntY()));
                }
                break;
            default:
                return;
        }
    }

    private void irradiate(){
        float irradiationChance = ((getTempRatio()/2) + 0.5f) * BASE_IRRADIATION_CHANCE;
        Vector velocity = new Vector(getVx(), getVy());
        if(velocity.getMagnitude() > 1){
            irradiationChance /= velocity.getMagnitude();
        }

        Set<PhysicsPixelType> moveTypes = DynamicPixel.getAllDynamicTypes();
        //moveTypes.put(PhysicsPixelType.EMPTY, 1f);
        //moveTypes.put(PhysicsPixelType.URANIUM, 0.25f);
        //moveTypes.add(PhysicsPixelType.EMPTY);
        //moveTypes.add(PhysicsPixelType.URANIUM);

        for(Map.Entry<Vector, PhysicsPixel> entry : getSurroundingPixels().entrySet()){
            Vector key = entry.getKey();
            PhysicsPixel value = entry.getValue();
            if(value == null || value.getType() == PhysicsPixelType.URANIUM) continue;
            float moveChance = irradiationChance * getRadiationMoveChance(value);
            if(PApplet.dist(getIntX(), getIntY(), value.getIntX(), value.getIntY()) > 1){
                moveChance *= (1/FallingSand.sqrt2);
            }
            value.attemptMoveRandom(moveTypes, moveChance);
            processAdditionalIrradiationEffects(value);

            if(value instanceof TemperatureObject){
                float tempChangeAmount = ((float) Math.random() * (MAX_TEMPERATURE - MIN_TEMPERATURE) +
                        MIN_TEMPERATURE) * 0.125f;
                ((TemperatureObject) value).addToTemperature(this, tempChangeAmount * ((TemperatureObject)
                        value).getTemperatureChangeFactor(this, tempChangeAmount));
            }
        }
    }

    @Override
    public void update() {
        super.update();
        setVx(getVx() * (1 - VELOCITY_SLOW_FACTOR));
        setVy(getVy() * (1 - VELOCITY_SLOW_FACTOR));
        Vector velocityVector = new Vector(getVx(), getVy());
        float magnitude = velocityVector.getMagnitude();
        if(magnitude > MAX_VELOCITY){
            setVx(getVx() * (MAX_VELOCITY/magnitude));
            setVy(getVy() * (MAX_VELOCITY/magnitude));
        }
        updateTemperature();
        irradiate();
    }
}
