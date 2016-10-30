package datatypes.pixel.physics.fixed;

import datatypes.pixel.Color;
import datatypes.pixel.physics.PhysicsManager;
import datatypes.pixel.physics.PhysicsPixel;
import datatypes.pixel.physics.TemperatureObject;
import datatypes.pixel.physics.dynamic.FirePixel;
import datatypes.pixel.physics.dynamic.LavaPixel;
import processing.core.PApplet;

/**
 * Created by psweeney on 10/2/16.
 */

public class MetalPixel extends PhysicsPixel implements TemperatureObject {
    private static final float R = 75, G = 75, B = 112;
    private static final float MELT_START_TEMPERATURE = 100;
    private static final float MELT_FINAL_TEMPERATURE = LavaPixel.LAVA_MIN_TEMPERATURE;

    private static final float NEARBY_METAL_TRANSFER_AMOUNT = 1f;
    private static final float MIN_TEMP_CHANGE_TO_TRIGGER_UPDATE = .1f;

    public static final float FIRE_PIXEL_ADD_AMOUNT = 5;

    private static final float POST_EFFECT_DEC_AMOUNT = 100;

    private static final float GRID_TEMP_AMOUNT = 0.2f;

    private float temperature;

    public MetalPixel(PhysicsManager manager, int x, int y, float temperature){
        super(manager, x, y, PhysicsPixelType.METAL);
        this.temperature = temperature;
        if(temperature >= MELT_FINAL_TEMPERATURE)
            getManager().setPixel(getIntX(), getIntY(), new LavaPixel(getManager(), getIntX(), getIntY(), temperature));
    }

    public MetalPixel(PhysicsManager manager, int x, int y){
        this(manager, x, y, manager.getEquilibriumTemperature());
    }

    public float getLavaRatio(){
        if(temperature <= MELT_START_TEMPERATURE) return 0;
        else if(temperature >= MELT_FINAL_TEMPERATURE) return 1;
        return (temperature - MELT_START_TEMPERATURE) / (MELT_FINAL_TEMPERATURE - MELT_START_TEMPERATURE);
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
        return (getLavaRatio()/4) + 0.75f;
    }

    @Override
    public float getBaseTemperatureChangeFactor() {
        return 0.75f;
    }

    @Override
    public void addToTemperature(TemperatureObject source, float amount) {
        temperature += amount;
        if(temperature >= MELT_FINAL_TEMPERATURE){
            getManager().setPixel(getIntX(), getIntY(), new LavaPixel(getManager(), getIntX(), getIntY(), temperature));
        } else if(amount != 0 || !(source instanceof TempGridObject)){
            getManager().swapPixels(getIntX(), getIntY(), getIntX(), getIntY());
        }
    }

    @Override
    public Color getBaseColor() {
        return new Color(R, G, B);
    }

    @Override
    public Color getColor() {
        float lavaRatio = getLavaRatio();
        if(lavaRatio <= 0) return getBaseColor();
        else if(lavaRatio >= 1) return new Color(LavaPixel.R, LavaPixel.G, LavaPixel.B);

        return new Color((1 - lavaRatio) * R + lavaRatio * LavaPixel.R, (1 - lavaRatio) * G + lavaRatio * LavaPixel.G,
                (1 - lavaRatio) * B + lavaRatio * LavaPixel.B);
    }

    @Override
    public Color getPostEffectColor(Color previousColor, float alphaCutoff, float maxAlpha) {
        float alpha = getLavaRatio() * (maxAlpha);
        Color effectColor = new Color(LavaPixel.R - POST_EFFECT_DEC_AMOUNT, LavaPixel.G - POST_EFFECT_DEC_AMOUNT,
                LavaPixel.B - POST_EFFECT_DEC_AMOUNT, alpha);

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

            if(t instanceof MetalPixel){
                float tInitTemp = t.getTemperature();
                float avgTemp = (temperature + ((MetalPixel) t).temperature)/2;
                float transferMultiplier = NEARBY_METAL_TRANSFER_AMOUNT;
                float distance = PApplet.dist(getIntX(), getIntY(), p.getIntX(), p.getIntY());
                if(distance > 1) transferMultiplier *= 1/distance;

                temperature = transferMultiplier * avgTemp + (1 - transferMultiplier) * temperature;
                ((MetalPixel) t).temperature = transferMultiplier * avgTemp + (1 - transferMultiplier) *
                        ((MetalPixel) t).temperature;
                if(PApplet.abs(t.getTemperature() - tInitTemp) >= MIN_TEMP_CHANGE_TO_TRIGGER_UPDATE) {
                    t.addToTemperature(this, 0);
                }
            } else if(p instanceof FirePixel) {
                continue;
            } else {
                TemperatureObject.swapTemperatures(this, t, 0.5f);
            }
        }
    }

    @Override
    public void update() {
        float initTemperature = temperature;
        updateTemperature();
        if(PApplet.abs(initTemperature - temperature) >= MIN_TEMP_CHANGE_TO_TRIGGER_UPDATE)
            getManager().swapPixels(getIntX(), getIntY(), getIntX(), getIntY());
    }
}