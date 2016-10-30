package datatypes.pr;

import math.Line;
import math.Vector;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by psweeney on 9/17/16.
 */
public class PMWhiteNode implements PMNode {
    private static PMWhiteNode singleton = null;

    private PMWhiteNode(){}

    public static PMWhiteNode getInstance(){
        if(singleton == null){
            singleton = new PMWhiteNode();
        }
        return singleton;
    }

    @Override
    public boolean isValid() {
        return true;
    }

    @Override
    public int numPoints() {
        return 0;
    }

    @Override
    public int numEdges() {
        return 0;
    }

    @Override
    public PMNode convertToValid() {
        return singleton;
    }

    @Override
    public Vector getPointAtLocation(Vector location) {
        return null;
    }

    @Override
    public Line getLineBetweenVectors(Vector v1, Vector v2) {
        return null;
    }

    @Override
    public PMNode putPoint(Vector point) {
        return this;
    }

    @Override
    public PMNode putLine(Line line) {
        return this;
    }

    @Override
    public PMNode removePoint(Vector point) {
        return this;
    }

    @Override
    public PMNode removeLine(Line line) {
        return this;
    }

    @Override
    public Set<Vector> getAllPoints() {
        return new HashSet<>();
    }

    @Override
    public Set<Vector> getAllPointsWithinRadius(Vector centerPoint, float radius) {
        return new HashSet<>();
    }

    @Override
    public Set<Line> getAllLines() {
        return new HashSet<>();
    }

    @Override
    public Set<Line> getAllConnectedLines(Vector point) {
        return new HashSet<>();
    }

    @Override
    public Set<Line> getAllLinesWithinRadius(Vector centerPoint, float radius) {
        return new HashSet<>();
    }
}
