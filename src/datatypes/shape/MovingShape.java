package datatypes.shape;

import display.Color;
import display.DrawOptions;
import processing.core.PApplet;
import processing.core.PGraphics;

/**
 * Created by psweeney on 6/12/17.
 */
public class MovingShape {
    public static float getAlphaMultiplierForDistance(float distance){
        if(distance <= 1){
            return 1;
        }
        return PApplet.min(1, PApplet.max(0, 0.05f + 1/PApplet.pow(distance, 0.9f)));
    }

    public static enum MotionDrawMode{
        START, END, MIDDLE, BLUR
    }

    private Shape start, end;
    private float distance, shapeIterationAlphaMultiplier;


    public MovingShape(Shape start, Shape end){
        this.start = start;
        this.end = end;
        distance = start.getCenterPoint().dist(end.getCenterPoint());
        shapeIterationAlphaMultiplier = getAlphaMultiplierForDistance(distance);
    }

    public void drawMovingShape(PGraphics canvas, Color.ColorMode colorMode, MotionDrawMode motionDrawMode){
        switch (motionDrawMode) {
            case START:
                start.drawShape(canvas, colorMode);
                break;
            case END:
                end.drawShape(canvas, colorMode);
                break;
            case MIDDLE:
                start.combine(end, 0.5f).drawShape(canvas, colorMode);
                break;
            case BLUR:
                if(distance < 1){
                    drawMovingShape(canvas, colorMode, MotionDrawMode.START);
                    break;
                }

                for (float i = 0; i < distance; i++) {
                    float progressRatio = i / distance;
                    Shape currShape = start.combine(end, progressRatio);
                    DrawOptions drawOptions = currShape.getDrawOptions();
                    Color newFillColor = drawOptions.getFillColor(),
                            newStrokeColor = drawOptions.getStrokeColor();
                    if(drawOptions.useFill() && newFillColor != null){
                        newFillColor = newFillColor.multiplyAlpha(shapeIterationAlphaMultiplier);
                    }

                    if(drawOptions.useStroke() && newStrokeColor != null){
                        newStrokeColor = newStrokeColor.multiplyAlpha(shapeIterationAlphaMultiplier);
                    }
                    drawOptions = new DrawOptions(drawOptions.useFill(), drawOptions.useStroke(), newFillColor,
                            newStrokeColor, drawOptions.getStrokeWeight(), drawOptions.getBlendMode());
                    currShape.setDrawOptions(drawOptions);
                    currShape.drawShape(canvas, colorMode);

                }
                break;
            default:
                break;
        }
    }
}
