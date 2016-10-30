package sketches;

import controller.ShadowTestController;
import controller.SketchController;
import datatypes.pixel.Color;
import datatypes.shadow.ShadowMap;
import datatypes.shadow.obj.ShadowCastRectangle;
import processing.core.PApplet;
import processing.event.MouseEvent;

/**
 * Created by psweeney on 10/23/16.
 */
public class ShadowTesting extends PApplet {
    public static final float SQRT_2 = PApplet.sqrt(2);
    public static final float ANGLE_CHECK_INCREMENT = PI/64;
    private static final float SHADOW_EDGE_FADE_START = 0.625f;
    private ShadowTestController controller;
    private ShadowMap shadowMap;

    @Override
    public void settings() {
        super.settings();
        size(400, 300);
    }

    @Override
    public void setup() {
        super.setup();
        controller = new ShadowTestController(new Color(255), 200, 15);
        shadowMap = new ShadowMap(controller.getMouseVector());
        //shadowMap.addShadowCastObject(new ShadowCastLine(width/2, height/2 - 25, width/2, height/2 + 25));
        //shadowMap.addShadowCastObject(new ShadowCastLine(width/2 - 25, height/2, width/2 + 25, height/2));
        //shadowMap.addShadowCastObject(new ShadowCastLine(width/2 - 25, height/2 + 25, width/2 + 25, height/2 - 25));

        shadowMap.addShadowCastObject(new ShadowCastRectangle(width/2 - 75, height/2 - 75, 25, 25, 5));
        shadowMap.addShadowCastObject(new ShadowCastRectangle(width/2 + 25, height/2 - 75, 25, 25, 5));
        shadowMap.addShadowCastObject(new ShadowCastRectangle(width/2 - 75, height/2 + 25, 25, 25, 5));
        shadowMap.addShadowCastObject(new ShadowCastRectangle(width/2 + 25, height/2 + 25, 25, 25, 5));

        background(0);
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

    int counter = 0;
    @Override
    public void draw() {
        boolean print = false;
        if(++counter == 60){
            counter = 0;
            print = true;
        }
        background(0, 255);

        controller.setMouseState(new SketchController.MouseState(mousePressed, mouseButton, mouseX, mouseY, pmouseX,
                pmouseY));
        controller.processControllerState();

        shadowMap.update();
        //shadowMap.draw(getGraphics());



        loadPixels();

        float maxDist = controller.getMouseLightBrightness();

        /*
        for(int x = (int) PApplet.max(0, mouseX - maxDist); x < PApplet.min(width, mouseX + maxDist); x++){
            for(int y = (int) PApplet.max(0, mouseY - maxDist); y < PApplet.min(height, mouseY + maxDist); y++){ */
        Color lightColor = controller.getMouseLightColor();
        for(float x = 0; x < width; x++){
            for(float y = 0; y < height; y++){
                float distance = PApplet.dist(x, y, mouseX, mouseY);
                int index = (int) y * width + (int) x;
                int color = color(0, 255);
                if(distance < maxDist){
                    float lightAlpha = ((1 - (distance/maxDist)) * 255) * shadowMap.getLightMultiplier(x, y,
                            controller.getMouseVector().z, SHADOW_EDGE_FADE_START);
                    color = convertColor(new Color(lightColor.getR(), lightColor.getG(), lightColor.getB(),
                            lightAlpha).flatten());
                }


                pixels[index] = color;
            }
        }

        updatePixels();


        if(print) System.out.println("FPS: " + (int) frameRate);
    }

    private int convertColor(Color color){
        if(color == null) return color(0, 0);
        return color(color.getR(), color.getG(), color.getB(), color.getA());
    }

    public static void main(String args[]){
        PApplet.main("sketches.ShadowTesting");
    }
}
