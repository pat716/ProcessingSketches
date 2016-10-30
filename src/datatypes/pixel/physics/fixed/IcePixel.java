package datatypes.pixel.physics.fixed;

import datatypes.pixel.Color;
import datatypes.pixel.physics.PhysicsManager;
import datatypes.pixel.physics.PhysicsPixel;
import datatypes.pixel.physics.TemperatureObject;
import datatypes.pixel.physics.dynamic.DynamicPixel;
import datatypes.pixel.physics.dynamic.WaterPixel;
import processing.core.PApplet;

/**
 * Created by psweeney on 9/26/16.
 */
public class IcePixel extends PhysicsPixel implements TemperatureObject{
    private static final float R = 200, G = 210, B = 255;
    private static final float MAX_ICE_TEMPERATURE = WaterPixel.MIN_WATER_TEMPERATURE;
    private static final float MIN_ICE_TEMPERATURE = -150;
    private static final float TEMP_CHANGE_FACTOR = 0.5f;
    private static final float MIN_TEMP_CHANGE_TO_TRIGGER_UPDATE = 0.05f;
    private static final float WATER_RATIO_THRESHOLD = 0.025f;
    private static final float WATER_RATIO_POW = 3;
    private static final float MAX_WATER_RATIO = 0.75f;

    private static final float GRID_TEMP_AMOUNT = 0.1f;

    private float temperature;

    public IcePixel(PhysicsManager manager, int x, int y, float temperature){
        super(manager, x, y, PhysicsPixelType.ICE);
        this.temperature = PApplet.min(MAX_ICE_TEMPERATURE, PApplet.max(MIN_ICE_TEMPERATURE, temperature));
    }

    public IcePixel(PhysicsManager manager, int x, int y){
        this(manager, x, y, MIN_ICE_TEMPERATURE);
    }

    @Override
    public Color getBaseColor() {
        return new Color(R, G, B);
    }

    @Override
    public Color getColor() {
        float waterRatio = getWaterRatio();
        Color waterColor = WaterPixel.getBaseWaterColor();

        float r = (1 - waterRatio) * R + waterRatio * waterColor.getR();
        float g = (1 - waterRatio) * G + waterRatio * waterColor.getG();
        float b = (1 - waterRatio) * B + waterRatio * waterColor.getB();

        return new Color(r, g, b);
    }

    @Override
    public Color getPostEffectColor(Color previousColor, float alphaCutoff, float maxAlpha) {
        Color pixelColor = getColor();
        Color effectColor = new Color(pixelColor.getR(), pixelColor.getG(), pixelColor.getB(), maxAlpha *
                (1 - getWaterRatio()) * 0.1f);

        if(previousColor != null){
            if(effectColor.getA() < alphaCutoff) return previousColor;
            return previousColor.add(effectColor, Color.BLEND_MODE.NORMAL);
        }

        if(effectColor.getA() < alphaCutoff) return null;
        return effectColor;
    }

    @Override
    public float getTemperature() {
        return temperature;
    }

    @Override
    public boolean hasStaticTemp() {
        return false;
    }

    public float getWaterRatio(){
        return PApplet.min(MAX_WATER_RATIO, PApplet.max(0, PApplet.pow(1 - ((MAX_ICE_TEMPERATURE - temperature)
                /(MAX_ICE_TEMPERATURE - MIN_ICE_TEMPERATURE)), WATER_RATIO_POW)));
    }

    @Override
    public float getTemperatureChangeFactor(TemperatureObject t, float amount) {
        float waterRatio = getWaterRatio();
        return (1 - waterRatio) * TEMP_CHANGE_FACTOR + waterRatio * WaterPixel.TEMP_CHANGE_FACTOR;
        //return waterRatio * TEMP_CHANGE_FACTOR + (1 - waterRatio) * WaterPixel.TEMP_CHANGE_FACTOR;
    }

    @Override
    public float getBaseTemperatureChangeFactor() {
        return getTemperatureChangeFactor(null, 0);
    }

    @Override
    public void addToTemperature(TemperatureObject source, float amount) {
        if(getWaterRatio() >= WATER_RATIO_THRESHOLD && PApplet.abs(amount) >= MIN_TEMP_CHANGE_TO_TRIGGER_UPDATE)
            getManager().swapPixels(getIntX(), getIntY(), getIntX(), getIntY());
        temperature += amount;
        if(temperature <= MIN_ICE_TEMPERATURE)
            temperature = MIN_ICE_TEMPERATURE;
        else if(temperature >= MAX_ICE_TEMPERATURE)
            getManager().setPixel(getIntX(), getIntY(), new WaterPixel(getManager(), getIntX(), getIntY(),
                    temperature));

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

            if(temperature > MAX_ICE_TEMPERATURE) break;
        }
    }

    @Override
    public void update() {
        updateTemperature();
    }

}
