package physics;

import drawing.Circle2D;
import math.Vector;
import processing.core.PApplet;
import sun.security.provider.certpath.Vertex;

import java.util.ArrayList;

/**
 * Created by psweeney on 9/13/16.
 */
public class BounceBall2D extends Circle2D implements PhysicsObject2D {
    private SimpleMotionPath motionPath;
    private float friction;
    private boolean allowMouseControl;
    private Vector nextPosition;
    public BounceBall2D(PApplet applet, DrawFlags flags, float x, float y, float radius, float friction, boolean allowMouseControl){
        super(applet, flags, x, y, radius);
        this.friction = friction;
        nextPosition = new Vector(x, y);
        this.allowMouseControl = allowMouseControl;
        motionPath = new SimpleMotionPath(0, 0);
    }

    private Vector getCollisionPoint(){
        Vector initialPos = new Vector(getX(), getY()), xCollisionPoint = null, yCollisionPoint = null;

        float x = getX(), y = getY(), vx = motionPath.getVx(), vy = motionPath.getVy(), radius = getRadius();

        float progressBeforeXCollision = 1, progressBeforeYCollision = 1;

        if(x + vx < radius){
            progressBeforeXCollision = applet.abs(x - radius)/applet.abs(vx);
        } else if(x + vx > applet.width - radius){
            progressBeforeXCollision = applet.abs(x - (applet.width - radius))/applet.abs(vx);
        }

        if(progressBeforeXCollision < 1){
            xCollisionPoint = new Vector(x + vx * progressBeforeXCollision, y + vy * progressBeforeXCollision);
        }

        if(y + vy < radius){
            progressBeforeYCollision = applet.abs(y - radius)/applet.abs(vy);
        } else if(y + vy > applet.height - radius){
            progressBeforeYCollision = applet.abs(y - (applet.height - radius))/applet.abs(vy);
        }

        if(progressBeforeYCollision < 1){
            yCollisionPoint = new Vector(x + vx * progressBeforeYCollision, y + vy * progressBeforeYCollision);
        }

        if(xCollisionPoint != null && yCollisionPoint != null){
            if(initialPos.dist(xCollisionPoint) < initialPos.dist(yCollisionPoint)){
                return xCollisionPoint;
            }
            return yCollisionPoint;
        } else if(xCollisionPoint != null){
            return xCollisionPoint;
        } else if(yCollisionPoint != null){
            return yCollisionPoint;
        }
        return null;
    }

    public void update(){
        setX(nextPosition.x);
        setY(nextPosition.y);
        if(allowMouseControl && applet.mousePressed){
            nextPosition = new Vector(applet.mouseX, applet.mouseY);
            motionPath.setVx(applet.mouseX - getX());
            motionPath.setVy(applet.mouseY - getY());

            flags.setBlurPath(new SimpleMotionPath(motionPath.getVx(), motionPath.getVy()));

            return;
        }

        motionPath.setVy(motionPath.getVy() + gravity);

        Vector collisionPoint = getCollisionPoint();

        if(collisionPoint == null){
            nextPosition = new Vector(getX() + motionPath.getVx(), getY() + motionPath.getVy());
            flags.setBlurPath(new SimpleMotionPath(motionPath.getVx(), motionPath.getVy()));
            return;
        }

        SimpleMotionPath p1 = new SimpleMotionPath(collisionPoint.x - getX(), collisionPoint.y - getY());

        float p1Ratio = p1.getDistance()/PApplet.dist(0, 0, motionPath.getVx(), motionPath.getVy());

        float newVX = motionPath.getVx() * (1 - friction), newVY = motionPath.getVy() * (1 - friction);

        if(collisionPoint.x <= getRadius()) {
            newVX = PApplet.abs(newVX);
        } else if(collisionPoint.x >= applet.width - getRadius()){
            newVX = -1 * PApplet.abs(newVX);
        } else if(collisionPoint.y <= getRadius()){
            newVY = PApplet.abs(newVY);
        } else {
            newVY = -1 * PApplet.abs(newVY);
        }

        SimpleMotionPath p2 = new SimpleMotionPath(newVX * (1 - p1Ratio), newVY * (1 - p1Ratio));

        nextPosition = new Vector(collisionPoint.x + p2.getVx(), collisionPoint.y + p2.getVy());

        ArrayList<SimpleMotionPath> subPaths = new ArrayList<>();
        subPaths.add(p1);
        subPaths.add(p2);

        flags.setBlurPath(new CompoundMotionPath(subPaths));
        motionPath.setVx(newVX);
        motionPath.setVy(newVY);
    }

    public void updateOriginal() {
        float x = getX(), y = getY(), radius = getRadius(), vx = motionPath.getVx(), vy = motionPath.getVy();

        if(allowMouseControl && applet.mousePressed){
            x = applet.mouseX;
            y = applet.mouseY;
            vx = x - applet.pmouseX;
            vy = y - applet.pmouseY;
        } else {
            vy += gravity;

            x += vx;
            y += vy;

            if (x < radius) {
                x = radius + (radius - x);
                vx *= -1 * (1 - friction);
            } else if (x > applet.width - radius) {
                x = (applet.width - radius) - (x - (applet.width - radius));
                vx *= -1 * (1 - friction);
            }

            if (y < radius) {
                y = radius + (radius - y);
                vy *= -1 * (1 - friction);
            } else if (y > applet.height - radius) {
                y = (applet.height - radius) - (y - (applet.height - radius));
                vy *= -1 * (1 - friction);
            }
        }

        SimpleMotionPath blurPath = new SimpleMotionPath(vx, vy);
        flags.setBlurPath(blurPath);

        motionPath.setVx(vx);
        motionPath.setVy(vy);

        setX(x);
        setY(y);
    }
}
