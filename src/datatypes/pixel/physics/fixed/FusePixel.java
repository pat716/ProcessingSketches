package datatypes.pixel.physics.fixed;

import datatypes.pixel.Color;
import datatypes.pixel.physics.PhysicsManager;
import datatypes.pixel.physics.PhysicsPixel;
import datatypes.pixel.physics.TemperatureObject;
import datatypes.pixel.physics.dynamic.FirePixel;
import math.Vector;
import processing.core.PApplet;

import java.util.Map;

/**
 * Created by psweeney on 9/26/16.
 */
public class FusePixel extends PhysicsPixel implements TemperatureObject{
    private static final float R = 100, G = 25, B = 10;
    private static final int NUM_IGNITE_FRAMES = 1;

    private boolean ignitedStarted = false;
    private int igniteFramesRemaining = NUM_IGNITE_FRAMES;

    private static final float TEMP_MAX_ADD_AMT = 2;
    public static final float FUSE_IGNITE_TEMP = 10;
    private float temperature = 0;

    public FusePixel(PhysicsManager manager, int x, int y, float temperature){
        super(manager, x, y, PhysicsPixelType.FUSE);
        this.temperature = PApplet.max(0, PApplet.min(FUSE_IGNITE_TEMP, temperature));
    }

    public FusePixel(PhysicsManager manager, int x, int y){
        this(manager, x, y, 0);
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
        if(!FirePixel.canIgnite(t) && !(t instanceof FusePixel)) return 0;
        return 1;
        /*
        if(amount < 0){
            return 1/(1 + PApplet.abs(amount));
        } else {
            if(amount <= TEMP_MAX_ADD_AMT) return 1;
            else return TEMP_MAX_ADD_AMT/amount;
        } */
    }

    @Override
    public float getBaseTemperatureChangeFactor() {
        return 0;
    }

    @Override
    public void addToTemperature(TemperatureObject source, float amount) {
        if(ignitedStarted) return;
        if (amount > 0){
            temperature += PApplet.min(amount, TEMP_MAX_ADD_AMT);
            if (temperature >= FUSE_IGNITE_TEMP) {
                if (FirePixel.canIgnite(source) || source instanceof FusePixel) ignite();
                else temperature = FUSE_IGNITE_TEMP;
            }
            getManager().swapPixels(getIntX(), getIntY(), getIntX(), getIntY());
        }
    }

    @Override
    public Color getBaseColor() {
        return new Color(R, G, B);
    }

    @Override
    public Color getColor() {
        if(temperature <= 0){
            return new Color(R, G, B);
        }

        Color fireColor = new FirePixel(getManager(), getIntX(), getIntY()).getBaseColor();

        float countdownRatio = 1 - PApplet.max(0, PApplet.min(FUSE_IGNITE_TEMP, temperature)) / FUSE_IGNITE_TEMP;

        float r = countdownRatio * R + (1 - countdownRatio) * fireColor.getR();
        float g = countdownRatio * G + (1 - countdownRatio) * fireColor.getG();
        float b = countdownRatio * B + (1 - countdownRatio) * fireColor.getB();

        return new Color(r, g, b);
    }

    @Override
    public Color getPostEffectColor(Color previousColor, float alphaCutoff, float maxAlpha) {
        if(previousColor != null) return previousColor;
        return null;
    }

    private void ignite(){
        ignitedStarted = true;
        igniteFramesRemaining = NUM_IGNITE_FRAMES;
        getManager().swapPixels(getIntX(), getIntY(), getIntX(), getIntY());
    }

    private void finalIgnite(){
        for(Map.Entry<Vector, PhysicsPixel> entry : getSurroundingPixels().entrySet()){
            PhysicsPixel p = entry.getValue();
            if(p == null || p == this) continue;

            if(p.getType() == PhysicsPixelType.FUSE){
                FusePixel f = (FusePixel) p;
                if(f.getTemperature() < FUSE_IGNITE_TEMP && getNearbyEventResult(entry.getKey(), 1)) {
                    f.temperature += FUSE_IGNITE_TEMP - f.getTemperature();
                    getManager().swapPixels(f.getIntX(), f.getIntY(), f.getIntX(), f.getIntY());
                }
            }
        }
        getManager().setPixel(getIntX(), getIntY(), new FirePixel(getManager(), getIntX(), getIntY()));
    }

    @Override
    public void updateTemperature() {
        TempGridObject tempGridObject = getManager().getTempGridObject(getIntX(), getIntY());
        addToTemperature(tempGridObject, tempGridObject.getTemperature());
        //if(temperature <= 0) temperature = 0;
    }

    @Override
    public void update() {
        if(ignitedStarted){
            if(igniteFramesRemaining <= 0){
                finalIgnite();
                return;
            } else {
                igniteFramesRemaining -= 1;
                getManager().swapPixels(getIntX(), getIntY(), getIntX(), getIntY());
            }
        } else {
            float startTemp = temperature;
            updateTemperature();
            if(temperature != startTemp){
                addToTemperature(this, TEMP_MAX_ADD_AMT);
                getManager().swapPixels(getIntX(), getIntY(), getIntX(), getIntY());
            }
        }

    }
}
