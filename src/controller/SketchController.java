package controller;

import display.Color;
import processing.core.PConstants;
import processing.event.MouseEvent;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by psweeney on 10/19/16.
 */
public class SketchController {
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

    private HashMap<Character, Boolean> keyMap, newKeyMap;
    private HashMap<Integer, Boolean> keyCodeMap, newKeyCodeMap;
    private MouseEvent lastMouseWheelEvent;

    private MouseState mouseState;

    public SketchController(){
        mouseState = new MouseState();
        keyMap = new HashMap<>();
        newKeyMap = new HashMap<>();
        keyCodeMap = new HashMap<>();
        newKeyCodeMap = new HashMap<>();
        lastMouseWheelEvent = null;
    }

    public MouseState getMouseState() {
        return mouseState;
    }

    public HashMap<Character, Boolean> getNewKeys(){
        HashMap<Character, Boolean> copy = new HashMap<>(newKeyMap);
        newKeyMap.clear();
        return copy;
    }

    public HashMap<Integer, Boolean> getNewKeyCodes() {
        HashMap<Integer, Boolean> copy = new HashMap<>(newKeyCodeMap);
        newKeyCodeMap.clear();
        return copy;
    }

    public boolean isKeyPressed(char key){
        return keyMap.containsKey(key) && keyMap.get(key);
    }

    public boolean isKeyCodePressed(int keyCode){
        return keyCodeMap.containsKey(keyCode) && keyCodeMap.get(keyCode);
    }

    public void setMouseState(MouseState mouseState) {
        this.mouseState = mouseState;
    }

    public void processKeyPressed(char key, int keyCode){
        if(key == PConstants.CODED){
            keyCodeMap.put(keyCode, true);
            newKeyCodeMap.put(keyCode, true);
        } else {
            keyMap.put(key, true);
            newKeyMap.put(key, true);
        }
    }

    public void processKeyReleased(char key, int keyCode){
        if(key == PConstants.CODED){
            keyCodeMap.put(keyCode, false);
            newKeyCodeMap.put(keyCode, false);
        } else {
            keyMap.put(key, false);
            newKeyMap.put(key, false);
        }
    }

    public void processMouseWheelEvent(MouseEvent event){
        lastMouseWheelEvent = event;
    }

    public MouseEvent popLastMouseWheelEvent(){
        MouseEvent temp = lastMouseWheelEvent;
        lastMouseWheelEvent = null;
        return temp;
    }
}
