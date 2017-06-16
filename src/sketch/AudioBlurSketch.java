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
    PGraphics primarySoundballGraphics;
    PGraphics secondarySoundballGraphics;
    PGraphics tertiarySoundballGraphics;
    PGraphics bloomGraphics;

    private static int SOUND_BALL_BLEND_MODE = NORMAL;
    private static float bloomGraphicsSizeFactor = 0.25f;

    private AudioBlurSketchDataModel abModel;

    public static int getSoundBallBlendMode() {
        return SOUND_BALL_BLEND_MODE;
    }

    public static void setSoundBallBlendMode(int soundBallBlendMode) {
        SOUND_BALL_BLEND_MODE = soundBallBlendMode;
    }



    @Override
    public void sketchSettings() {
        size(800, 600);
        //fullScreen();
    }

    @Override
    public void sketchSetup() {
        abModel = new AudioBlurSketchDataModel(new SketchController(), this);
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
        primarySoundballGraphics.blendMode(PConstants.NORMAL);
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
            secondarySoundballGraphics.blendMode(PConstants.NORMAL);
            secondarySoundballGraphics.rect(0, 0,
                    secondarySoundballGraphics.width, secondarySoundballGraphics.height);
        }

        if(whiteMode) secondarySoundballGraphics.blendMode(PConstants.NORMAL);
        else secondarySoundballGraphics.blendMode(PConstants.NORMAL);

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
            bloomGraphics.fill(0, 100);
            bloomGraphics.rect(0, 0, bloomGraphics.width, bloomGraphics.height);
            //bloomGraphics.background(0, 0);
            bloomGraphics.image(secondarySoundballGraphics.get(), 0, 0, bloomGraphics.width, bloomGraphics.height);
            bloomGraphics.endDraw();
            bloomGraphics.filter(PConstants.BLUR, 4f);
            tertiarySoundballGraphics.blendMode(ADD);
            tertiarySoundballGraphics.image(bloomGraphics, 0, 0, width, height);
        }
        tertiarySoundballGraphics.endDraw();
    }

    @Override
    public void sketchDraw() {
        blendMode(NORMAL);
        boolean whiteMode = abModel.isWhiteModeActivated();
        if(whiteMode) background(255, 255);
        else background(0, 255);
        getModel().applyControllerStateToModel();

        preparePrimarySoundballGraphics();
        prepareSecondarySoundballGraphics();
        prepareTertiarySoundballGraphics();

        image(tertiarySoundballGraphics, 0, 0, width, height);



    }

    public static void main(String[] args){
        PApplet.main("sketch.AudioBlurSketch");
    }
}
