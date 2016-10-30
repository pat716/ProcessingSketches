package controller;

import datatypes.water.WaterManager;
import processing.core.PConstants;
import processing.event.MouseEvent;

/**
 * Created by psweeney on 10/19/16.
 */
public class WaterController extends SketchController{
    private static final float DEFAULT_MOUSE_STRENGTH_INCREASE_AMOUNT = 0.1f;

    private WaterManager manager;
    private float mouseRadius;
    private float mouseStrength;
    private float mouseStrengthIncreaseAmount = 0;
    private boolean diagOverlayEnabled = true;

    public WaterController(WaterManager manager, float mouseRadius, float mouseStrength){
        super();
        this.manager = manager;
        this.mouseRadius = mouseRadius;
        this.mouseStrength = mouseStrength;
    }

    public float getMouseRadius() {
        return mouseRadius;
    }

    public float getMouseStrength() {
        return mouseStrength;
    }

    public boolean isDiagOverlayEnabled() {
        return diagOverlayEnabled;
    }

    @Override
    public void processKeyPressed(char key, int keyCode) {
        switch (key){
            case 'w':
                mouseStrengthIncreaseAmount = DEFAULT_MOUSE_STRENGTH_INCREASE_AMOUNT;
                break;
            case 's':
                mouseStrengthIncreaseAmount = -DEFAULT_MOUSE_STRENGTH_INCREASE_AMOUNT;
                break;
        }
    }

    @Override
    public void processKeyReleased(char key, int keyCode) {
        switch (key){
            case 'w':
                mouseStrengthIncreaseAmount = 0;
                break;
            case 's':
                mouseStrengthIncreaseAmount = 0;
                break;
        }
    }

    @Override
    public void processKeyTyped(char key, int keyCode) {
        switch (key){
            case 'i':
                diagOverlayEnabled = !diagOverlayEnabled;
                break;
        }
    }

    @Override
    public void processMouseWheelEvent(MouseEvent event) {
        mouseRadius += event.getCount();
        if(mouseRadius < 1) mouseRadius = 1;
    }

    @Override
    public void processControllerState() {
        mouseStrength += mouseStrengthIncreaseAmount;
        if(mouseStrength < 1) mouseStrength = 1;

        MouseState mouseState = getMouseState();
        boolean mousePressed = mouseState.isMousePressed();
        int mouseButton = mouseState.getMouseButton();
        float mouseX = mouseState.getMouseX(), mouseY = mouseState.getMouseY();

        if(mousePressed){
            if(mouseButton == PConstants.RIGHT)
                manager.push(mouseX, mouseY, mouseRadius, mouseStrength);
            else if(mouseButton == PConstants.LEFT)
                manager.pull(mouseX, mouseY, mouseRadius, mouseStrength);
        }
    }
}
