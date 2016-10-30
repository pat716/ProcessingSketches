package datatypes.pixel.physics.fixed;

import datatypes.pixel.Color;
import datatypes.pixel.CustomPixel;
import datatypes.pixel.physics.PhysicsManager;
import datatypes.pixel.physics.PhysicsPixel;
import datatypes.pixel.physics.TemperatureObject;
import datatypes.pixel.physics.dynamic.CO2GasPixel;
import datatypes.pr.Region;
import math.Vector;
import processing.core.PApplet;

import java.util.Map;

/**
 * Created by psweeney on 9/26/16.
 */
public class CO2SolidPixel extends PhysicsPixel implements TemperatureObject{
    private static final float R = 150, G = 155, B = 175;
    private static final float CONCENTRATION_DEC_PER_SPAWN = 0.1f;
    private float concentration;

    public CO2SolidPixel(PhysicsManager manager, int x, int y, float concentration){
        super(manager, x, y, PhysicsPixelType.CO2_SOLID);
        this.concentration = concentration;
    }

    public CO2SolidPixel(PhysicsManager manager, int x, int y){
        this(manager, x, y, 1);
    }

    @Override
    public float getTemperature() {
        return CO2GasPixel.CO2_TEMPERATURE;
    }

    @Override
    public boolean hasStaticTemp() {
        return true;
    }

    @Override
    public float getTemperatureChangeFactor(TemperatureObject t, float amount) {
        return 0;
    }

    @Override
    public float getBaseTemperatureChangeFactor() {
        return 0;
    }

    @Override
    public void addToTemperature(TemperatureObject source, float amount) {

    }

    @Override
    public Color getBaseColor() {
        return new Color(R, G, B);
    }

    @Override
    public Color getColor() {
        Color baseColor = getBaseColor();
        return new Color(baseColor.getR(), baseColor.getG(), baseColor.getB(), 255 * concentration);
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
        if(concentration <= 0){
            getManager().setPixel(getIntX(), getIntY(), null);
            return;
        }

        for(Map.Entry<Vector, PhysicsPixel> entry : getSurroundingPixels().entrySet()){
            Vector key = entry.getKey();
            PhysicsPixel value = entry.getValue();
            if(value == null && getNearbyEventResult(key, 1)){

                getManager().setPixel(key, new CO2GasPixel(getManager(), CustomPixel.convertInt(key.x),
                        CustomPixel.convertInt(key.y)));
                concentration -= CONCENTRATION_DEC_PER_SPAWN;
            }
        }

        getManager().swapPixels(getIntX(), getIntY(), getIntX(), getIntY());
    }
}
