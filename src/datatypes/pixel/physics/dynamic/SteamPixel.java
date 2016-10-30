package datatypes.pixel.physics.dynamic;

import datatypes.pixel.Color;
import datatypes.pixel.physics.PhysicsManager;
import datatypes.pixel.physics.PhysicsPixel;
import datatypes.pixel.physics.TemperatureObject;
import processing.core.PApplet;

/**
 * Created by psweeney on 9/25/16.
 */
public class SteamPixel extends DynamicPixel implements TemperatureObject{
    private static final float R = 100, G = 125, B = 175;
    private static final float DENSITY = 0.05f;
    private static final float FLUIDITY = 0.95f;
    private static final float GRAVITY_MULTIPLIER = -0.5f;
    private static final float RANDOM_VELOCITY_AMOUNT = 0.95f;
    private static final float CONCENTRATION_UPDATE_AMT = 0.01f;
    private static final float MIN_CONCENTRATION = 0.0125f;
    private static final float MAX_SPEED = 1.95f;

    private static final float MIN_STEAM_TEMPERATURE = WaterPixel.MAX_WATER_TEMPERATURE;
    private static final float MAX_STEAM_TEMPERATURE = 50;
    private static final float TEMP_CHANGE_FACTOR = 0.5f;

    private static final float GRID_TEMP_AMOUNT = 0.1f;

    private float concentration, temperature;

    public SteamPixel(PhysicsManager manager, int x, int y, float temperature, float vx, float vy){
        super(manager, x, y, PhysicsPixelType.STEAM);
        this.concentration = 1;
        this.temperature = PApplet.max(MIN_STEAM_TEMPERATURE, PApplet.min(MAX_STEAM_TEMPERATURE, temperature));
        setVx(vx);
        setVy(vy);
    }

    public SteamPixel(PhysicsManager manager, int x, int y, float temperature){
        this(manager, x, y, temperature, 0, 0);
    }

    public SteamPixel(PhysicsManager manager, int x, int y){
        this(manager, x, y, MAX_STEAM_TEMPERATURE);
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
        return TEMP_CHANGE_FACTOR * concentration;
    }

    @Override
    public float getBaseTemperatureChangeFactor() {
        return TEMP_CHANGE_FACTOR * concentration;
    }

    @Override
    public void addToTemperature(TemperatureObject source, float amount) {
        temperature += amount;
        if(temperature <= MIN_STEAM_TEMPERATURE)
            getManager().setPixel(getIntX(), getIntY(), new WaterPixel(getManager(), getIntX(), getIntY(), temperature,
                    getVx(), getVy()));
        else if(temperature > MAX_STEAM_TEMPERATURE) temperature = MAX_STEAM_TEMPERATURE;
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
        if(temperature >= MAX_STEAM_TEMPERATURE) return new Color(R, G, B, concentration * 255);
        float tempRatio = PApplet.max(0, PApplet.min(1, (MAX_STEAM_TEMPERATURE - temperature)/(MAX_STEAM_TEMPERATURE -
                MIN_STEAM_TEMPERATURE)));

        Color baseWaterColor = WaterPixel.getBaseWaterColor();

        float r = R * (1 - tempRatio) + baseWaterColor.getR() * tempRatio;
        float g = G * (1 - tempRatio) + baseWaterColor.getG() * tempRatio;
        float b = B * (1 - tempRatio) + baseWaterColor.getB() * tempRatio;

        return new Color(r, g, b, concentration * 255);
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
    public float getDensity() {
        return DENSITY;
    }

    private void updateConcentration(){
        float steamCount = 0;
        for(PhysicsPixel p : getSurroundingPixels().values()) if(p != null && p.getType() == PhysicsPixelType.STEAM){
            steamCount++;
            SteamPixel s = (SteamPixel) p;
            float avgConcentration = (concentration + s.concentration)/2;
            concentration = (concentration + avgConcentration)/2;
            s.concentration = (s.concentration + avgConcentration)/2;
            //steamCount += ((SteamPixel) p).concentration;
        }

        concentration = CONCENTRATION_UPDATE_AMT * ((steamCount/8) - 0.01f) + (1 - CONCENTRATION_UPDATE_AMT) *
                concentration;
        concentration -= 0.001f;
    }

    @Override
    public void updateTemperature(){
        TempGridObject tempGridObject = getManager().getTempGridObject(getIntX(), getIntY());
        addToTemperature(tempGridObject, tempGridObject.getTemperature());

        for(PhysicsPixel p : getSurroundingPixels().values()){
            if(!(p instanceof TemperatureObject)) continue;
            TemperatureObject t = (TemperatureObject) p;
            if(t instanceof DynamicPixel) TemperatureObject.swapTemperatures(this, t, concentration * 0.5f);
            else TemperatureObject.swapTemperatures(this, t, concentration);

            if(temperature < MIN_STEAM_TEMPERATURE) break;
        }
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
        updateTemperature();
        if(concentration < MIN_CONCENTRATION){
            getManager().setPixel(getIntX(), getIntY(), null);
            return;
        }
    }
}
