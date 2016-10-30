package physics;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * Created by psweeney on 9/14/16.
 */
public class CompoundMotionPath implements MotionPath {
    private ArrayList<SimpleMotionPath> subPaths;
    private float distance;

    public CompoundMotionPath(ArrayList<SimpleMotionPath> subPaths){
        this.subPaths = new ArrayList<>();
        this.distance = 0;

        for(SimpleMotionPath subPath : subPaths){
            this.subPaths.add(subPath.clone());
            distance += subPath.getDistance();
        }
    }

    @Override
    public float getDistance() {
        return distance;
    }

    @Override
    public MotionPath clone() {
        return new CompoundMotionPath(subPaths);
    }

    @Override
    public MotionState getStateAtDistance(float distance){
        if(distance <= 0){
            return subPaths.get(0).getStateAtDistance(0);
        }
        float distanceRemaining = distance;
        MotionState currentState = new MotionState(0, 0, 0, 0, 0);
        for(SimpleMotionPath subPath : subPaths){
            if(subPath.getDistance() <= distanceRemaining){
                distanceRemaining -= subPath.getDistance();
                currentState = currentState.add(subPath.getStateAtDistance(subPath.getDistance()));
            } else {
                return currentState.add(subPath.getStateAtDistance(distanceRemaining));
            }
        }

        MotionPath lastSubPath = subPaths.get(subPaths.size() - 1);
        return lastSubPath.getStateAtDistance(lastSubPath.getDistance());
    }

    @Override
    public Iterator<MotionState> getPathIterator(boolean useStepLimit, float numSteps) {
        if(subPaths.size() <= 0){
            return new Iterator<MotionState>() {
                @Override
                public boolean hasNext() {
                    return false;
                }

                @Override
                public MotionState next() {
                    return null;
                }
            };
        }

        return new Iterator<MotionState>() {
            private float totalDistanceTraveled = 0;

            @Override
            public boolean hasNext() {
                return totalDistanceTraveled < distance;
            }

            @Override
            public MotionState next() {
                MotionState state = getStateAtDistance(totalDistanceTraveled);
                if(useStepLimit){
                    totalDistanceTraveled += distance/numSteps;
                } else {
                    totalDistanceTraveled += 1;
                }
                return state;
            }
        };
    }
}
