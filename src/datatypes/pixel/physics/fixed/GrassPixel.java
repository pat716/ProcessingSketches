package datatypes.pixel.physics.fixed;

import datatypes.pixel.Color;
import datatypes.pixel.physics.PhysicsManager;
import datatypes.pixel.physics.PhysicsPixel;
import datatypes.pixel.physics.TemperatureObject;
import datatypes.pixel.physics.dynamic.FirePixel;
import math.Vector;
import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PVector;

/**
 * Created by psweeney on 9/25/16.
 */
public class GrassPixel extends PhysicsPixel implements TemperatureObject{
    private static final float R = 10, G = 175, B = 50;
    private static final float MAX_CONVERSION_ANGLE = PConstants.PI/3;
    private static final float INIT_TEMPERATURE = 0;
    private static final float MAX_TEMPERATURE = 15;
    private static final float MIN_TEMPERATURE = -25;
    private static final float TEMP_CHANGE_AMT = 0.25f;

    float temperature = INIT_TEMPERATURE;

    private static final float GRID_TEMP_AMOUNT = 0.01f;

    private Vector upDirection;

    public GrassPixel(PhysicsManager manager, int x, int y, Vector upDirection){
        super(manager, x, y, PhysicsPixelType.GRASS);
        PVector p = upDirection.normalize();
        this.upDirection = new Vector(p.x, p.y);
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
        if(amount < 0) return TEMP_CHANGE_AMT/(1 + PApplet.abs(amount)); else return TEMP_CHANGE_AMT;
    }

    @Override
    public float getBaseTemperatureChangeFactor() {
        return 1 - TEMP_CHANGE_AMT;
    }

    @Override
    public void addToTemperature(TemperatureObject source, float amount) {
        temperature += amount;
        if(temperature <= MIN_TEMPERATURE){
            temperature = MIN_TEMPERATURE;
            //TODO frozen plant?
        } else if(temperature >= MAX_TEMPERATURE) {
            if (FirePixel.canIgnite(source))
                getManager().setPixel(getIntX(), getIntY(), new FirePixel(getManager(), getIntX(), getIntY()));
            else temperature = MAX_TEMPERATURE;
        }
    }

    @Override
    public void updateTemperature() {
        TempGridObject tempGridObject = getManager().getTempGridObject(getIntX(), getIntY());
        float startTemp = temperature;
        addToTemperature(tempGridObject, tempGridObject.getTemperature());
        if(PApplet.abs(temperature - startTemp) > 0) getManager().swapPixels(getIntX(), getIntY(), getIntX(),
                getIntY());
    }

    @Override
    public void update() {
        updateTemperature();
    }

    public static float getConversionTypeMultiplier(PhysicsPixel other){
        if(other == null) return 0;

        switch (other.getType()){
            case WATER: return 2f;
        }
        return 0;
    }

    public Vector getConversionAngle(PhysicsPixel other){
        if(other == null || getConversionTypeMultiplier(other) <= 0){
            return null;
        }

        Vector pos = new Vector(getX(), getY()), otherPos = new Vector(other.getX(), other.getY());
        float angleTo = pos.getAngleToVector(otherPos);
        if(Vector.getAbsoluteAngleDistance(angleTo, upDirection.getAngleFromOrigin()) <= MAX_CONVERSION_ANGLE){
            return Vector.getVectorRelativeToOrigin(angleTo, 1);
        }
        return null;
    }

    @Override
    public Color getBaseColor() {
        return new Color(R, G, B);
    }

    @Override
    public Color getColor() {
        return new Color(R, G, B);
    }

    @Override
    public Color getPostEffectColor(Color previousColor, float alphaCutoff, float maxAlpha) {
        if(previousColor != null) return previousColor;
        return null;
    }
}
