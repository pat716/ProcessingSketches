package special.audioblur;

import datatypes.geom.Vector;
import datatypes.geom.container.twodimensional.RectangleContainer2D;
import datatypes.shape.Circle;
import datatypes.shape.MovingShape;
import display.Color;
import display.DrawOptions;
import processing.core.PApplet;
import processing.core.PGraphics;
import sketch.AudioBlurSketch;

import static processing.core.PApplet.*;
import static processing.core.PConstants.NORMAL;
import static processing.core.PConstants.PI;
import static processing.core.PConstants.TWO_PI;
import static sketch.Sketch.randomFloat;

/**
 * Created by psweeney on 6/12/17.
 */
public class SoundBall {
    private static final float MIN_BOTTOM_ROTATION_SPEED = PI/2500;
    private static final float MAX_BOTTOM_ROTATION_SPEED = PI/500;
    private static final float RANDOM_BOTTOM_ROTATION_SPEED_AMOUNT = PI/1100;
    private static final float CENTER_BAND_MULTIPLIER = 1f;
    private static final float RANDOM_Z_AMOUNT = 2;
    private static final float VZ_MULTIPLIER = .025f;
    private static final float MIN_Z_GRAVITY = 1f;
    private static final float Z_GRAVITY_MULTIPLIER = 8f;
    private static final float BAND_RATIO_PHYSICS_PERCENTAGE = 0.98f;

    public static float minBottomZ = 0;
    public static float maxBottomZ = 40;
    public static float minRadius = 3f;
    public static float maxRadius = 30;
    public static float randomRadiusAmount = 2f;
    public static float zMultiplier = 4f;

    public static SoundBall generateRandomSoundBall(FFTHelper fftHelper){
        float band = PApplet.pow(randomFloat(), 1f/3.0f) * ((float) fftHelper.getNumBands() - 1);
        return new SoundBall(fftHelper, band);
    }

    private FFTSample fftSample;
    private float band;
    private float bandRatio, bandAmplitudeRatio, radius, rotation, bottomRotationSpeed,
            minZ, z, vz, prevX, prevY, prevZ;
    private Color bandColor;

    public SoundBall(FFTHelper fftHelper, float band){
        fftSample = fftHelper.getCurrentSample();
        this.band = band;
        bandRatio = band / ((float) fftHelper.getNumBands());

        bottomRotationSpeed = MIN_BOTTOM_ROTATION_SPEED + randomFloat() * bandRatio * (MAX_BOTTOM_ROTATION_SPEED - MIN_BOTTOM_ROTATION_SPEED);
        bottomRotationSpeed += bandRatio * randomFloat(RANDOM_BOTTOM_ROTATION_SPEED_AMOUNT);
        if(randomFloat() > 0.5f){
            bottomRotationSpeed *= -1;
        }

        rotation = randomFloat(TWO_PI);

        minZ = minBottomZ + PApplet.sqrt(bandRatio) * (maxBottomZ - minBottomZ);
        minZ += bandRatio * randomFloat(RANDOM_Z_AMOUNT);
        z = minZ;
        vz = 0;

        prevX = getX();
        prevY = getY();
        prevZ = minZ;
        rotation += bottomRotationSpeed;

        radius = minRadius + (1 - bandRatio) * (maxRadius - minRadius);
        radius += (1 - bandRatio) * randomFloat() * randomRadiusAmount;

        bandColor = fftSample.getColorForBand(band);
    }

    public float getX(){
        return fftSample.getModel().getSketch().width/2 - cos(rotation) * z * zMultiplier;
    }

    public float getY(){
        return fftSample.getModel().getSketch().height/2 - sin(rotation) * z * zMultiplier;
    }

    public float getBandForPosition(){
        float boundedRotation = rotation % TWO_PI;
        float centerBand = fftSample.getVolumeMultipliedBand(band) * CENTER_BAND_MULTIPLIER;
        if(boundedRotation >= PI/2 && boundedRotation < 3 * PI/2){
            float rightBand = fftSample.getVolumeMultipliedRightBand(band);
            float rightRatio = PApplet.max(0, PApplet.min(1,1 - abs(boundedRotation - PI)/(PI/2)));
            return rightRatio * rightBand + (1 - rightRatio) * centerBand;
        } else {
            float leftBand = fftSample.getVolumeMultipliedLeftBand(band);
            float leftRatio = 1;
            if(boundedRotation < PI/2){
                leftRatio = PApplet.max(0, PApplet.min(1, 1 - boundedRotation/(PI/2)));
            } else {
                leftRatio = PApplet.max(0, PApplet.min(1, 1 - abs(boundedRotation - TWO_PI)/(PI/2)));
            }
            return leftRatio * leftBand + (1 - leftRatio) * centerBand;
        }
    }

    public void updateZ(float zMultiplier) {
        float newZ = minZ +
                (randomFloat(0.999f, 1.001f) * (PApplet.pow(getBandForPosition()/2, 0.8f) *
                        ((1 - BAND_RATIO_PHYSICS_PERCENTAGE) + bandRatio * BAND_RATIO_PHYSICS_PERCENTAGE)
                        * 400)/(PApplet.sqrt(z))) * zMultiplier;
        vz -= MIN_Z_GRAVITY * zMultiplier + (Z_GRAVITY_MULTIPLIER * ((1 - BAND_RATIO_PHYSICS_PERCENTAGE) + bandRatio *
                BAND_RATIO_PHYSICS_PERCENTAGE)) * zMultiplier;
        if(newZ > z){
            float vz_addition = ((newZ - z) * VZ_MULTIPLIER * (0.2f + PApplet.pow((1 - BAND_RATIO_PHYSICS_PERCENTAGE)
                    + bandRatio * BAND_RATIO_PHYSICS_PERCENTAGE, 2))) * zMultiplier;
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

    public void update(FFTSample newSample, float frameRate){
        float frameRateMultiplier = 60/frameRate;
        fftSample = newSample;
        prevX = getX();
        prevY = getY();
        prevZ = PApplet.dist(fftSample.getModel().getSketch().width/2, fftSample.getModel().getSketch().height/2, prevX, prevY);
        rotation += bottomRotationSpeed * frameRateMultiplier + 2f * bottomRotationSpeed * PApplet.sqrt(z/minZ) * frameRateMultiplier;
        updateZ(frameRateMultiplier);
        bandColor = fftSample.getColorForBand(band);

    }

    public void draw(PGraphics canvas, Color.ColorMode colorMode, MovingShape.MotionDrawMode motionDrawMode){
        float x = getX(), y = getY();
        float travelDist = PApplet.dist(prevX, prevY, x, y);
        if(motionDrawMode == MovingShape.MotionDrawMode.BLUR && travelDist > radius * 4 && fftSample.getModel().isFastBlurEnabled()){
            canvas.blendMode(NORMAL);
            canvas.stroke(
                    bandColor.multiplyAlpha(
                            MovingShape.getAlphaMultiplierForDistance(travelDist) * radius
                    * (0.45f)).convert(canvas, colorMode));

            canvas.strokeWeight(radius);
            canvas.line(prevX, prevY, x, y);
            float xExtraAmount = ((x - prevX)/travelDist) * radius/2, yExtraAmount = ((y - prevY)/travelDist) * radius/2;
            canvas.line(prevX - xExtraAmount, prevY - yExtraAmount, x + xExtraAmount, y + yExtraAmount);
            return;
        }
        Circle start = new Circle(prevX, prevY, radius, new DrawOptions(
                true,
                false,
                bandColor,
                null,
                0,
                AudioBlurSketch.getSoundBallBlendMode())),

                end = new Circle(getX(), getY(), radius, new DrawOptions(
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
