package sketches;

import datatypes.pixel.Color;
import datatypes.pixel.CustomPixel;
import controller.PhysicsController;
import datatypes.pixel.physics.PhysicsManager;
import datatypes.pixel.physics.PhysicsPixel;
import math.Vector;
import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PGraphics;
import processing.event.MouseEvent;

import java.util.*;

/**
 * Created by psweeney on 9/19/16.
 */


public class FallingSand extends PApplet{
    public static final float sqrt2 = sqrt(2);
    private PhysicsManager manager;
    private PhysicsController controller;

    private PGraphics particleGraphics;
    private PGraphics effectGraphics;
    private PGraphics staticEffectGraphics;
    private PGraphics swapGraphics;
    private PGraphics blurImage;

    private Map<Integer, PhysicsPixel> staticPostEffects = new HashMap<>();
    private float diagonalSize;

    private static final float PARTICLE_EFFECT_MIN_ALPHA = 1;
    private static final float PARTICLE_EFFECT_MAX_ALPHA = 255;
    private static final float PARTICLE_EFFECT_BLUR_AMT = 2f;
    private static final float PARTICLE_EFFECT_STROKE_WEIGHT = 5f;
    private static final float PARTICLE_EFFECT_CLEAR_ALPHA = 180;
    private static final float PARTICLE_EFFECT_FINAL_ALPHA = 255;
    private static final float UI_DEFAULT_TEXT_SIZE = 15;
    private static final float UI_PADDING_SIZE = 6;
    private static final float UI_FPS_DECIMAL_PRECISION = 1;
    private static final float UI_PAUSE_STATE_SIZE = UI_DEFAULT_TEXT_SIZE;

    private static final int EFFECTS_TILE_SIZE = 8;
    private static final int SWAP_GRAPHICS_TILE_DIVISOR = 1;

    @Override
    public void settings() {
        super.settings();
        size(600, 450);
        diagonalSize = PApplet.dist(0, 0, width, height);
        noSmooth();
    }

    @Override
    public void setup() {
        super.setup();
        background(0);
        manager = new PhysicsManager(this, width, height);
        controller = new PhysicsController(manager, PhysicsPixel.PhysicsPixelType.WALL, 7);
        particleGraphics = createGraphics(width, height);
        particleGraphics.beginDraw();
        particleGraphics.imageMode(PConstants.CENTER);
        particleGraphics.background(0);
        particleGraphics.endDraw();

        effectGraphics = createGraphics(CustomPixel.convertInt(width / EFFECTS_TILE_SIZE),
                CustomPixel.convertInt(height / EFFECTS_TILE_SIZE));
        effectGraphics.noSmooth();
        effectGraphics.beginDraw();
        effectGraphics.background(0, 0);
        effectGraphics.ellipseMode(PConstants.CENTER);
        effectGraphics.rectMode(PConstants.CORNER);
        effectGraphics.imageMode(PConstants.CENTER);
        effectGraphics.endDraw();

        staticEffectGraphics = createGraphics(CustomPixel.convertInt(width / EFFECTS_TILE_SIZE),
                CustomPixel.convertInt(height / EFFECTS_TILE_SIZE));
        staticEffectGraphics.noSmooth();
        staticEffectGraphics.beginDraw();
        staticEffectGraphics.background(0, 0);
        staticEffectGraphics.ellipseMode(PConstants.CENTER);
        staticEffectGraphics.rectMode(PConstants.CORNER);
        staticEffectGraphics.imageMode(CENTER);
        staticEffectGraphics.endDraw();

        swapGraphics = createGraphics(CustomPixel.convertInt(width / (EFFECTS_TILE_SIZE / SWAP_GRAPHICS_TILE_DIVISOR))
                        + EFFECTS_TILE_SIZE * 2, CustomPixel.convertInt(height / (
                        EFFECTS_TILE_SIZE / SWAP_GRAPHICS_TILE_DIVISOR)) + EFFECTS_TILE_SIZE * 2);
        swapGraphics.noSmooth();
        swapGraphics.beginDraw();
        swapGraphics.noStroke();
        swapGraphics.background(0, 0);
        swapGraphics.imageMode(PConstants.CENTER);
        swapGraphics.endDraw();

        blurImage = createGraphics((int) (PARTICLE_EFFECT_STROKE_WEIGHT + PApplet.sq(PARTICLE_EFFECT_BLUR_AMT)) + 1,
                (int) (PARTICLE_EFFECT_STROKE_WEIGHT + PApplet.sq(PARTICLE_EFFECT_BLUR_AMT)) + 1);
        blurImage.smooth();
        blurImage.beginDraw();
        blurImage.background(0, 0);
        blurImage.stroke(255, 255);
        blurImage.strokeWeight(PARTICLE_EFFECT_STROKE_WEIGHT);
        blurImage.point(blurImage.width/2, blurImage.height/2);
        blurImage.filter(PConstants.BLUR, PARTICLE_EFFECT_BLUR_AMT);
        blurImage.imageMode(PConstants.CENTER);
        blurImage.endDraw();

        blendMode(ADD);
        noFill();
        stroke(255);
        strokeWeight(1);
        ellipseMode(CENTER);
        rectMode(CENTER);
        imageMode(CENTER);
    }

    @Override
    public void keyPressed() {
        super.keyPressed();
        controller.processKeyPressed(key, keyCode);
    }

    @Override
    public void keyReleased() {
        controller.processKeyReleased(key, keyCode);
    }

    @Override
    public void keyTyped() {
        super.keyTyped();
        controller.processKeyTyped(key, keyCode);
    }

    @Override
    public void mouseWheel(MouseEvent event) {
        super.mouseWheel();
        controller.processMouseWheelEvent(event);
    }

    private void updateMouseState(){
        controller.setMouseState(new PhysicsController.MouseState(mousePressed, mouseButton, mouseX, mouseY, pmouseX,
                pmouseY));
        controller.processControllerState();
    }

    private void drawMouseState(){
        if(controller.getMouseState().isMousePressed()){
            if(controller.lineModeEnabled()){
                Vector mouseLineStart = controller.getMouseLineStart(), mouseLineEnd = controller.getMouseLineEnd();
                float startX = mouseLineStart.x, startY = mouseLineStart.y, endX = mouseLineEnd.x,
                        endY = mouseLineEnd.y;
                stroke(255);
                strokeWeight(controller.getMouseRadius() * 2);
                line(startX, startY, endX, endY);
            } else if(controller.tempGridModeEnabled()){
                Vector mouseGridStart = controller.getMouseGridStart(), mouseGridEnd = controller.getMouseGridEnd();
                if(mouseGridStart != null && mouseGridEnd != null){
                    stroke(255);
                    noFill();
                    rectMode(CORNER);
                    float minX = PApplet.min(mouseGridStart.x, mouseGridEnd.x), maxX = PApplet.max(mouseGridStart.x,
                            mouseGridEnd.x), minY = PApplet.min(mouseGridStart.y, mouseGridEnd.y),
                            maxY = PApplet.max(mouseGridStart.y, mouseGridEnd.y);
                    rect(minX, minY, maxX - minX, maxY - minY);
                }
            }
        }

        if(!controller.tempGridModeEnabled() && (!controller.lineModeEnabled() ||
                controller.getMouseLineStart() == null)){
            noFill();
            strokeWeight(1);
            stroke(255);
            float controllerMouseX = controller.getMouseState().getMouseX(),
                    controllerMouseY = controller.getMouseState().getMouseY(),
                    controllerMouseRadius = controller.getMouseRadius();

            ellipse(controllerMouseX, controllerMouseY, controllerMouseRadius * 2, controllerMouseRadius * 2);

            float diagonalOffset = controllerMouseRadius * sqrt2 / 2;
            switch (controller.getSkipIndexMode()){
                case 1:
                    line(controllerMouseX - diagonalOffset, controllerMouseY + diagonalOffset,
                            controllerMouseX + diagonalOffset, controllerMouseY - diagonalOffset);
                    break;
                case 2:
                    line(controllerMouseX - diagonalOffset, controllerMouseY - diagonalOffset,
                            controllerMouseX + diagonalOffset, controllerMouseY + diagonalOffset);
                    break;
                case 3:
                    line(controllerMouseX - diagonalOffset, controllerMouseY + diagonalOffset,
                            controllerMouseX + diagonalOffset, controllerMouseY - diagonalOffset);
                    line(controllerMouseX - diagonalOffset, controllerMouseY - diagonalOffset,
                            controllerMouseX + diagonalOffset, controllerMouseY + diagonalOffset);
                    break;
                default:
                    break;
            }
        }
    }

    private void drawPausedState(){
        float lx = width - (UI_PADDING_SIZE + UI_PAUSE_STATE_SIZE), ux = lx + UI_PAUSE_STATE_SIZE;
        float ly = UI_PADDING_SIZE, uy = UI_PADDING_SIZE + UI_PAUSE_STATE_SIZE;

        noStroke();
        fill(255);

        if(manager.isPaused()){
            rectMode(CORNER);
            rect(lx, ly, UI_PAUSE_STATE_SIZE/3, UI_PAUSE_STATE_SIZE);
            rect(lx + (2 * UI_PAUSE_STATE_SIZE)/3, ly, UI_PAUSE_STATE_SIZE/3, UI_PAUSE_STATE_SIZE);
        } else {
            beginShape();
            vertex(lx, ly);
            vertex(ux, ly + UI_PAUSE_STATE_SIZE/2);
            vertex(lx, uy);
            endShape(CLOSE);
        }
    }

    public void draw() {
        particleGraphics.beginDraw();
        particleGraphics.loadPixels();

        updateMouseState();
        //loadPixels();

        Map<Integer, Color> postEffectMap = new HashMap<>();

        Set<Integer> pixelUpdates = manager.update();

        boolean recomputeStaticEffects = false;

        for(Integer i : pixelUpdates){
            int x = i % width, y = i / width;
            int effectX = CustomPixel.convertInt((float) x / (float) EFFECTS_TILE_SIZE),
                    effectY = CustomPixel.convertInt((float) y / (float) EFFECTS_TILE_SIZE);
            int effectIndex = effectY * effectGraphics.width + effectX;
            PhysicsPixel p = manager.getPixel(x, y);
            if(p == null){
                particleGraphics.pixels[i] = color(0);
                if(staticPostEffects.containsKey(i)){
                    staticPostEffects.remove(i);
                    recomputeStaticEffects = true;
                }
            } else {
                particleGraphics.pixels[i] = convertColor(manager.getPixel(i).getColor());

                if(PhysicsPixel.isStaticPixelWithPostUpdate(p)){
                    staticPostEffects.put(i, p);
                    recomputeStaticEffects = true;
                    continue;
                }

                Color currentEffectColor = null;
                if(postEffectMap.containsKey(effectIndex)) currentEffectColor = postEffectMap.get(effectIndex);
                Color newEffectColor = p.getPostEffectColor(currentEffectColor, PARTICLE_EFFECT_MIN_ALPHA,
                        PARTICLE_EFFECT_MAX_ALPHA);
                if(newEffectColor != null && newEffectColor != currentEffectColor) postEffectMap.put(effectIndex,
                        newEffectColor);
            }
        }

        particleGraphics.updatePixels();
        particleGraphics.endDraw();
        image(particleGraphics, width/2, height/2, width, height);


        if(recomputeStaticEffects){
            Map<Integer, Color> staticPostEffectMap = new HashMap<>();

            for(Map.Entry<Integer, PhysicsPixel> entry : staticPostEffects.entrySet()){
                int i = entry.getKey();
                int x = i % width, y = i / width;
                int effectX = CustomPixel.convertInt((float) x / (float) EFFECTS_TILE_SIZE),
                        effectY = CustomPixel.convertInt((float) y / (float) EFFECTS_TILE_SIZE);
                int effectIndex = effectY * effectGraphics.width + effectX;

                Color currentColor = null;
                if(staticPostEffectMap.containsKey(effectIndex)) currentColor = staticPostEffectMap.get(effectIndex);
                Color newColor = entry.getValue().getPostEffectColor(currentColor, PARTICLE_EFFECT_MIN_ALPHA,
                        PARTICLE_EFFECT_MAX_ALPHA);
                if(newColor != null && newColor != currentColor){
                    staticPostEffectMap.put(effectIndex, newColor);
                }
            }

            staticEffectGraphics.beginDraw();
            staticEffectGraphics.background(0, 0);

            /*
            staticEffectGraphics.fill(0, PARTICLE_EFFECT_CLEAR_ALPHA);
            staticEffectGraphics.rect(-PARTICLE_EFFECT_BLUR_AMT, -PARTICLE_EFFECT_BLUR_AMT, staticEffectGraphics.width
                    + 2 * PARTICLE_EFFECT_BLUR_AMT, staticEffectGraphics.height + 2 * PARTICLE_EFFECT_BLUR_AMT);
            */

            staticEffectGraphics.strokeWeight(PARTICLE_EFFECT_STROKE_WEIGHT);

            for (Map.Entry<Integer, Color> entry : staticPostEffectMap.entrySet()) {
                int key = entry.getKey();

                int effectX = key % staticEffectGraphics.width, effectY = key / staticEffectGraphics.width;

                Color value = entry.getValue();
                float r = PApplet.max(0, value.getR());
                float g = PApplet.max(0, value.getG());
                float b = PApplet.max(0, value.getB());
                float a = PApplet.max(0, value.getA());

                staticEffectGraphics.tint(r, g, b, a);
                staticEffectGraphics.blendMode(PConstants.ADD);
                staticEffectGraphics.image(blurImage, effectX, effectY, blurImage.width *
                                (PARTICLE_EFFECT_STROKE_WEIGHT/EFFECTS_TILE_SIZE), blurImage.height *
                                (PARTICLE_EFFECT_STROKE_WEIGHT/EFFECTS_TILE_SIZE));
                staticEffectGraphics.noTint();
                /*
                staticEffectGraphics.stroke(r, g, b, a);
                //staticEffectGraphics.point(effectX - 1/(float) EFFECTS_TILE_SIZE, effectY - 1/(float) EFFECTS_TILE_SIZE);
                staticEffectGraphics.point(effectX, effectY);
                */
            }

            //staticEffectGraphics.filter(PConstants.BLUR, PARTICLE_EFFECT_BLUR_AMT);
            //if(!useSwapGraphics) staticEffectGraphics.filter(PConstants.BLUR, PARTICLE_EFFECT_BLUR_AMT);

            staticEffectGraphics.endDraw();
            //tint(255, 255);
            recomputeStaticEffects = false;
        }

        if(controller.effectsEnabled()) {
            effectGraphics.beginDraw();

            if(controller.swapGraphicsEnabled()) {
                swapGraphics.beginDraw();
                //swapGraphics.background(0, 0);
                swapGraphics.fill(0, PARTICLE_EFFECT_CLEAR_ALPHA);
                swapGraphics.rect(0, 0, swapGraphics.width, swapGraphics.height);
                swapGraphics.blendMode(PConstants.ADD);

                swapGraphics.image(staticEffectGraphics, swapGraphics.width/2, swapGraphics.height/2,
                        swapGraphics.width - EFFECTS_TILE_SIZE * 2, swapGraphics.height - EFFECTS_TILE_SIZE * 2);
            } else {
                effectGraphics.blendMode(ADD);
                effectGraphics.image(staticEffectGraphics, effectGraphics.width/2, effectGraphics.height/2,
                        staticEffectGraphics.width, staticEffectGraphics.height);
            }


            /*
            effectGraphics.fill(0, PARTICLE_EFFECT_CLEAR_ALPHA);
            effectGraphics.blendMode(PConstants.NORMAL);
            effectGraphics.rectMode(CORNER);
            effectGraphics.rect(-PARTICLE_EFFECT_BLUR_AMT, -PARTICLE_EFFECT_BLUR_AMT, effectGraphics.width + 2 *
                    PARTICLE_EFFECT_BLUR_AMT, effectGraphics.height + 2 * PARTICLE_EFFECT_BLUR_AMT);
            */

            effectGraphics.background(0, 0);
            effectGraphics.strokeWeight(PARTICLE_EFFECT_STROKE_WEIGHT);
            for (Map.Entry<Integer, Color> entry : postEffectMap.entrySet()) {
                int key = entry.getKey();

                int effectX = key % effectGraphics.width, effectY = key / effectGraphics.width;

                Color value = entry.getValue();
                float r = PApplet.max(0, value.getR());
                float g = PApplet.max(0, value.getG());
                float b = PApplet.max(0, value.getB());
                float a = PApplet.max(0, value.getA());

                effectGraphics.tint(r, g, b, a);
                effectGraphics.blendMode(ADD);
                effectGraphics.image(blurImage, effectX, effectY, blurImage.width *
                        (PARTICLE_EFFECT_STROKE_WEIGHT/EFFECTS_TILE_SIZE), blurImage.height *
                        (PARTICLE_EFFECT_STROKE_WEIGHT/EFFECTS_TILE_SIZE));
                effectGraphics.noTint();

                //effectGraphics.stroke(r, g, b, a * 2);
                //effectGraphics.point(effectX, effectY);
            }

            //effectGraphics.filter(PConstants.BLUR, PARTICLE_EFFECT_BLUR_AMT);
            //if(!useSwapGraphics) effectGraphics.filter(PConstants.BLUR, PARTICLE_EFFECT_BLUR_AMT);

            effectGraphics.endDraw();
            //tint(255, 255);

            blendMode(ADD);
            tint(255, 255, 255, PARTICLE_EFFECT_FINAL_ALPHA);
            if(controller.swapGraphicsEnabled()) {

                swapGraphics.image(effectGraphics, swapGraphics.width/2, swapGraphics.height/2, swapGraphics.width -
                        EFFECTS_TILE_SIZE * 2, swapGraphics.height - EFFECTS_TILE_SIZE * 2);
                swapGraphics.filter(PConstants.BLUR, PARTICLE_EFFECT_BLUR_AMT);
                swapGraphics.endDraw();

                image(swapGraphics,
                        width/2 + (PARTICLE_EFFECT_BLUR_AMT + EFFECTS_TILE_SIZE) * (width/diagonalSize) - 1,
                        height/2 + (PARTICLE_EFFECT_BLUR_AMT + EFFECTS_TILE_SIZE) * (height/diagonalSize) * (1/EFFECTS_TILE_SIZE),
                        width + (EFFECTS_TILE_SIZE) * (SWAP_GRAPHICS_TILE_DIVISOR) * 16,
                        height + (EFFECTS_TILE_SIZE) * (SWAP_GRAPHICS_TILE_DIVISOR) * 16);

            } else {
                image(effectGraphics, width/2, height/2, width, height);
            }
            noTint();
        }

        blendMode(ADD);
        PGraphics tempGridImage = manager.getTempRegionImage();
        image(tempGridImage, width/2, height/2, width, height);

        if(controller.diagnosticsEnabled()) {
            int i = 1;
            textSize(UI_DEFAULT_TEXT_SIZE);
            for(Map.Entry<String, Color> diagStringEntry : getDiagnosticStrings(pixelUpdates.size())){
                fill(convertColor(diagStringEntry.getValue()));
                text(diagStringEntry.getKey(), UI_PADDING_SIZE, i * UI_PADDING_SIZE + i * UI_DEFAULT_TEXT_SIZE);
                i++;
            }
            drawPausedState();
        }

        drawMouseState();
    }

    private int convertColor(Color c){
        if(c == null){
            return color(0, 0);
        }

        float alphaRatio = c.getA()/255;
        return color(c.getR() * alphaRatio, c.getG() * alphaRatio, c.getB() * alphaRatio);
    }

    public ArrayList<Map.Entry<String, Color>> getDiagnosticStrings(int pixelUpdateCount){
        ArrayList<Map.Entry<String, Color>> strings = new ArrayList<>();

        PhysicsPixel.PhysicsPixelType mouseType = controller.getMouseType();
        String mouseTypeLabel = PhysicsPixel.getTextForPixelType(mouseType);
        if(mouseType == PhysicsPixel.PhysicsPixelType.COOL_GRID || mouseType ==
                PhysicsPixel.PhysicsPixelType.HEAT_GRID)
            mouseTypeLabel += " (" + PApplet.abs(controller.getTempGridMultiplier()) + ")";

        Color mouseTypeColor = PhysicsPixel.getColorForPixelType(manager, controller.getMouseType());

        String totalPixelsLabel = "Total pixels: " + manager.getPixelCount();
        Color totalPixelsColor = new Color(255);

        String pixelUpdateLabel = "Pixel update count: " + pixelUpdateCount;
        Color pixelUpdateColor = new Color(255);

        String framerateLabel = "FPS: " + (float) ((int) ((float) frameRate * pow(10, UI_FPS_DECIMAL_PRECISION))) /
                pow(10, UI_FPS_DECIMAL_PRECISION);
        Color framerateColor = new Color(255);

        String gravityEnabledLabel = "Gravity: ";
        if(manager.isGravityEnabled()) gravityEnabledLabel += "ENABLED"; else gravityEnabledLabel += "DISABLED";
        Color gravityEnabledColor = new Color(255);

        strings.add(new AbstractMap.SimpleEntry<String, Color>(mouseTypeLabel, mouseTypeColor));
        strings.add(new AbstractMap.SimpleEntry<String, Color>(totalPixelsLabel, totalPixelsColor));
        strings.add(new AbstractMap.SimpleEntry<String, Color>(pixelUpdateLabel, pixelUpdateColor));
        strings.add(new AbstractMap.SimpleEntry<String, Color>(framerateLabel, framerateColor));
        strings.add(new AbstractMap.SimpleEntry<String, Color>(gravityEnabledLabel, gravityEnabledColor));

        return strings;
    }

    public static void main(String args[]){
        PApplet.main("sketches.FallingSand");
    }
}
