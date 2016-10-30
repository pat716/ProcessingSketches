package datatypes.pr;

import math.Line;
import math.Vector;
import processing.core.PApplet;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by psweeney on 9/17/16.
 */
public class PMBlackNode implements PMNode {
    private Vector point;
    private Set<Line> edges;
    private Region region;

    public PMBlackNode(Region region){
        this.point = null;
        this.edges = new HashSet<>();
        this.region = region;
    }

    public PMBlackNode(Vector point, Set<Line> edges, Region region){
        this(region);
        this.point = point;
        this.edges.addAll(edges);
    }

    public PMBlackNode(Vector point, Region region){
        this(point, new HashSet<>(), region);
    }

    public PMBlackNode(Set<Line> edges, Region region){
        this(null, edges, region);
    }

    public Vector getPoint(){
        return point;
    }

    public Region getRegion(){
        return region;
    }

    @Override
    public int numPoints() {
        if(point == null){
            return 0;
        }
        return 1;
    }

    @Override
    public int numEdges() {
        return edges.size();
    }

    @Override
    public boolean isValid() {
        if(point == null && edges.size() <= 0) return false;

        int pointCount = 0;
        if(point != null) pointCount++;

        for(Line l : edges){
            if(region.inBounds(l.getV1())) pointCount++;
            if(region.inBounds(l.getV2())) pointCount++;
        }

        if(pointCount > 1){
            return false;
        }

        return true;
    }

    @Override
    public PMNode convertToValid() {
        if(point == null && edges.size() <= 0) return PMWhiteNode.getInstance();

        Set<Vector> points = new HashSet<>();
        if(point != null){
            points.add(point);
        }

        for(Line l : edges){
            if(region.inBounds(l.getV1())) points.add(l.getV1());
            if(region.inBounds(l.getV2())) points.add(l.getV2());
        }

        if(points.size() > 1){
            return new PMGreyNode(points, edges, region);
        }

        return this;
    }

    public static boolean positionEffectivelyEquals(Vector v1, Vector v2){
        return v1 != null && v2 != null && (int) v1.x == (int) v2.x && (int) v1.y == (int) v2.y;
    }

    @Override
    public Vector getPointAtLocation(Vector location) {
        if(positionEffectivelyEquals(this.point, location)) return this.point;
        return null;
    }

    @Override
    public Line getLineBetweenVectors(Vector v1, Vector v2) {
        if(!positionEffectivelyEquals(this.point, v1)) return null;

        for(Line l : getAllConnectedLines(this.point)){
            if(positionEffectivelyEquals(l.getV1(), v2) || positionEffectivelyEquals(l.getV2(), v2)){
                return l;
            }
        }
        return null;
    }

    @Override
    public PMNode putPoint(Vector point) {
        if(point == null) return this;

        if(this.point == null || positionEffectivelyEquals(point, this.point)){
            this.point = point;
            return this;
        }

        if(region.getWidth() <= 1 || region.getHeight() <= 1){
            return this;
        }

        Set<Vector> points = new HashSet<>();
        points.add(this.point);
        points.add(point);

        return new PMGreyNode(points, null, region);
    }

    @Override
    public PMNode putLine(Line line) {
        if(line == null) return this;

        if(edges.contains(line)) return this;

        edges.add(line);

        Set<Vector> extraPoints = new HashSet<>();
        Vector v1 = line.getV1(), v2 = line.getV2();

        if(region.inBounds(v1) && this.point != v1){
            if(this.point == null){
                this.point = v1;
            } else {
                extraPoints.add(v1);
            }
        }

        if(region.inBounds(v2) && this.point != v2){
            if(this.point == null){
                this.point = v2;
            } else {
                extraPoints.add(v2);
            }
        }

        if(extraPoints.size() > 0){
            extraPoints.add(this.point);
            return new PMGreyNode(extraPoints, edges, region);
        }

        return this;
    }

    @Override
    public PMNode removePoint(Vector point) {
        if(point == null){
            return this;
        }

        Set<Line> removeEdges = new HashSet<>();
        for(Line l : edges){
            if(l.getV1().equals(point) || l.getV2().equals(point)){
                removeEdges.add(l);
            }
        }

        edges.removeAll(removeEdges);

        if(point.equals(this.point)){
            this.point = null;
        }

        return convertToValid();
    }

    @Override
    public PMNode removeLine(Line line) {
        edges.remove(line);
        if(edges.size() <= 0 && point == null){
            return PMWhiteNode.getInstance();
        }
        return this;
    }

    @Override
    public Set<Vector> getAllPoints() {
        Set<Vector> points = new HashSet<>();
        if(point != null){
            points.add(point);
        }
        return points;
    }

    @Override
    public Set<Vector> getAllPointsWithinRadius(Vector centerPoint, float radius) {
        Set<Vector> points = new HashSet<>();
        if(point != null && region.withinRadiusOfPoint(centerPoint, radius)){
            points.add(point);
        }
        return points;
    }

    @Override
    public Set<Line> getAllLines() {
        return edges;
    }

    @Override
    public Set<Line> getAllConnectedLines(Vector point) {
        if(point != null && point.equals(this.point)){
            Set<Line> connectedLines = new HashSet<>();
            for(Line l : edges){
                if(l.getV1().equals(point) || l.getV2().equals(point)) connectedLines.add(l);
            }
            return connectedLines;
        }
        return new HashSet<>();
    }

    @Override
    public Set<Line> getAllLinesWithinRadius(Vector centerPoint, float radius) {
        Set<Line> lines = new HashSet<>();
        for(Line l : edges){
            if(l.ptSegDist(centerPoint) <= radius){
                lines.add(l);
            }
        }

        return lines;
    }
}
