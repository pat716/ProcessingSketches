package datatypes.shape;

import datatypes.geom.Vector;
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
        return PApplet.min(1, PApplet.max(0, 0.02f + 1/PApplet.pow(distance, 0.6f)));
    }

    public static enum MotionDrawMode{
        START, MIDDLE, END, BLUR
    }

    public static MotionDrawMode getNextMotionDrawMode(MotionDrawMode drawMode){
        switch (drawMode){
            case START:
                return MotionDrawMode.MIDDLE;
            case MIDDLE:
                return MotionDrawMode.END;
            case END:
                return MotionDrawMode.BLUR;
            case BLUR:
                return MotionDrawMode.START;
        }
        return MotionDrawMode.START;
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
        Vector startPoint = start.getCenterPoint(), endPoint = end.getCenterPoint();
        float x1 = startPoint.getX(), y1 = startPoint.getY(), x2 = endPoint.getX(), y2 = endPoint.getY();
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
