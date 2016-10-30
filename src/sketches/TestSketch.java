package sketches;

import drawing.Shape2D;
import physics.BounceBall2D;
import processing.core.PApplet;

/**
 * Created by psweeney on 9/13/16.
 */
public class TestSketch extends PApplet {

    @Override
    public void settings() {
        super.settings();
        size(800, 600);
    }

    public void setup() {
        background(0);
    }

    boolean paused = false;
    Shape2D.DrawFlags mouseDrawFlags = new Shape2D.DrawFlags(this, ADD, color(255, 255), color(255, 0, 0), 5, true, true, null);
    BounceBall2D ball = new BounceBall2D(this, mouseDrawFlags, width/2, height/2, 30, 0.01f, true);

    public void draw(){
        background(0);

        if(!paused)
            ball.update();

        ball.drawShape();
    }

    public static void main(String args[]){
        PApplet.main("sketches.TestSketch");
    }

    @Override
    public void keyTyped() {
        if(key == 'p'){
            paused = !paused;
        }
    }
}
