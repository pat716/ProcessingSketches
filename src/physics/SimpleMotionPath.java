package physics;

import processing.core.PApplet;

import java.util.Iterator;

/**
 * Created by psweeney on 9/13/16.
 */
public class SimpleMotionPath implements MotionPath{
    private float vx, vy, vz, angleR, angleP, distance;

    public SimpleMotionPath(float vx, float vy, float vz, float angleR, float angleP){
        this.vx = vx;
        this.vy = vy;
        this.vz = vz;
        this.angleR = angleR;
        this.angleP = angleP;
        this.distance = PApplet.dist(0, 0, 0, vx, vy, vz);
    }

    public SimpleMotionPath(float vx, float vy, float angleR){
        this(vx, vy, 0, angleR, 0);
    }
    public SimpleMotionPath(float vx, float vy){
        this(vx, vy, 0);
    }

    public float getVx() {
        return vx;
    }

    public void setVx(float vx) {
        this.vx = vx;
    }

    public float getVy() {
        return vy;
    }

    public void setVy(float vy) {
        this.vy = vy;
    }

    public float getVz() {
        return vz;
    }

    public void setVz(float vz) {
        this.vz = vz;
    }

    public float getAngleR() {
        return angleR;
    }

    public void setAngleR(float angleR) {
        this.angleR = angleR;
    }

    public float getAngleP() {
        return angleP;
    }

    public void setAngleP(float angleP) {
        this.angleP = angleP;
    }

    @Override
    public float getDistance() {
        return distance;
    }

    @Override
    public MotionState getStateAtDistance(float distance) {
        if(distance <= 0){
            return new MotionState(0, 0, 0, 0, 0);
        } else if(distance >= this.distance){
            return new MotionState(vx, vy, vz, angleR, angleP);
        }

        float progressRatio = distance/this.distance;

        return new MotionState(progressRatio * vx, progressRatio * vy, progressRatio * vz, angleR * progressRatio,
                angleP * progressRatio);
    }

    @Override
    public SimpleMotionPath clone() {
        return new SimpleMotionPath(vx, vy, vz, angleR, angleP);
    }

    public Iterator<MotionPath.MotionState> getPathIterator(boolean useSteps, float numSteps){
        return new Iterator<MotionPath.MotionState>() {
            private float distanceTraveled = 0;
            @Override
            public boolean hasNext() {
                return distanceTraveled < SimpleMotionPath.this.getDistance();
            }

            @Override
            public MotionPath.MotionState next() {
                float distance = SimpleMotionPath.this.distance;
                float x = vx * (distanceTraveled/distance), y = vy * (distanceTraveled/distance), z = vz *
                        (distanceTraveled/distance);
                float angleR = SimpleMotionPath.this.angleR * (distanceTraveled/distance);
                float angleP = SimpleMotionPath.this.angleP * (distanceTraveled/distance);

                if(useSteps){
                    distanceTraveled += PApplet.max(1, distance/numSteps);
                } else {
                    distanceTraveled += 1;
                }

                return new MotionPath.MotionState(x, y, z, angleR, angleP);
            }
        };
    }
}
