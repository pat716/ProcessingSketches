package controller;

import datatypes.pixel.Color;
import math.Vector;
import processing.event.MouseEvent;

/**
 * Created by psweeney on 10/23/16.
 */
public class ShadowTestController extends SketchController {
    private Vector mouseVector;
    private Color mouseLightColor;
    private float mouseLightBrightness;

    public ShadowTestController(Color mouseLightColor, float mouseLightBrightness, float initialMouseHeight){
        super();
        this.mouseLightColor = mouseLightColor;
        this.mouseLightBrightness = mouseLightBrightness;
        mouseVector = new Vector(getMouseState().getMouseX(), getMouseState().getMouseY(), initialMouseHeight);
    }

    public Color getMouseLightColor() {
        return mouseLightColor;
    }

    public float getMouseLightBrightness() {
        return mouseLightBrightness;
    }

    public Vector getMouseVector() {
        return mouseVector;
    }

    @Override
    public void processKeyPressed(char key, int keyCode) {

    }

    @Override
    public void processKeyReleased(char key, int keyCode) {

    }

    @Override
    public void processKeyTyped(char key, int keyCode) {

    }

    @Override
    public void processMouseWheelEvent(MouseEvent event) {
        mouseVector.z += event.getCount();
        if(mouseVector.z < 1) mouseVector.z = 1;
    }

    @Override
    public void processControllerState() {
        mouseVector.x = getMouseState().getMouseX();
        mouseVector.y = getMouseState().getMouseY();
    }
}
