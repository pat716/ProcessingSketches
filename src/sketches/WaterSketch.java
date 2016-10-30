package sketches;

import controller.SketchController;
import controller.WaterController;
import datatypes.pixel.Color;
import datatypes.water.WaterManager;
import datatypes.water.WaterPolygon;
import datatypes.water.WaterVector;
import math.Vector;
import processing.core.PApplet;
import processing.core.PGraphics;
import processing.event.MouseEvent;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;

/**
 * Created by psweeney on 10/19/16.
 */
public class WaterSketch extends PApplet{
    public static final float SQRT_2 = PApplet.sqrt(2);
    private static final float POLYGON_EDGE_LENGTH = 20;
    private static final int POLYGON_IMAGE_PADDING = 1;
    private static final float POLYGON_IMAGE_SIZE_MULT = 1.08f;
    private static final float BLOOM_TILE_SIZE = 8;
    private static final float UI_DEFAULT_TEXT_SIZE = 15;
    private static final float UI_PADDING_SIZE = 6;
    private static final float UI_FPS_DECIMAL_PRECISION = 1;

    private WaterManager manager;
    private WaterController controller;
    private PGraphics polygonImage, upsideDownPolygonImage;
    @Override
    public void settings() {
        super.settings();
        size(600, 450);
    }

    @Override
    public void setup() {
        super.setup();
        background(0);

        manager = new WaterManager(width, height, POLYGON_EDGE_LENGTH);
        controller = new WaterController(manager, 20, 20);

        polygonImage = createGraphics(ceil(POLYGON_EDGE_LENGTH * POLYGON_IMAGE_SIZE_MULT) + 2 * POLYGON_IMAGE_PADDING,
                ceil(POLYGON_EDGE_LENGTH * POLYGON_IMAGE_SIZE_MULT) + 2 * POLYGON_IMAGE_PADDING);


        float polygonHeight = sqrt(3) * ((POLYGON_EDGE_LENGTH * POLYGON_IMAGE_SIZE_MULT)/2);
        float polygonCenterWidth = polygonImage.width/2 + POLYGON_IMAGE_PADDING;
        float polygonCenterHeight = 2 * (polygonHeight/3) + POLYGON_IMAGE_PADDING;
        polygonImage.smooth(8);
        polygonImage.beginDraw();
        polygonImage.background(0, 0);
        polygonImage.noStroke();
        polygonImage.fill(255);
        polygonImage.beginShape();


        polygonImage.vertex(polygonCenterWidth - (POLYGON_EDGE_LENGTH * POLYGON_IMAGE_SIZE_MULT)/2,
                polygonCenterHeight - polygonHeight/2);
        polygonImage.vertex(polygonCenterWidth + (POLYGON_EDGE_LENGTH * POLYGON_IMAGE_SIZE_MULT)/2,
                polygonCenterHeight - polygonHeight/2);
        polygonImage.vertex(polygonCenterWidth, polygonCenterHeight + polygonHeight/2);
        polygonImage.endShape();
        polygonImage.endDraw();

        upsideDownPolygonImage = createGraphics(
                ceil(POLYGON_EDGE_LENGTH * POLYGON_IMAGE_SIZE_MULT) + 2 * POLYGON_IMAGE_PADDING,
                ceil(POLYGON_EDGE_LENGTH * POLYGON_IMAGE_SIZE_MULT) + 2 * POLYGON_IMAGE_PADDING);
        upsideDownPolygonImage.smooth(8);

        polygonCenterHeight = (polygonHeight/3) + POLYGON_IMAGE_PADDING;
        upsideDownPolygonImage.beginDraw();
        upsideDownPolygonImage.background(0, 0);
        upsideDownPolygonImage.noStroke();
        upsideDownPolygonImage.fill(255);
        upsideDownPolygonImage.beginShape();
        upsideDownPolygonImage.vertex(polygonCenterWidth - (POLYGON_EDGE_LENGTH * POLYGON_IMAGE_SIZE_MULT)/2,
                polygonCenterHeight + polygonHeight/2);
        upsideDownPolygonImage.vertex(polygonCenterWidth + (POLYGON_EDGE_LENGTH * POLYGON_IMAGE_SIZE_MULT)/2,
                polygonCenterHeight + polygonHeight/2);
        upsideDownPolygonImage.vertex(polygonCenterWidth, polygonCenterHeight - polygonHeight/2);
        upsideDownPolygonImage.endShape();
        upsideDownPolygonImage.endDraw();
    }

    @Override
    public void keyPressed() {
        super.keyPressed();
        controller.processKeyPressed(key, keyCode);
    }

    @Override
    public void keyReleased() {
        super.keyReleased();
        controller.processKeyReleased(key, keyCode);
    }

    @Override
    public void keyTyped() {
        super.keyTyped();
        controller.processKeyTyped(key, keyCode);
    }

    @Override
    public void mouseWheel(MouseEvent event) {
        super.mouseWheel(event);
        controller.processMouseWheelEvent(event);
    }

    private void drawMouseState(){
        SketchController.MouseState mouseState = controller.getMouseState();
        float mouseX = mouseState.getMouseX(), mouseY = mouseState.getMouseY(), mouseRadius =
                controller.getMouseRadius();
        ellipseMode(CENTER);
        strokeWeight(1);
        stroke(255);
        noFill();
        ellipse(mouseX, mouseY, mouseRadius * 2, mouseRadius * 2);
    }

    @Override
    public void draw() {
        background(0);

        controller.setMouseState(new SketchController.MouseState(mousePressed, mouseButton, mouseX, mouseY, pmouseX,
                pmouseY));
        controller.processControllerState();

        /*
        waterGraphics.beginDraw();
        waterGraphics.background(0);
        waterGraphics.noStroke();
        manager.update(this.getGraphics());
        waterGraphics.endDraw();

        image(waterGraphics, 0, 0, width, height);
        */

        manager.update();
        Set<WaterPolygon> polygons = manager.getPolygons();
        blendMode(LIGHTEST);
        noStroke();
        imageMode(CENTER);

        for(WaterPolygon polygon : polygons){
            Vector centerVector = polygon.getCenter();
            tint(polygon.getFillColor(getGraphics()));
            if(polygon.pointsTowardBottom()){
                image(polygonImage, centerVector.x, centerVector.y, polygonImage.width, polygonImage.height);
            } else {
                image(upsideDownPolygonImage, centerVector.x, centerVector.y, upsideDownPolygonImage.width,
                        upsideDownPolygonImage.height);
            }
            /*
            WaterVector v1 = polygon.getV1(), v2 = polygon.getV2(), v3 = polygon.getV3();
            fill(polygon.getFillColor(getGraphics()));
            beginShape(TRIANGLES);
            vertex(v1.x, v1.y);
            vertex(v2.x, v2.y);
            vertex(v3.x, v3.y);
            endShape();
            */
        }

        drawMouseState();

        if(controller.isDiagOverlayEnabled()) {
            int i = 1;
            textSize(UI_DEFAULT_TEXT_SIZE);
            for(Map.Entry<String, Color> diagStringEntry : getDiagnosticStrings()){
                fill(convertColor(diagStringEntry.getValue()));
                text(diagStringEntry.getKey(), UI_PADDING_SIZE, i * UI_PADDING_SIZE + i * UI_DEFAULT_TEXT_SIZE);
                i++;
            }
        }
    }

    private int convertColor(Color color){
        if(color == null) return color(0, 0);
        return color(color.getR(), color.getG(), color.getB(), color.getA());
    }

    private ArrayList<Map.Entry<String, Color>> getDiagnosticStrings(){
        ArrayList<Map.Entry<String, Color>> strings = new ArrayList<>();

        String framerateLabel = "FPS: " + (float) ((int) ((float) frameRate * pow(10, UI_FPS_DECIMAL_PRECISION))) /
                pow(10, UI_FPS_DECIMAL_PRECISION);
        Color framerateColor = new Color(255);

        String mouseStrengthLabel = "Mouse strength: " + (int) controller.getMouseStrength();
        Color mouseStrengthColor = framerateColor;

        strings.add(new AbstractMap.SimpleEntry<String, Color>(framerateLabel, framerateColor));
        strings.add(new AbstractMap.SimpleEntry<String, Color>(mouseStrengthLabel, mouseStrengthColor));

        return strings;
    }

    public static void main(String args[]){
        PApplet.main("sketches.WaterSketch");
    }
}
