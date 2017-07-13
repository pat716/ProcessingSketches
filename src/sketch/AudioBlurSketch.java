package sketch;

import controller.SketchController;
import ddf.minim.Minim;
import display.Color;
import display.ColorSpectrum;
import model.AudioBlurSketchDataModel;
import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PGraphics;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Map;

/**
 * Created by psweeney on 2/28/17.
 */
public class AudioBlurSketch extends Sketch {
    public enum AudioBlurSketchMode{
        REALTIME_MP3, REALTIME_MIC, RECORD, RECORD_TEST
    }

    public static AudioBlurSketchMode startMode = AudioBlurSketchMode.RECORD;

    PGraphics primarySoundballGraphics;
    PGraphics secondarySoundballGraphics;
    PGraphics tertiarySoundballGraphics;
    PGraphics bloomGraphics;

    private static int SOUND_BALL_BLEND_MODE = NORMAL;
    private static float bloomGraphicsSizeFactor = 0.5f;

    private AudioBlurSketchDataModel abModel;

    public static int getSoundBallBlendMode() {
        return SOUND_BALL_BLEND_MODE;
    }

    public static void setSoundBallBlendMode(int soundBallBlendMode) {
        SOUND_BALL_BLEND_MODE = soundBallBlendMode;
    }

    public AudioBlurSketchMode sketchMode = AudioBlurSketchMode.REALTIME_MP3;

    @Override
    public void sketchSettings() {
        if(startMode == AudioBlurSketchMode.REALTIME_MP3 || startMode == AudioBlurSketchMode.REALTIME_MIC){
            size(800, 600);
        } else {
            size(1920, 1080);
        }
        //fullScreen();
    }

    @Override
    public void sketchSetup() {
        if(startMode == AudioBlurSketchMode.RECORD || startMode == AudioBlurSketchMode.RECORD_TEST) {
            frameRate(24);
        } else {
            frameRate(60);
        }
        abModel = new AudioBlurSketchDataModel(new SketchController(), this, startMode);
        setModel(abModel);
        ellipseMode(CENTER);
        rectMode(CENTER);

        primarySoundballGraphics = createGraphics(width, height);
        //primarySoundballGraphics.noSmooth();
        primarySoundballGraphics.beginDraw();
        primarySoundballGraphics.background(0);
        primarySoundballGraphics.endDraw();

        secondarySoundballGraphics = createGraphics(width, height);
        secondarySoundballGraphics.noSmooth();
        secondarySoundballGraphics.beginDraw();
        secondarySoundballGraphics.background(0);
        secondarySoundballGraphics.rectMode(CORNER);
        secondarySoundballGraphics.fill(0);
        secondarySoundballGraphics.noStroke();
        secondarySoundballGraphics.endDraw();

        tertiarySoundballGraphics = createGraphics(width, height);
        tertiarySoundballGraphics.noSmooth();
        tertiarySoundballGraphics.beginDraw();
        tertiarySoundballGraphics.background(0);
        tertiarySoundballGraphics.endDraw();

        bloomGraphics = createGraphics((int) (((float) width) * bloomGraphicsSizeFactor),
                (int) (((float) height) * bloomGraphicsSizeFactor));
        bloomGraphics.noSmooth();
        bloomGraphics.beginDraw();
        bloomGraphics.background(0, 0);
        bloomGraphics.endDraw();
    }

    private void preparePrimarySoundballGraphics(){
        if(abModel.isPaused()) return;
        primarySoundballGraphics.beginDraw();
        primarySoundballGraphics.blendMode(SOUND_BALL_BLEND_MODE);
        //primarySoundballGraphics.blendMode(PConstants.NORMAL);
        getModel().applyModelStateToSketch(primarySoundballGraphics, COLOR_MODE);
        primarySoundballGraphics.endDraw();
    }

    private void prepareSecondarySoundballGraphics(){
        boolean whiteMode = abModel.isWhiteModeActivated();
        if(abModel.isPaused()) return;
        secondarySoundballGraphics.beginDraw();
        secondarySoundballGraphics.imageMode(CENTER);
        if(!abModel.useVariableAlphaBuffers() || abModel.getSecondarySoundballGraphicsClearAlpha() == 255){
            if(whiteMode) secondarySoundballGraphics.background(255, 0);
            else secondarySoundballGraphics.background(0, 0);
        } else {
            if(whiteMode){
                secondarySoundballGraphics.fill(255, abModel.getSecondarySoundballGraphicsClearAlpha());
            } else {
                secondarySoundballGraphics.fill(0, abModel.getSecondarySoundballGraphicsClearAlpha());
            }
            secondarySoundballGraphics.blendMode(SOUND_BALL_BLEND_MODE);
            //secondarySoundballGraphics.blendMode(PConstants.NORMAL);
            secondarySoundballGraphics.rect(0, 0,
                    secondarySoundballGraphics.width, secondarySoundballGraphics.height);
        }

        if(whiteMode) secondarySoundballGraphics.blendMode(PConstants.NORMAL);
        else secondarySoundballGraphics.blendMode(SOUND_BALL_BLEND_MODE);

        if(abModel.useVariableAlphaBuffers() && abModel.getPrimarySoundballGraphicsImageAlpha() != 255){
            secondarySoundballGraphics.tint(255, abModel.getPrimarySoundballGraphicsImageAlpha());
        }
        secondarySoundballGraphics.image(primarySoundballGraphics,
                secondarySoundballGraphics.width/2,
                secondarySoundballGraphics.height/2,
                secondarySoundballGraphics.width,
                secondarySoundballGraphics.height);
        secondarySoundballGraphics.noTint();
        secondarySoundballGraphics.endDraw();
    }

    private void prepareTertiarySoundballGraphics(){
        boolean whiteMode = abModel.isWhiteModeActivated();
        tertiarySoundballGraphics.beginDraw();
        tertiarySoundballGraphics.blendMode(NORMAL);
        tertiarySoundballGraphics.background(0, 0);
        tertiarySoundballGraphics.image(secondarySoundballGraphics.get(), 0, 0, tertiarySoundballGraphics.width, tertiarySoundballGraphics.height);
        if(!whiteMode && abModel.isBloomEnabled()){
            bloomGraphics.beginDraw();
            bloomGraphics.fill(0, 230);
            bloomGraphics.rect(0, 0, bloomGraphics.width, bloomGraphics.height);
            //bloomGraphics.background(0, 0);
            bloomGraphics.image(secondarySoundballGraphics.get(), 0, 0, bloomGraphics.width, bloomGraphics.height);
            bloomGraphics.endDraw();
            bloomGraphics.filter(PConstants.BLUR, 16f);
            tertiarySoundballGraphics.blendMode(ADD);
            tertiarySoundballGraphics.image(bloomGraphics, 0, 0, width, height);
        }
        tertiarySoundballGraphics.endDraw();
    }

    int lastFrameRendered = 0;
    @Override
    public void sketchDraw() {
        getModel().applyControllerStateToModel();
        if(abModel.recordingMode){
            if(!abModel.recordingFinished){
                abModel.applyModelStateToSketch(primarySoundballGraphics, COLOR_MODE);
            } else {
                if(abModel.snapshotRenderingFinished){
                    if(AudioBlurSketchDataModel.mp3FileName.equals(AudioBlurSketchDataModel.RAWM_FILENAME) ||
                                AudioBlurSketchDataModel.mp3FileName.equals(AudioBlurSketchDataModel.TYB_FILENAME)){
                        abModel.recordingFinished = false;
                        abModel.snapshotRenderingFinished = false;
                        abModel.nextSong();
                    } else {
                        exit();
                    }
                } else if(abModel.isPaused()){
                    frameCount = lastFrameRendered;
                    return;
                }

                background(0, 0);
                preparePrimarySoundballGraphics();
                prepareSecondarySoundballGraphics();
                prepareTertiarySoundballGraphics();
                image(tertiarySoundballGraphics, 0, 0, width, height);
                if(AudioBlurSketchDataModel.mp3FileName.equals(AudioBlurSketchDataModel.RAWM_FILENAME)) {
                    saveFrame("data/recordings/RAWM/RAWM-recorded-image-#######.png");
                } else if(AudioBlurSketchDataModel.mp3FileName.equals(AudioBlurSketchDataModel.TYB_FILENAME)){
                    saveFrame("data/recordings/TYB/TYB-recorded-image-#######.png");
                } else if(AudioBlurSketchDataModel.mp3FileName.equals(AudioBlurSketchDataModel.FEVER_FILENAME)){
                    saveFrame("data/recordings/FEVER/FEVER-recorded-image-#######.png");
                }

                lastFrameRendered = frameCount;

                float progressBarWidth = abModel.getRecordedSnapshotProgress() * width;

                fill(255);
                noStroke();
                rectMode(CORNER);
                rect(0, height/2 - 20, progressBarWidth, 40);
            }
        } else {
            blendMode(NORMAL);
            boolean whiteMode = abModel.isWhiteModeActivated();
            if (whiteMode) background(255, 255);
            else background(0, 255);
            getModel().applyControllerStateToModel();

            preparePrimarySoundballGraphics();
            prepareSecondarySoundballGraphics();
            prepareTertiarySoundballGraphics();

            image(tertiarySoundballGraphics, 0, 0, width, height);
        }
    }

    public static void main(String[] args){
        PApplet.main("sketch.AudioBlurSketch");
    }
}
