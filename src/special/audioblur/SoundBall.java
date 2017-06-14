package special.audioblur;

import datatypes.geom.Vector;
import datatypes.geom.container.twodimensional.RectangleContainer2D;
import datatypes.shape.Ellipse;
import datatypes.shape.MovingShape;
import display.Color;
import display.DrawOptions;
import processing.core.PApplet;
import processing.core.PGraphics;
import sketch.AudioBlurSketch;

import static processing.core.PApplet.min;
import static processing.core.PApplet.cos;
import static processing.core.PApplet.sin;
import static processing.core.PConstants.NORMAL;
import static processing.core.PConstants.PI;
import static processing.core.PConstants.TWO_PI;
import static sketch.Sketch.randomFloat;

/**
 * Created by psweeney on 6/12/17.
 */
public class SoundBall {
    private static final float MIN_BOTTOM_ROTATION_SPEED = PI/1024;
    private static final float MAX_BOTTOM_ROTATION_SPEED = PI/128;
    private static final float RANDOM_BOTTOM_ROTATION_SPEED_AMOUNT = PI/256;
    private static final float MIN_BOTTOM_Z = 0;
    private static final float MAX_BOTTOM_Z = 50;
    private static final float RANDOM_Z_AMOUNT = 0;
    private static final float MIN_RADIUS = 1.5f;
    private static final float MAX_RADIUS = 8;
    private static final float RANDOM_RADIUS_AMOUNT = 0.5f;
    private static final float VZ_MULTIPLIER = .005f;
    private static final float MIN_Z_GRAVITY = 1f;
    private static final float Z_GRAVITY_MULTIPLIER = 5f;
    private static final float Z_MULTIPLIER = 2f;

    public static SoundBall generateRandomSoundBall(FFTHelper fftHelper){
        int band = (int) (PApplet.sqrt(randomFloat()) * ((float) fftHelper.getNumBands()));
        return new SoundBall(fftHelper, band);
    }

    private FFTHelper fftHelper;
    private int band;
    private float bandRatio, bandAmplitudeRatio, radius, rotation, bottomRotationSpeed,
            minZ, z, vz, prevX, prevY, prevZ;
    private Color bandColor;

    public SoundBall(FFTHelper fftHelper, int band){
        this.fftHelper = fftHelper;
        this.band = band;
        bandRatio = ((float) band) / ((float) fftHelper.getNumBands());


        bottomRotationSpeed = MIN_BOTTOM_ROTATION_SPEED + randomFloat() * bandRatio * (MAX_BOTTOM_ROTATION_SPEED - MIN_BOTTOM_ROTATION_SPEED);
        bottomRotationSpeed += bandRatio * randomFloat(RANDOM_BOTTOM_ROTATION_SPEED_AMOUNT);
        if(randomFloat() > 0.5f){
            bottomRotationSpeed *= -1;
        }

        rotation = randomFloat(TWO_PI);

        minZ = MIN_BOTTOM_Z + PApplet.sqrt(bandRatio) * (MAX_BOTTOM_Z - MIN_BOTTOM_Z);
        minZ += bandRatio * randomFloat(RANDOM_Z_AMOUNT);
        z = minZ;
        vz = 0;

        prevX = getX();
        prevY = getY();
        prevZ = minZ;
        rotation += bottomRotationSpeed;

        radius = MIN_RADIUS + (1 - bandRatio) * (MAX_RADIUS - MIN_RADIUS);
        radius += (1 - bandRatio) * randomFloat() * RANDOM_RADIUS_AMOUNT;

        bandColor = fftHelper.getColorForBand(band);
    }

    public float getX(){
        return fftHelper.getModel().getSketch().width/2 - cos(rotation) * z * Z_MULTIPLIER;
    }

    public float getY(){
        return fftHelper.getModel().getSketch().height/2 - sin(rotation) * z * Z_MULTIPLIER;
    }

    public void updateZ() {
        float newZ = minZ +
                randomFloat(0.99f, 1.01f) * (PApplet.pow(fftHelper.getVolumeMultipliedBand(band)/2, 0.8f) *
                        (0.25f + 3 * bandRatio/4) * 400)/(PApplet.sqrt(z));
        vz -= MIN_Z_GRAVITY + Z_GRAVITY_MULTIPLIER * bandRatio;
        if(newZ > z){
            float vz_addition = (newZ - z) * VZ_MULTIPLIER * (0.2f + PApplet.pow(2 * bandRatio/4 + 0.75f, 2));
            if(vz + vz_addition < vz_addition){
                vz = vz_addition;
            } else {
                vz += vz_addition;
            }

        }

        z += vz;
        if(z < minZ){
            z = minZ;
            vz = 0;
        }
    }

    public float getRadius() {
        return radius;
    }

    public void update(){
        prevX = getX();
        prevY = getY();
        prevZ = PApplet.dist(fftHelper.getModel().getSketch().width/2, fftHelper.getModel().getSketch().height/2, prevX, prevY);
        updateZ();
        bandColor = fftHelper.getColorForBand(band);
        rotation += bottomRotationSpeed + 2f * bottomRotationSpeed * PApplet.sqrt(z/minZ);
    }

    public void draw(PGraphics canvas, Color.ColorMode colorMode, MovingShape.MotionDrawMode motionDrawMode){
        float x = getX(), y = getY();
        float travelDist = PApplet.dist(prevX, prevY, x, y);
        if(travelDist > radius * 4 && fftHelper.getModel().isFastBlurEnabled()){
            canvas.blendMode(NORMAL);
            canvas.stroke(
                    bandColor.multiplyAlpha(
                            MovingShape.getAlphaMultiplierForDistance(travelDist) * radius
                    * (0.5f)).convert(canvas, colorMode));

            canvas.strokeWeight(radius);
            canvas.line(prevX, prevY, x, y);
            float xExtraAmount = ((x - prevX)/travelDist) * radius/2, yExtraAmount = ((y - prevY)/travelDist) * radius/2;
            canvas.line(prevX - xExtraAmount, prevY - yExtraAmount, x + xExtraAmount, y + yExtraAmount);
            return;
        }
        Ellipse start = new Ellipse(prevX, prevY, radius, new DrawOptions(
                true,
                false,
                bandColor,
                null,
                0,
                AudioBlurSketch.getSoundBallBlendMode())),

                end = new Ellipse(getX(), getY(), radius, new DrawOptions(
                true,
                false,
                bandColor,
                null,
                0,
                AudioBlurSketch.getSoundBallBlendMode())
                );
        Vector startPoint = start.getCenterPoint(), endPoint = end.getCenterPoint();
        MovingShape movingShape = new MovingShape(start, end);
        RectangleContainer2D canvasContainer = new RectangleContainer2D(0, 0, canvas.width, canvas.height);

        if (!(canvasContainer.inBounds(startPoint) || canvasContainer.inBounds(endPoint))) return;

        movingShape.drawMovingShape(canvas, colorMode, motionDrawMode);
    }
}
