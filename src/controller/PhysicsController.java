package controller;

import datatypes.pixel.physics.PhysicsManager;
import datatypes.pixel.physics.PhysicsPixel;
import datatypes.pixel.physics.PhysicsPixelFactory;
import datatypes.pixel.physics.TemperatureObject;
import math.Vector;
import processing.core.PApplet;
import processing.event.MouseEvent;

import java.util.Arrays;
import java.util.HashSet;

import static processing.core.PConstants.*;

/**
 * Created by psweeney on 10/12/16.
 */
public class PhysicsController extends SketchController{

    private PhysicsManager manager;
    private Vector mouseLineStart = null;
    private Vector mouseLineEnd = null;
    private Vector mouseGridStart = null;
    private Vector mouseGridEnd = null;

    private boolean useEffects = true;
    private boolean useSwapGraphics = true;
    private boolean showDiagnostics = true;
    private boolean lineMode = false;
    private boolean snapMode = false;
    private boolean tempGridMode = false;

    private int skipIndexMode = 0;
    private static final int NUM_SKIP_INDEX_MODES = 5;

    private MouseState mouseState;
    private PhysicsPixel.PhysicsPixelType mouseType;
    private float mouseRadius;
    private int tempGridMultiplier = 0;


    public PhysicsController(PhysicsManager manager, PhysicsPixel.PhysicsPixelType mouseType, float mouseRadius){
        super();
        this.manager = manager;
        this.mouseType = mouseType;
        this.mouseRadius = mouseRadius;
    }

    private void setMouseType(PhysicsPixel.PhysicsPixelType type){
        mouseType = type;
        if(mouseType == PhysicsPixel.PhysicsPixelType.HEAT_GRID || mouseType ==
                PhysicsPixel.PhysicsPixelType.COOL_GRID || mouseType == PhysicsPixel.PhysicsPixelType.TEMP_GRID_ERASE){
            tempGridMode = true;
        } else {
            tempGridMultiplier = 0;
            tempGridMode = false;
            mouseGridStart = null;
            mouseGridEnd = null;
        }
    }

    private void setTempGridMultiplier(int tempGridMultiplier) {
        this.tempGridMultiplier = tempGridMultiplier;
        if((float) this.tempGridMultiplier * TemperatureObject.TempGridObject.DEFAULT_TEMP_GRID_AMOUNT >
                TemperatureObject.TempGridObject.MAX_GRID_TEMP)
            this.tempGridMultiplier = (int) (TemperatureObject.TempGridObject.MAX_GRID_TEMP /
                    TemperatureObject.TempGridObject.DEFAULT_TEMP_GRID_AMOUNT);
        else if((float) this.tempGridMultiplier * TemperatureObject.TempGridObject.DEFAULT_TEMP_GRID_AMOUNT <
                TemperatureObject.TempGridObject.MIN_GRID_TEMP)
            this.tempGridMultiplier = (int) (TemperatureObject.TempGridObject.MIN_GRID_TEMP /
                    TemperatureObject.TempGridObject.DEFAULT_TEMP_GRID_AMOUNT);
        if(tempGridMultiplier > 0) setMouseType(PhysicsPixel.PhysicsPixelType.HEAT_GRID);
        else if(tempGridMultiplier < 0) setMouseType(PhysicsPixel.PhysicsPixelType.COOL_GRID);
        else setMouseType(PhysicsPixel.PhysicsPixelType.TEMP_GRID_ERASE);
    }

    public boolean lineModeEnabled() {
        return lineMode;
    }

    public boolean tempGridModeEnabled() {
        return tempGridMode;
    }

    public int getTempGridMultiplier() {
        return tempGridMultiplier;
    }

    public boolean effectsEnabled(){
        return useEffects;
    }

    public boolean swapGraphicsEnabled(){
        return useSwapGraphics;
    }

    public boolean diagnosticsEnabled() {
        return showDiagnostics;
    }

    public int getSkipIndexMode() {
        return skipIndexMode;
    }

    public Vector getMouseLineStart() {
        return mouseLineStart;
    }

    public Vector getMouseLineEnd() {
        return mouseLineEnd;
    }

    public Vector getMouseGridStart() {
        return mouseGridStart;
    }

    public Vector getMouseGridEnd() {
        return mouseGridEnd;
    }

    public float getMouseRadius() {
        return mouseRadius;
    }

    public PhysicsPixel.PhysicsPixelType getMouseType() {
        return mouseType;
    }

    @Override
    public void processKeyPressed(char key, int keyCode){
        switch (key){
            case CODED:
                switch (keyCode){
                    case SHIFT:
                        lineMode = true;
                        break;
                }
                break;
            case ' ':
                snapMode = true;
                break;
        }
    }

    @Override
    public void processKeyReleased(char key, int keyCode){
        switch (key){
            case CODED:
                switch (keyCode){
                    case SHIFT:
                        lineMode = false;
                        mouseLineStart = null;
                        mouseLineEnd = null;
                        break;
                    case CONTROL:
                        manager.clear();
                        break;
                }
                break;
            case ' ':
                snapMode = false;
                break;
        }
    }

    @Override
    public void processKeyTyped(char key, int keyCode){
        switch (key){
            case 'o':
                manager.clearClone();
                break;
            case 'p':
                manager.togglePaused();
                break;
            case 'w':
                switch (mouseType){
                    case WATER:
                        setMouseType(PhysicsPixel.PhysicsPixelType.STEAM);
                        break;
                    case STEAM:
                        setMouseType(PhysicsPixel.PhysicsPixelType.ICE);
                        break;
                    case ICE:
                        setMouseType(PhysicsPixel.PhysicsPixelType.WATER);
                        break;
                    default: setMouseType(PhysicsPixel.PhysicsPixelType.WATER);
                        break;
                }
                break;
            case '0':
                switch (mouseType){
                    case EMPTY:
                        setTempGridMultiplier(0);
                        break;
                    case TEMP_GRID_ERASE:
                        setMouseType(PhysicsPixel.PhysicsPixelType.EMPTY);
                        break;
                    default:
                        setMouseType(PhysicsPixel.PhysicsPixelType.EMPTY);
                        break;
                }
                break;
            case '1':
                setMouseType(PhysicsPixel.PhysicsPixelType.WALL);
                break;
            case '2':
            case 'f':
                setMouseType(PhysicsPixel.PhysicsPixelType.FIRE);
                break;
            case '3':
            case 'g':
                setMouseType(PhysicsPixel.PhysicsPixelType.GRASS);
                break;
            case 'i':
                showDiagnostics = !showDiagnostics;
                break;
            case '4':
            case 'l':
                setMouseType(PhysicsPixel.PhysicsPixelType.FUSE);
                break;
            case '5':
            case 'b':
                setMouseType(PhysicsPixel.PhysicsPixelType.GUNPOWDER);
                break;
            case '6':
            case 'c':
                switch (mouseType){
                    case CO2_SOLID:
                        setMouseType(PhysicsPixel.PhysicsPixelType.CO2_GAS);
                        break;
                    case CO2_GAS:
                        setMouseType(PhysicsPixel.PhysicsPixelType.CO2_SOLID);
                        break;
                    default:
                        setMouseType(PhysicsPixel.PhysicsPixelType.CO2_SOLID);
                        break;
                }
                break;
            case '7':
            case 'm':
                switch (mouseType){
                    case METAL:
                        setMouseType(PhysicsPixel.PhysicsPixelType.LAVA);
                        break;
                    case LAVA:
                        setMouseType(PhysicsPixel.PhysicsPixelType.METAL);
                        break;
                    default:
                        setMouseType(PhysicsPixel.PhysicsPixelType.METAL);
                        break;
                }
                break;
            case '8':
            case 'a':
                setMouseType(PhysicsPixel.PhysicsPixelType.ASH);
                break;
            case 'u':
                setMouseType(PhysicsPixel.PhysicsPixelType.URANIUM);
                break;
            case 'e':
                useEffects = !useEffects;
                break;
            case 'q':
                useSwapGraphics = !useSwapGraphics;
                break;
            case '9':
                switch (mouseType){
                    case CLONE:
                        setMouseType(PhysicsPixel.PhysicsPixelType.CLONE_DEACTIVATE);
                        break;
                    case CLONE_DEACTIVATE:
                        setMouseType(PhysicsPixel.PhysicsPixelType.CLONE_ACTIVATE);
                        break;
                    default:
                        setMouseType(PhysicsPixel.PhysicsPixelType.CLONE);
                        break;
                }
                break;
            case 't':
                switch (mouseType){
                    case HEAT:
                        setMouseType(PhysicsPixel.PhysicsPixelType.COOL);
                        break;
                    case COOL:
                        setMouseType(PhysicsPixel.PhysicsPixelType.HEAT);
                        break;
                    default:
                        setMouseType(PhysicsPixel.PhysicsPixelType.HEAT);
                        break;
                }
                break;
            case 'x':
                manager.toggleGravity();
                break;
            case 'y':
                skipIndexMode = (skipIndexMode + 1) % NUM_SKIP_INDEX_MODES;
                break;
            case '-':
            case '_':
                setTempGridMultiplier(tempGridMultiplier - 1);
                break;
            case '=':
            case '+':
                setTempGridMultiplier(tempGridMultiplier + 1);
                break;
        }
    }

    @Override
    public void processMouseWheelEvent(MouseEvent event){
        if(tempGridMode) return;
        mouseRadius += event.getCount();
        if(mouseRadius < 1) mouseRadius = 1;
    }

    @Override
    public void processControllerState(){
        MouseState mouseState = getMouseState();
        boolean mousePressed = mouseState.isMousePressed();
        float mouseX = mouseState.getMouseX(), mouseY = mouseState.getMouseY();
        if(mouseState.isMousePressed()){
            if(lineMode){
                if(mouseLineStart == null){
                    mouseLineStart = new Vector(mouseX, mouseY);
                    mouseLineEnd = new Vector(mouseX, mouseY);
                } else {
                    float startX = mouseLineStart.x, startY = mouseLineStart.y, endX = mouseX,
                            endY = mouseY;
                    if (snapMode) {
                        float effectiveOffset = PApplet.dist(startX, startY, endX, endY);
                        Vector mouseVector = new Vector(mouseX, mouseY);
                        Vector endVector = null;
                        float sqrt2 = PApplet.sqrt(2);
                        for (Vector v : new HashSet<>(Arrays.asList(new Vector[]{
                                Vector.getVectorRelativeToOtherVector(mouseLineStart, 0, effectiveOffset),
                                Vector.getVectorRelativeToOtherVector(mouseLineStart, PI/4f, effectiveOffset),
                                Vector.getVectorRelativeToOtherVector(mouseLineStart, PI/2, effectiveOffset),
                                Vector.getVectorRelativeToOtherVector(mouseLineStart, 3 * PI / 4, effectiveOffset),
                                Vector.getVectorRelativeToOtherVector(mouseLineStart, PI, effectiveOffset),
                                Vector.getVectorRelativeToOtherVector(mouseLineStart, 5 * PI / 4, effectiveOffset),
                                Vector.getVectorRelativeToOtherVector(mouseLineStart, 3 * PI / 2, effectiveOffset),
                                Vector.getVectorRelativeToOtherVector(mouseLineStart, 7 * PI / 4, effectiveOffset)
                        }))) {
                            if (endVector == null || mouseVector.dist(v) < mouseVector.dist(endVector))
                                endVector = v;
                        }

                        if (endVector != null) {
                            endX = endVector.x;
                            endY = endVector.y;
                        }
                    }

                    mouseLineEnd = new Vector(endX, endY);
                }
            } else if(tempGridMode){
                if(mouseGridStart == null) mouseGridStart = new Vector(mouseX, mouseY);
                mouseGridEnd = new Vector(mouseX, mouseY);
            } else {
                PhysicsPixelFactory.addPhysicsPixelsInRadius(manager, mouseX, mouseY,
                        mouseRadius, mouseType, skipIndexMode);
            }
        } else {
            if(lineMode && mouseLineStart != null && mouseLineEnd != null){
                PhysicsPixelFactory.addPhysicsPixelsAlongLine(manager, mouseLineStart.x, mouseLineStart.y,
                        mouseLineEnd.x, mouseLineEnd.y, mouseRadius, mouseType, skipIndexMode);
            } else if(tempGridMode && mouseGridStart != null && mouseGridEnd != null){
                float minX = PApplet.min(mouseGridStart.x, mouseGridEnd.x), maxX = PApplet.max(mouseGridStart.x,
                        mouseGridEnd.x), minY = PApplet.min(mouseGridStart.y, mouseGridEnd.y),
                        maxY = PApplet.max(mouseGridStart.y, mouseGridEnd.y);
                if(mouseType == PhysicsPixel.PhysicsPixelType.TEMP_GRID_ERASE)
                    manager.clearTempGridInRegion(minX, minY, maxX - minX, maxY - minY);
                else manager.applyTempGridToRegion(minX, minY, maxX - minX, maxY - minY,
                        (float) tempGridMultiplier * TemperatureObject.TempGridObject.DEFAULT_TEMP_GRID_AMOUNT);

            }
            mouseLineStart = null;
            mouseLineEnd = null;
            mouseGridStart = null;
            mouseGridEnd = null;
        }
    }
}
