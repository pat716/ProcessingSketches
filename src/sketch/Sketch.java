package sketch;

import controller.SketchController;
import display.*;
import model.SketchDataModel;
import processing.core.PApplet;
import processing.core.PGraphics;
import processing.event.KeyEvent;
import processing.event.MouseEvent;

import java.util.ArrayList;

/**
 * Created by psweeney on 2/7/17.
 */
public abstract class Sketch extends PApplet {
    private SketchDataModel model;
    private PGraphics defaultGraphics;
    public final Color.ColorMode COLOR_MODE = new Color.ColorMode(255, 255, 255, 255);

    private KeyValueDisplayString frameRateDisplayString = new KeyValueDisplayString("FPS", "", new Color(255, 255), 16);
    private static int frameRateDecimalPlace = 1;
    private static void setFrameRateDecimalPlace(int newDecimalPlace){
        Sketch.frameRateDecimalPlace = newDecimalPlace;
    }

    private DisplayStringColumn diagInfoColumn = new DisplayStringColumn(new ArrayList<>());

    private boolean showDiagInfo = true;
    private int frameCount = 0;
    private int maxFrameCount = 60;

    public SketchDataModel getModel() {
        return model;
    }

    public void setModel(SketchDataModel model) {
        this.model = model;
    }

    @Override
    public final void settings(){
        super.settings();
        sketchSettings();
    }

    @Override
    public final void setup(){
        super.setup();
        sketchSetup();
        defaultGraphics = createGraphics(width, height, sketchRenderer());

        diagInfoColumn.addDisplayString(frameRateDisplayString);
    }

    @Override
    public final void colorMode(int mode) {
        super.colorMode(mode);
    }

    @Override
    public final void colorMode(int mode, float max1, float max2, float max3, float maxA) {
        super.colorMode(mode, max1, max2, max3, maxA);
        COLOR_MODE.setRMax(max1);
        COLOR_MODE.setGMax(max2);
        COLOR_MODE.setBMax(max3);
        COLOR_MODE.setAMax(maxA);
    }

    @Override
    public final void colorMode(int mode, float max1, float max2, float max3) {
        colorMode(mode, max1, max2, max3, PApplet.max(new float[] {max1, max2, max3}));
    }

    @Override
    public final void colorMode(int mode, float max) {
        colorMode(mode, max, max, max);
    }

    public PGraphics getDefaultGraphics() {
        return defaultGraphics;
    }

    public void addDisplayStringToDiagInfoColumn(DisplayString displayString){
        diagInfoColumn.addDisplayString(displayString);
    }

    private String getFrameRateDisplayString(){
        return DisplayString.getFloatStringForDecimalPlace(frameRate, frameRateDecimalPlace);
    }

    public int getFrameCount() {
        return frameCount;
    }

    public int getMaxFrameCount() {
        return maxFrameCount;
    }

    public void setMaxFrameCount(int maxFrameCount) {
        this.maxFrameCount = PApplet.max(1, maxFrameCount);
    }

    @Override
    public final void draw(){
        background(0);
        getModel().getController().setMouseState(
                new SketchController.MouseState(mousePressed, mouseButton, mouseX, mouseY, pmouseX, pmouseY));
        defaultGraphics.beginDraw();
        sketchDraw();
        defaultGraphics.endDraw();
        image(defaultGraphics, 0, 0, width, height);
        frameRateDisplayString.setValue(getFrameRateDisplayString());

        if(showDiagInfo){
            diagInfoColumn.draw(getGraphics(), COLOR_MODE, 10, 10, 5);
        }
        frameCount = (frameCount + 1) % maxFrameCount;
    }

    public final void setShowDiagInfo(boolean showDiagInfo){
        this.showDiagInfo = showDiagInfo;
    }

    public final void toggleShowDiagInfo(){
        showDiagInfo = !showDiagInfo;
    }

    public abstract void sketchSettings();
    public abstract void sketchSetup();
    public abstract void sketchDraw();

    @Override
    public void keyPressed(KeyEvent event) {
        super.keyPressed(event);
        model.getController().processKeyPressed(event.getKey(), event.getKeyCode());
    }

    @Override
    public void keyReleased(KeyEvent event) {
        super.keyReleased(event);
        model.getController().processKeyReleased(event.getKey(), event.getKeyCode());
    }

    @Override
    public void mouseWheel(MouseEvent event) {
        super.mouseWheel(event);
        model.getController().processMouseWheelEvent(event);
    }

    public static float randomFloat(){
        return (float) Math.random();
    }

    public static float randomFloat(float max){
        return randomFloat() * max;
    }

    public static float randomFloat(float min, float max){
        return min + randomFloat(max - min);
    }
}
