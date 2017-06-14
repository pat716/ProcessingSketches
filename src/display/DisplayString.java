package display;

import processing.core.PApplet;
import processing.core.PGraphics;
import sketch.Sketch;

/**
 * Created by psweeney on 3/5/17.
 */
public class DisplayString {
    public static String getFloatStringForDecimalPlace(float value, int decimalPlace){
        float mult = PApplet.pow(10, PApplet.max(0, decimalPlace));
        return ((float) ((int) (value * mult)) / mult) + "";
    }

    private String text;
    private Color color;
    private float textSize;

    public DisplayString(String text, Color color, float textSize){
        this.text = text;
        this.color = color;
        this.textSize = textSize;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public float getTextSize() {
        return textSize;
    }

    public void setTextSize(float textSize) {
        this.textSize = textSize;
    }

    public void draw(PGraphics canvas, Color.ColorMode colorMode, float xOffset, float yOffset){
        canvas.textSize(textSize);
        canvas.fill(color.convert(canvas, colorMode));
        canvas.text(getText(), xOffset, yOffset + getTextSize());
    }
}
