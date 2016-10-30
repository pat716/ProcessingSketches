package datatypes.pixel.physics;

import datatypes.pixel.Color;
import datatypes.pixel.physics.dynamic.FirePixel;
import processing.core.PApplet;

/**
 * Created by psweeney on 9/26/16.
 */
public interface TemperatureObject {
    public float getTemperature();
    public boolean hasStaticTemp();
    public float getBaseTemperatureChangeFactor();
    public float getTemperatureChangeFactor(TemperatureObject t, float amount);
    public void addToTemperature(TemperatureObject source, float amount);
    public void updateTemperature();

    public static void addStaticTemp(TemperatureObject source, TemperatureObject dest){
        if(source == null || dest == null || !source.hasStaticTemp() || dest.hasStaticTemp()) return;

        float addAmount = source.getTemperature() - dest.getTemperature();
        addAmount *= dest.getTemperatureChangeFactor(source, addAmount);

        dest.addToTemperature(source, addAmount);
    }

    public static void swapTemperatures(TemperatureObject t1, TemperatureObject t2, float ratio){
        if(t1 == null || t2 == null || (t1.hasStaticTemp() && t2.hasStaticTemp())) return;

        if(t1.hasStaticTemp() && !t2.hasStaticTemp()){
            addStaticTemp(t1, t2);
            return;
        } else if(t2.hasStaticTemp() && !t1.hasStaticTemp()){
            addStaticTemp(t2, t1);
            return;
        }

        float t1Temp = t1.getTemperature(), t2Temp = t2.getTemperature();

        float addAmount = (t2Temp - t1Temp);
        float t1CF = t1.getTemperatureChangeFactor(t2, addAmount), t2CF = t2.getTemperatureChangeFactor(t1,
                -addAmount);

        if(t1CF < 0 || t2CF < 0) return;

        addAmount *= (t1CF + t2CF) / 2;

        float t1AddRatio = t1CF/(t1CF + t2CF);

        t1.addToTemperature(t2, addAmount * t1AddRatio);
        t2.addToTemperature(t1, addAmount * -1 * (1 - t1AddRatio));
    }

    public static class TempGridObject implements TemperatureObject{
        public static final float DEFAULT_TEMP_GRID_AMOUNT = 0.1f;
        private static final float HEAT_R = 255, HEAT_G = 150, HEAT_B = 75;
        private static final float COOL_R = 75, COOL_G = 125, COOL_B = 255;
        private static final float MAX_TEMP_GRID_ALPHA = 255;

        public static final float MIN_GRID_TEMP = -0.5f;
        public static final float MAX_GRID_TEMP = 0.5f;

        private float temperature;

        public TempGridObject(float temperature){
            this.temperature = temperature;
        }

        @Override
        public float getTemperature() {
            return temperature;
        }

        @Override
        public boolean hasStaticTemp() {
            return true;
        }

        @Override
        public float getBaseTemperatureChangeFactor() {
            return 0;
        }

        @Override
        public float getTemperatureChangeFactor(TemperatureObject t, float amount) {
            return 0;
        }

        @Override
        public void addToTemperature(TemperatureObject source, float amount) {
            if(!(source instanceof TempGridObject)) return;
            temperature = PApplet.max(MIN_GRID_TEMP, PApplet.min(MAX_GRID_TEMP, temperature + amount));
        }

        public static Color getHeatColor(){
            return new Color(HEAT_R, HEAT_G, HEAT_B, MAX_TEMP_GRID_ALPHA);
        }

        public static Color getCoolColor(){
            return new Color(COOL_R, COOL_G, COOL_B, MAX_TEMP_GRID_ALPHA);
        }

        public Color getColor(){
            if(temperature == 0) return new Color(0, 0, 0, 0);
            float r, g, b, a;
            if(temperature > 0){
                float ratio = temperature/MAX_GRID_TEMP;
                r = HEAT_R;
                g = HEAT_G;
                b = HEAT_B;
                a = ratio * MAX_TEMP_GRID_ALPHA;
            } else {
                float ratio = temperature/MIN_GRID_TEMP;
                r = COOL_R;
                g = COOL_G;
                b = COOL_B;
                a = ratio * MAX_TEMP_GRID_ALPHA;
            }

            return new Color(r, g, b, a);
        }

        @Override
        public void updateTemperature() {}
    }
}
