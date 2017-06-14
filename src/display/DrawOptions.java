package display;

import processing.core.PGraphics;

/**
 * Created by psweeney on 3/7/17.
 */
public class DrawOptions {
    private boolean fill, stroke;
    private Color fillColor, strokeColor;
    private float strokeWeight;
    private int blendMode;

    public DrawOptions(boolean fill, boolean stroke, Color fillColor, Color strokeColor, float strokeWeight,
                       int blendMode){
        this.fill = fill;
        this.stroke = stroke;
        this.fillColor = fillColor;
        this.strokeColor = strokeColor;
        this.strokeWeight = strokeWeight;
        this.blendMode = blendMode;
    }

    public boolean useFill(){
        return fill;
    }

    public boolean useStroke(){
        return stroke;
    }

    public Color getFillColor() {
        return fillColor;
    }

    public Color getStrokeColor() {
        return strokeColor;
    }

    public float getStrokeWeight() {
        return strokeWeight;
    }

    public int getBlendMode() {
        return blendMode;
    }

    public void applyToPGraphics(PGraphics canvas, Color.ColorMode colorMode){
        if(fill && fillColor != null) canvas.fill(fillColor.convert(canvas, colorMode));
        else canvas.noFill();

        if(stroke && strokeColor != null){
            canvas.stroke(strokeColor.convert(canvas, colorMode));
            canvas.strokeWeight(strokeWeight);
        } else canvas.noStroke();

        canvas.blendMode(blendMode);
    }

    public void applyToPGraphics(PGraphics canvas, Color.ColorMode colorMode, float alphaMultiplier){
        if(fill && fillColor != null){
            Color tempFillColor = new Color(fillColor.getR(), fillColor.getG(), fillColor.getB(),
                    fillColor.getA() * alphaMultiplier, 1);
            canvas.fill(tempFillColor.convert(canvas, colorMode));
        } else canvas.noFill();

        if(stroke && strokeColor != null){
            Color tempStrokeColor = new Color(strokeColor.getR(), strokeColor.getG(), strokeColor.getB(),
                    strokeColor.getA() * alphaMultiplier, 1);
            canvas.stroke(tempStrokeColor.convert(canvas, colorMode));
            canvas.strokeWeight(strokeWeight);
        } else canvas.noStroke();
        canvas.blendMode(blendMode);
    }

    public DrawOptions combine(DrawOptions other, float otherRatio){
        boolean newFill, newStroke;
        int newBlendMode;

        if(otherRatio <= 0.5f){
            newFill = fill;
            newStroke = stroke;
            newBlendMode = blendMode;
        } else {
            newFill = other.fill;
            newStroke = other.stroke;
            newBlendMode = other.blendMode;
        }

        Color newFillColor, newStrokeColor;

        if(fillColor != null && other.fillColor != null)
            newFillColor = fillColor.combine(other.fillColor, otherRatio);
        else if(fillColor != null && otherRatio <= 0.5f){
            newFillColor = fillColor;
        } else if(other.fillColor != null && otherRatio > 0.5f){
            newFillColor = other.fillColor;
        } else {
            newFill = false;
            newFillColor = null;
        }

        if(stroke && strokeColor != null && other.stroke && other.strokeColor != null)
            newStrokeColor = strokeColor.combine(other.strokeColor, otherRatio);
        else if(stroke && strokeColor != null && otherRatio <= 0.5f){
            newStrokeColor = strokeColor;
        } else if(other.stroke && other.strokeColor != null && otherRatio > 0.5f){
            newStrokeColor = other.strokeColor;
        } else {
            newStroke = false;
            newStrokeColor = null;
        }

        float newStrokeWeight = (1 - otherRatio) * strokeWeight + otherRatio * other.strokeWeight;

        return new DrawOptions(newFill, newStroke, newFillColor, newStrokeColor, newStrokeWeight, newBlendMode);
    }
}
