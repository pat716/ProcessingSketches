package display;


import processing.core.PApplet;
import processing.core.PGraphics;
import sketch.Sketch;

/**
 * Created by psweeney on 3/5/17.
 */
public class Color {
    public enum BlendMode{
        ADD, SUBTRACT, MULTIPLY, AVERAGE, HIGHEST, LOWEST
    }

    public static class ColorMode{
        private float rMax, gMax, bMax, aMax;
        public ColorMode(float rMax, float gMax, float bMax, float aMax){
            this.rMax = rMax;
            this.gMax = gMax;
            this.bMax = bMax;
            this.aMax = aMax;
        }

        public float getRMax() {
            return rMax;
        }

        public void setRMax(float rMax) {
            this.rMax = rMax;
        }

        public float getGMax() {
            return gMax;
        }

        public void setGMax(float gMax) {
            this.gMax = gMax;
        }

        public float getBMax() {
            return bMax;
        }

        public void setBMax(float bMax) {
            this.bMax = bMax;
        }

        public float getAMax() {
            return aMax;
        }

        public void setAMax(float aMax) {
            this.aMax = aMax;
        }
    }

    private static float constrain(float f){
        return PApplet.max(0, PApplet.min(1, f));
    }

    private static float add(float v1, float v2){
        return constrain(v1 + v2);
    }

    private static float subtract(float v1, float v2){
        return constrain(v1 - v2);
    }

    private static float multiply(float v1, float v2){
        return constrain(v1 * v2);
    }

    private static float average(float v1, float v2){
        return constrain((v1 + v2)/2);
    }

    private static float highest(float v1, float v2){
        return constrain(PApplet.max(v1, v2));
    }

    private static float lowest(float v1, float v2){
        return constrain(PApplet.min(v1, v2));
    }

    private static float blend(float v1, float v2, BlendMode mode){
        switch (mode){
            case ADD:
                return add(v1, v2);
            case SUBTRACT:
                return subtract(v1, v2);
            case MULTIPLY:
                return multiply(v1, v2);
            case HIGHEST:
                return highest(v1, v2);
            case LOWEST:
                return lowest(v1, v2);
            case AVERAGE:
            default:
                return average(v1, v2);
        }
    }

    private float r, g, b, a;

    public Color(float r, float g, float b, float a, float maxValue){
        this.r = constrain(r/maxValue);
        this.g = constrain(g/maxValue);
        this.b = constrain(b/maxValue);
        this.a = constrain(a/maxValue);
    }

    public Color(float r, float g, float b, float maxValue){
        this(r, g, b, maxValue, maxValue);
    }

    public Color(float gray, float a, float maxValue){
        this(gray, gray, gray, a, maxValue);
    }

    public Color(float gray, float maxValue){
        this(gray, maxValue, maxValue);
    }

    public Color(Color other){
        this.r = other.r;
        this.g = other.g;
        this.b = other.b;
        this.a = other.a;
    }

    public float getR() {
        return r;
    }

    public float getG() {
        return g;
    }

    public float getB() {
        return b;
    }

    public float getA() {
        return a;
    }

    public Color multiplyAlpha(float alphaMultiplier){
        return new Color(r, g, b, constrain(a * alphaMultiplier), 1);
    }

    public Color flatten(){
        return new Color(r * a, g * a, b * a, 1, 1);
    }

    public Color blend(Color other, BlendMode rMode, BlendMode gMode, BlendMode bMode, BlendMode aMode){
        return new Color(
                blend(r, other.r, rMode),
                blend(g, other.g, gMode),
                blend(b, other.b, bMode),
                blend(a, other.a, aMode)
        );
    }

    public Color blend(Color other, BlendMode colorMode, BlendMode alphaMode){
        return blend(other, colorMode, colorMode, colorMode, alphaMode);
    }

    public Color blend(Color other, BlendMode blendMode){
        return blend(other, blendMode, blendMode);
    }

    public int convert(PGraphics canvas, ColorMode colorMode){
        return canvas.color(r * colorMode.getRMax(), g * colorMode.getGMax(),
                b * colorMode.getBMax(), a * colorMode.getAMax());
    }

    public Color combine(Color other, float otherRatio){
        float cr = (1 - otherRatio) * r + otherRatio * other.r,
                cg = (1 - otherRatio) * g + otherRatio * other.g,
                cb = (1 - otherRatio) * b + otherRatio * other.b,
                ca = (1 - otherRatio) * a + otherRatio * other.a;
        return new Color(cr, cg, cb, ca);
    }
}
