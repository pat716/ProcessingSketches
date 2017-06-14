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
    private static int SOUND_BALL_BLEND_MODE = SUBTRACT;
    private boolean whiteMode = false;
    private AudioBlurSketchDataModel abModel;

    public static int getSoundBallBlendMode() {
        return SOUND_BALL_BLEND_MODE;
    }

    public static void setSoundBallBlendMode(int soundBallBlendMode) {
        SOUND_BALL_BLEND_MODE = soundBallBlendMode;
    }

    public boolean isWhiteModeActivated() {
        return whiteMode;
    }

    public void toggleWhiteMode(){
        whiteMode = !whiteMode;
        if(whiteMode){
            SOUND_BALL_BLEND_MODE = NORMAL;
        } else {
            SOUND_BALL_BLEND_MODE = LIGHTEST;
        }
    }

    @Override
    public void sketchSettings() {
        size(800, 600);
    }

    @Override
    public void sketchSetup() {
        abModel = new AudioBlurSketchDataModel(new SketchController(), this);
        setModel(abModel);
        ellipseMode(CENTER);
        rectMode(CENTER);

        if(whiteMode) SOUND_BALL_BLEND_MODE = NORMAL;
        else SOUND_BALL_BLEND_MODE = LIGHTEST;

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
    }

    private void preparePrimarySoundballGraphics(){
        if(abModel.isPaused()) return;
        primarySoundballGraphics.beginDraw();
        primarySoundballGraphics.blendMode(PConstants.NORMAL);
        getModel().applyModelStateToSketch(primarySoundballGraphics, COLOR_MODE);
        primarySoundballGraphics.endDraw();
    }

    private void prepareSecondarySoundballGraphics(){
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
        else secondarySoundballGraphics.blendMode(PConstants.ADD);

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

    @Override
    public void sketchDraw() {
        if(whiteMode) background(255, 255);
        else background(0, 255);
        getModel().applyControllerStateToModel();

        preparePrimarySoundballGraphics();
        prepareSecondarySoundballGraphics();

        image(secondarySoundballGraphics, 0, 0, width, height);
    }

    public static void main(String[] args){
        PApplet.main("sketch.AudioBlurSketch");
    }
}
