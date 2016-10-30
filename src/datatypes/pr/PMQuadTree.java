package datatypes.pr;

import math.Line;
import math.Vector;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by psweeney on 9/18/16.
 */
public class PMQuadTree {
    private Region region;
    private PMNode root;

    public PMQuadTree(Region region){
        this.region = region;
        this.root = PMWhiteNode.getInstance();
    }

    public PMQuadTree(Set<Vector> points, Set<Line> lines, Region region){
        this(region);
        if((points == null || points.size() == 0) || (lines == null || lines.size() == 0)){
            return;
        }

        root = new PMBlackNode(region);

        for(Vector p : points){
            root = root.putPoint(p);
        }

        for(Line l : lines){
            root = root.putLine(l);
        }
    }

    public Region getRegion() {
        return region;
    }

    public Vector getPointAtLocation(Vector location){
        if(location == null) return null;
        return root.getPointAtLocation(location);
    }

    public Line getLineBetweenVectors(Vector v1, Vector v2){
        if(v1 == null || v2 == null) return null;
        return root.getLineBetweenVectors(v1, v2);
    }

    public void putPoint(Vector point){
        if(point == null || !region.inBounds(point)){
            return;
        }

        if(root instanceof PMWhiteNode){
            root = new PMBlackNode(point, region);
        } else {
            root = root.putPoint(point);
        }
    }

    public void putLine(Line line){
        if(line == null || !region.inBounds(line) || !region.inBounds(line.getV1()) || !region.inBounds(line.getV2())){
            return;
        }

        if(root instanceof PMWhiteNode){
            Set<Vector> points = new HashSet<>();
            points.add(line.getV1());
            points.add(line.getV2());

            Set<Line> lines = new HashSet<>();
            lines.add(line);

            root = new PMGreyNode(points, lines, region);
        } else {
            root = root.putLine(line);
        }
    }

    public void removePoint(Vector point){
        Set<Line> lines = root.getAllConnectedLines(point);
        for(Line l : lines){
            root = root.removeLine(l);
        }
        root = root.removePoint(point);
    }

    public void removeLine(Line line){
        root = root.removeLine(line);
    }

    public Set<Vector> getAllPoints(){
        return root.getAllPoints();
    }

    public Set<Vector> getAllPointsWithinRadius(Vector centerPoint, float radius) {
        return root.getAllPointsWithinRadius(centerPoint, radius);
    }

    public Set<Line> getAllLinesWithinRadius(Vector centerPoint, float radius){
        return root.getAllLinesWithinRadius(centerPoint, radius);
    }
}
