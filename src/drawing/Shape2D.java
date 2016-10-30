package drawing;

import physics.MotionPath;
import physics.SimpleMotionPath;
import processing.core.PApplet;

import java.util.Iterator;

/**
 * Created by psweeney on 9/13/16.
 */
public abstract class Shape2D {
    public static float motionBlurAlphaCoefficient = PApplet.sqrt(2);
    public static float motionBlurMinFrameAlpha = 0;
    public static float motionBlurNumSteps = 20;
    public static boolean motionBlurLimitNumSteps = false;

    public static void setMotionBlurAlphaCoefficient(float motionBlurAlphaCoefficient){
        Shape2D.motionBlurAlphaCoefficient = motionBlurAlphaCoefficient;
    }

    public static class DrawFlags{
        private PApplet applet;
        private int blendMode, strokeColor, fillColor;
        private float strokeWeight;
        private boolean useStroke, useFill;
        private MotionPath blurPath;

        private DrawFlags(PApplet applet){
            this.applet = applet;
            blendMode = PApplet.NORMAL;
            strokeColor = 0;
            fillColor = 0;
            strokeWeight = 0;
            useStroke = false;
            useFill = true;
            blurPath = null;
        }

        public DrawFlags(PApplet applet, int blendMode, int strokeColor, int fillColor, float strokeWeight, boolean useStroke,
                         boolean useFill, MotionPath blurPath){
            this(applet);
            this.blendMode = blendMode;
            this.strokeColor = strokeColor;
            this.fillColor = fillColor;
            this.strokeWeight = strokeWeight;
            this.useStroke = useStroke;
            this.useFill = useFill;
            this.blurPath = blurPath;
        }

        public DrawFlags(PApplet applet, int blendMode, int strokeColor, int fillColor, float strokeWeight,
                         boolean useStroke, boolean useFill){
            this(applet, blendMode, strokeColor, fillColor, strokeWeight, useStroke, useFill, null);
        }

        public int getBlendMode() {
            return blendMode;
        }

        public void setBlendMode(int blendMode) {
            this.blendMode = blendMode;
        }

        private static int getColorWithMotionBlurAlpha(PApplet applet, int color){
            float frameAlpha = PApplet.min(255, PApplet.max(motionBlurMinFrameAlpha, motionBlurAlphaCoefficient *
                    applet.alpha(color)) / motionBlurNumSteps);
            return applet.color(applet.red(color), applet.green(color), applet.blue(color), frameAlpha);
        }

        private static int getColorWithMotionBlurAlpha(PApplet applet, int color, float distance){
            if(distance <= PApplet.sqrt(2)){
                return color;
            }

            if(motionBlurLimitNumSteps && distance >= motionBlurNumSteps){
                return getColorWithMotionBlurAlpha(applet, color);
            }

            float frameAlpha = PApplet.min(255, PApplet.max(motionBlurMinFrameAlpha, motionBlurAlphaCoefficient *
                    applet.alpha(color)) / distance);
            return applet.color(applet.red(color), applet.green(color), applet.blue(color), frameAlpha);
        }

        public int getStrokeColor() {
            if(!useStroke){
                return applet.color(0, 0, 0, 0);
            }

            if(blurPath != null && blurPath.getDistance() > PApplet.sqrt(2)){
                return getColorWithMotionBlurAlpha(applet, strokeColor, blurPath.getDistance());
            }
            return strokeColor;
        }

        public void setStrokeColor(int strokeColor) {
            this.strokeColor = strokeColor;
        }

        public int getFillColor() {
            if(!useFill){
                return applet.color(0, 0, 0, 0);
            }

            if(blurPath != null && blurPath.getDistance() > PApplet.sqrt(2)){
                return getColorWithMotionBlurAlpha(applet, fillColor, blurPath.getDistance());
            }
            return fillColor;
        }

        public void setFillColor(int fillColor) {
            this.fillColor = fillColor;
        }

        public float getStrokeWeight() {
            return strokeWeight;
        }

        public void setStrokeWeight(float strokeWeight) {
            this.strokeWeight = strokeWeight;
        }

        public MotionPath getBlurPath(){
            return blurPath;
        }

        public void setBlurPath(MotionPath blurPath) {
            this.blurPath = blurPath;
        }

        public boolean useStroke() {
            return useStroke;
        }

        public void setUseStroke(boolean useStroke) {
            this.useStroke = useStroke;
        }

        public boolean useFill() {
            return useFill;
        }

        public void setUseFill(boolean useFill) {
            this.useFill = useFill;
        }
    }

    protected PApplet applet;
    protected DrawFlags flags;

    public DrawFlags getFlags() {
        return flags;
    }

    public void setFlags(DrawFlags flags) {
        this.flags = flags;
    }

    private void prepareDraw(){
        applet.blendMode(flags.getBlendMode());
        if(flags.useStroke){
            applet.strokeWeight(flags.getStrokeWeight());
            applet.stroke(flags.getStrokeColor());
        } else {
            applet.noStroke();
        }

        if(flags.useFill){
            applet.fill(flags.getFillColor());
        } else {
            applet.noFill();
        }
    }

    protected abstract void callDraw();

    protected abstract void callDraw(float xOffset, float yOffset);

    protected abstract void callDraw(float xOffset, float yOffset, float rotation);

    public void drawShape(){
        prepareDraw();
        MotionPath blurPath = flags.getBlurPath();
        if(blurPath != null && blurPath.getDistance() > 1){
            Iterator<SimpleMotionPath.MotionState> blurPathIterator = blurPath.getPathIterator(motionBlurLimitNumSteps,
                    motionBlurNumSteps);
            while (blurPathIterator.hasNext()){
                SimpleMotionPath.MotionState currentState = blurPathIterator.next();
                if(currentState.getAngleR() != 0){
                    callDraw(currentState.getX(), currentState.getY(), currentState.getAngleR());
                } else {
                    callDraw(currentState.getX(), currentState.getY());
                }
            }

            return;
        }
        callDraw();
    }
}
