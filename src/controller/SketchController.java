package controller;

import processing.core.PConstants;
import processing.event.MouseEvent;

/**
 * Created by psweeney on 10/19/16.
 */
public abstract class SketchController {
    public static class MouseState{
        private boolean mousePressed = false;
        private int mouseButton = PConstants.RIGHT;
        private float mouseX = 0;
        private float mouseY = 0;
        private float pmouseX = 0;
        private float pmouseY = 0;

        public MouseState(boolean mousePressed, int mouseButton, float mouseX, float mouseY, float pmouseX,
                          float pmouseY){
            this.mousePressed = mousePressed;
            this.mouseButton = mouseButton;
            this.mouseX = mouseX;
            this.mouseY = mouseY;
            this.pmouseX = pmouseX;
            this.pmouseY = pmouseY;
        }

        public MouseState(){
            this(false, PConstants.RIGHT, 0, 0, 0, 0);
        }

        public boolean isMousePressed() {
            return mousePressed;
        }

        public int getMouseButton() {
            return mouseButton;
        }

        public float getMouseX() {
            return mouseX;
        }

        public float getMouseY() {
            return mouseY;
        }

        public float getPmouseX() {
            return pmouseX;
        }

        public float getPmouseY() {
            return pmouseY;
        }
    }
    private MouseState mouseState;

    public SketchController(){
        mouseState = new MouseState();
    }

    public MouseState getMouseState() {
        return mouseState;
    }

    public void setMouseState(MouseState mouseState) {
        this.mouseState = mouseState;
    }

    public abstract void processKeyPressed(char key, int keyCode);

    public abstract void processKeyReleased(char key, int keyCode);

    public abstract void processKeyTyped(char key, int keyCode);

    public abstract void processMouseWheelEvent(MouseEvent event);

    public abstract void processControllerState();
}
