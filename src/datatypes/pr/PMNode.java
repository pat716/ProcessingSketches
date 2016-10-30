package datatypes.pr;

import math.Line;
import math.Vector;

import java.util.Set;

/**
 * Created by psweeney on 9/17/16.
 */
public interface PMNode {
    public static boolean existsWithinRegion(float x, float y, float lx, float ly, float w, float h){
        if(x >= lx && x < lx + w && y >= ly && y < ly + h){
            return true;
        }
        return false;
    }

    public int numPoints();
    public int numEdges();
    public boolean isValid();
    public PMNode convertToValid();
    public Vector getPointAtLocation(Vector location);
    public Line getLineBetweenVectors(Vector v1, Vector v2);
    public PMNode putPoint(Vector point);
    public PMNode putLine(Line line);
    public PMNode removePoint(Vector point);
    public PMNode removeLine(Line line);
    public Set<Vector> getAllPoints();
    public Set<Vector> getAllPointsWithinRadius(Vector centerPoint, float radius);
    public Set<Line> getAllLines();
    public Set<Line> getAllConnectedLines(Vector point);
    public Set<Line> getAllLinesWithinRadius(Vector centerPoint, float radius);
}
