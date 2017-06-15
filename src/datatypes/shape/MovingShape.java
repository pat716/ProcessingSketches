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
        return PApplet.min(1, PApplet.max(0, 0.05f + 1/PApplet.pow(distance, 0.9f)));
    }

    public static enum MotionDrawMode{
        START, MIDDLE, END, FASTEST_BLUR, FAST_BLUR, QUALITY_BLUR
    }

    public static MotionDrawMode getNextMotionDrawMode(MotionDrawMode drawMode){
        switch (drawMode){
            case START:
                return MotionDrawMode.MIDDLE;
            case MIDDLE:
                return MotionDrawMode.END;
            case END:
                return MotionDrawMode.FASTEST_BLUR;
            case FASTEST_BLUR:
                return MotionDrawMode.FAST_BLUR;
            case FAST_BLUR:
                return MotionDrawMode.QUALITY_BLUR;
            case QUALITY_BLUR:
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
            case FASTEST_BLUR:
                float strokeWeight = (start.getStrokeWeightForSimpleRendering() +
                        end.getStrokeWeightForSimpleRendering())/2;
                Color strokeColor = start.combine(end, 0.5f).getDrawOptions().getFillColor()
                        .multiplyAlpha(shapeIterationAlphaMultiplier * strokeWeight/2);
                canvas.stroke(strokeColor.convert(canvas, colorMode));
                canvas.strokeWeight(strokeWeight);
                canvas.line(x1, y1, x2, y2);
                break;
            case FAST_BLUR:
                strokeWeight = (start.getStrokeWeightForSimpleRendering() +
                        end.getStrokeWeightForSimpleRendering())/2;
                strokeColor = start.combine(end, 0.5f).getDrawOptions().getFillColor();
                canvas.stroke(strokeColor.multiplyAlpha(
                        shapeIterationAlphaMultiplier * strokeWeight * 0.5f)
                        .convert(canvas, colorMode));
                canvas.strokeWeight(strokeWeight);
                canvas.line(x1, y1, x2, y2);
                float xExtraAmount = ((x2 - x1)/distance) * strokeWeight/2,
                        yExtraAmount = ((y2 - y1)/distance) * strokeWeight/2;
                canvas.line(x1 - xExtraAmount, y1 - yExtraAmount, x2 + xExtraAmount, y2 + yExtraAmount);
                break;
            case QUALITY_BLUR:
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
