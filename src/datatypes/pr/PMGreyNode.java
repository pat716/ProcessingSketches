package datatypes.pr;

import math.Line;
import math.Vector;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created by psweeney on 9/17/16.
 */
public class PMGreyNode implements PMNode {
    private Map<Region.SubQuadrant, PMNode> subNodeMap;
    private Region region;

    public PMGreyNode(Set<Vector> points, Set<Line> edges, Region region){
        this.region = region;
        subNodeMap = new HashMap<>();
        subNodeMap.put(Region.SubQuadrant.LX_LY, PMWhiteNode.getInstance());
        subNodeMap.put(Region.SubQuadrant.LX_UY, PMWhiteNode.getInstance());
        subNodeMap.put(Region.SubQuadrant.UX_LY, PMWhiteNode.getInstance());
        subNodeMap.put(Region.SubQuadrant.UX_UY, PMWhiteNode.getInstance());

        if(points != null) for(Vector p : points) putPoint(p);
        if(edges != null) for(Line l : edges) putLine(l);
    }

    public PMGreyNode(Region region){
        this(null, null, region);
    }

    @Override
    public int numPoints() {
        int total = 0;
        for(PMNode subNode : subNodeMap.values()){
            total += subNode.numPoints();
        }
        return total;
    }

    @Override
    public int numEdges() {
        return getAllLines().size();
    }

    @Override
    public boolean isValid() {
        int bCount = 0, gCount = 0;

        for(PMNode node : subNodeMap.values()){
            if(node.isValid() && node instanceof PMGreyNode){
                gCount++;
            } else if(node.isValid() && node instanceof PMBlackNode){
                bCount++;
            }
        }

        if(gCount > 0 || bCount > 1){
            return true;
        }

        return false;
    }

    @Override
    public PMNode convertToValid() {
        if(isValid()){
            return this;
        }

        Map<Region.SubQuadrant, PMNode> newMappings = new HashMap<>();

        for(Map.Entry<Region.SubQuadrant, PMNode> entry : subNodeMap.entrySet()){
            if(!entry.getValue().isValid()){
                newMappings.put(entry.getKey(), entry.getValue().convertToValid());
            }
        }

        Set<Vector> points = getAllPoints();
        Set<Line> edges = getAllLines();

        if(points.size() <= 0 && edges.size() <= 0){
            return PMWhiteNode.getInstance();
        } else {
            for(Vector v : points) putPoint(v);
            for(Line l : edges) putLine(l);
            return this;
        }
    }

    @Override
    public Vector getPointAtLocation(Vector location) {
        Region.SubQuadrant quadrant = region.getSubQuadrantForVector(location);
        if(quadrant != Region.SubQuadrant.OUT_OF_BOUNDS) return subNodeMap.get(quadrant).getPointAtLocation(location);
        return null;
    }

    @Override
    public Line getLineBetweenVectors(Vector v1, Vector v2) {
        Region.SubQuadrant quadrant = region.getSubQuadrantForVector(v1);
        if(quadrant != Region.SubQuadrant.OUT_OF_BOUNDS) return subNodeMap.get(quadrant).getLineBetweenVectors(v1, v2);
        return null;
    }

    @Override
    public PMNode putPoint(Vector point) {
        if(point == null){
            return this;
        }

        Region.SubQuadrant quadrant = region.getSubQuadrantForVector(point);

        if(quadrant == Region.SubQuadrant.OUT_OF_BOUNDS){
            return this;
        }

        PMNode quadrantNode = subNodeMap.get(quadrant);

        if(quadrantNode instanceof PMWhiteNode){
            subNodeMap.put(quadrant, new PMBlackNode(point, region.getRegionForQuadrant(quadrant)));
        } else {
            subNodeMap.put(quadrant, quadrantNode.putPoint(point));
        }

        return this;
    }

    @Override
    public PMNode putLine(Line line) {
        if(line == null){
            return this;
        }

        Set<Region.SubQuadrant> quadrants = region.getSubQuadrantsForLine(line);
        for(Region.SubQuadrant q : quadrants){
            PMNode n = subNodeMap.get(q);

            if(n instanceof PMWhiteNode){
                Set<Line> lineSet = new HashSet<>();
                lineSet.add(line);
                subNodeMap.put(q, new PMBlackNode(lineSet, region.getRegionForQuadrant(q)));
            } else {
                subNodeMap.put(q, n.putLine(line));
            }
        }

        return this;
    }

    @Override
    public PMNode removePoint(Vector point) {
        Region.SubQuadrant quadrant = region.getSubQuadrantForVector(point);
        if(quadrant == Region.SubQuadrant.OUT_OF_BOUNDS){
            return this;
        }

        PMNode quadrantNode = subNodeMap.get(quadrant);
        if(quadrantNode instanceof PMWhiteNode){
            return this;
        }

        subNodeMap.put(quadrant, quadrantNode.removePoint(point));
        return convertToValid();
    }

    @Override
    public PMNode removeLine(Line line) {
        Set<Region.SubQuadrant> quadrants = region.getSubQuadrantsForLine(line);
        for(Region.SubQuadrant q : quadrants){
            subNodeMap.put(q, subNodeMap.get(q).removeLine(line));
        }
        return convertToValid();
    }

    @Override
    public Set<Vector> getAllPoints() {
        Set<Vector> points = new HashSet<>();

        for(PMNode n : subNodeMap.values()){
            points.addAll(n.getAllPoints());
        }

        return points;
    }

    @Override
    public Set<Vector> getAllPointsWithinRadius(Vector centerPoint, float radius) {
        Set<Vector> points = new HashSet<>();
        for(PMNode n : subNodeMap.values()){
            if(n instanceof PMGreyNode){
                PMGreyNode gn = (PMGreyNode) n;
                if(gn.region.withinRadiusOfPoint(centerPoint, radius)){
                    points.addAll(gn.getAllPointsWithinRadius(centerPoint, radius));
                }
            } else if(n instanceof PMBlackNode){
                PMBlackNode bn = (PMBlackNode) n;
                if(bn.getRegion().withinRadiusOfPoint(centerPoint, radius)){
                    points.addAll(bn.getAllPointsWithinRadius(centerPoint, radius));
                }
            } else {
                continue;
            }
        }
        return points;
    }

    @Override
    public Set<Line> getAllLines() {
        Set<Line> edges = new HashSet<>();

        for(PMNode n : subNodeMap.values()){
            edges.addAll(n.getAllLines());
        }

        return edges;
    }

    @Override
    public Set<Line> getAllConnectedLines(Vector point) {
        if(point == null) return new HashSet<>();

        Region.SubQuadrant q = region.getSubQuadrantForVector(point);
        if(q == Region.SubQuadrant.OUT_OF_BOUNDS) return new HashSet<>();
        return subNodeMap.get(q).getAllConnectedLines(point);
    }

    @Override
    public Set<Line> getAllLinesWithinRadius(Vector centerPoint, float radius) {
        Set<Line> edges = new HashSet<>();
        for(PMNode n : subNodeMap.values()){
            if(n instanceof PMGreyNode){
                PMGreyNode gn = (PMGreyNode) n;
                if(gn.region.withinRadiusOfPoint(centerPoint, radius)){
                    edges.addAll(gn.getAllLinesWithinRadius(centerPoint, radius));
                }
            } else if(n instanceof PMBlackNode){
                PMBlackNode bn = (PMBlackNode) n;
                if(bn.getRegion().withinRadiusOfPoint(centerPoint, radius)){
                    edges.addAll(bn.getAllLinesWithinRadius(centerPoint, radius));
                }
            } else {
                continue;
            }
        }
        return edges;
    }
}
