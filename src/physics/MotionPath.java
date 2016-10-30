package physics;

import java.util.Iterator;

/**
 * Created by psweeney on 9/14/16.
 */
public interface MotionPath {
    public static class MotionState{
        private float x, y, z, angleR, angleP;
        public MotionState(float x, float y, float z, float angleR, float angleP){
            this.x = x;
            this.y = y;
            this.z = z;
            this.angleR = angleR;
            this.angleP = angleP;
        }

        public float getX() {
            return x;
        }

        public float getY() {
            return y;
        }

        public float getZ() {
            return z;
        }

        public float getAngleR() {
            return angleR;
        }

        public float getAngleP() {
            return angleP;
        }

        public MotionState add(MotionState other){
            return new MotionState(x + other.x, y + other.y, z + other.z, angleR + other.angleR, angleP + other.angleP);
        }
    }

    public float getDistance();
    public MotionState getStateAtDistance(float distance);
    public MotionPath clone();
    public Iterator<MotionState> getPathIterator(boolean useStepLimit, float numSteps);
}
