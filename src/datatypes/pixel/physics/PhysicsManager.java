package datatypes.pixel.physics;

import datatypes.pixel.Color;
import datatypes.pixel.CustomPixel;
import datatypes.pixel.physics.fixed.ClonePixel;
import datatypes.pr.Region;
import math.Vector;
import processing.core.PApplet;
import processing.core.PGraphics;

import java.util.*;

import static java.lang.Math.PI;

/**
 * Created by psweeney on 9/24/16.
 */
public class PhysicsManager {
    private int width, height;
    private Region region;
    private boolean gravityEnabled = true;
    private Vector gravityDirection = Vector.getVectorRelativeToOrigin((float) PI/2, 1);
    private float gravityStrength = 9.8f/60;

    private int pixelCount = 0;

    //private float gravityStrength = 0;
    private boolean paused = false;

    private static final Comparator<Integer> randomComparator = new Comparator<Integer>() {
        @Override
        public int compare(Integer i1, Integer i2) {
            if(i1 == null && i2 == null) return 0;
            if(i1 == null) return 1;
            if(i2 == null) return -1;
            if(i1.intValue() == i2.intValue()) return 0;
            if(Math.random() <= 0.5) return 1;
            return -1;
        }
    };

    private PhysicsPixel[][] pixels;
    private Set<Integer> pixelUpdates, nextPixelUpdates;

    private int frameCounter = 0;
    private TemperatureObject.TempGridObject[][] tempRegions;
    private PGraphics tempRegionGraphics;
    private static float equilibriumTemperature = 0;
    private static final int MAX_FRAME_COUNTER = 60;
    private static final int TILE_SIZE = 16;

    public PhysicsManager(PApplet applet, int width, int height){
        this.width = width;
        this.height = height;
        region = new Region(0, 0, width, height);
        pixels = new PhysicsPixel[width][height];
        tempRegions = new TemperatureObject.TempGridObject[width][height];
        tempRegionGraphics = applet.createGraphics(width, height);

        tempRegionGraphics.beginDraw();
        tempRegionGraphics.background(0, 0);
        tempRegionGraphics.endDraw();

        pixelUpdates = new HashSet<>();
        nextPixelUpdates = new HashSet<>();
    }

    public boolean isPaused() {
        return paused;
    }

    public void togglePaused(){
        paused = !paused;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public int getPixelCount() {
        return pixelCount;
    }

    public boolean isGravityEnabled(){ return gravityEnabled; }

    public void toggleGravity(){
        gravityEnabled = !gravityEnabled;
    }

    public Vector getGravityVector() {
        if(!gravityEnabled) return new Vector(0, 0);
        return new Vector(gravityDirection.x * gravityStrength, gravityDirection.y * gravityStrength);
    }

    public void applyTempGridToRegion(float lx, float ly, float w, float h, float temperature){
        int intLX = CustomPixel.convertInt(lx), intLY = CustomPixel.convertInt(ly);

        tempRegionGraphics.beginDraw();
        tempRegionGraphics.loadPixels();

        for(int i = intLX; i < intLX + w; i++){
            if(i < 0) continue;
            else if(i >= width) break;

            for(int j = intLY; j < intLY + h; j++){
                if(j < 0) continue;
                else if(j >= height) break;

                TemperatureObject.TempGridObject t = tempRegions[i][j];

                if(t == null){
                    t = new TemperatureObject.TempGridObject(temperature);
                    tempRegions[i][j] = t;
                } else t.addToTemperature(t, temperature);

                int index = i + j * width;
                Color gridColor = t.getColor();
                tempRegionGraphics.pixels[index] = tempRegionGraphics.color(gridColor.getR(), gridColor.getG(),
                        gridColor.getB(), gridColor.getA());

                PhysicsPixel p = getPixel(i, j);
                if(p != null && p instanceof TemperatureObject){
                    nextPixelUpdates.add(p.getIndex());
                }
            }
        }

        tempRegionGraphics.updatePixels();
        tempRegionGraphics.endDraw();
    }

    public void clearTempGridInRegion(float lx, float ly, float w, float h){
        int intLX = CustomPixel.convertInt(lx), intLY = CustomPixel.convertInt(ly);

        tempRegionGraphics.beginDraw();
        tempRegionGraphics.loadPixels();

        for(int i = intLX; i < intLX + w; i++){
            if(i < 0) continue;
            else if(i >= width) break;

            for(int j = intLY; j < intLY + h; j++){
                if(j < 0) continue;
                else if(j >= height) break;
                tempRegions[i][j] = null;
                int index = i + j * width;
                tempRegionGraphics.pixels[index] = tempRegionGraphics.color(0, 0);
            }
        }

        tempRegionGraphics.updatePixels();
        tempRegionGraphics.endDraw();
    }

    public PGraphics getTempRegionImage() {
        return tempRegionGraphics;
    }

    public PhysicsPixel getPixel(int x, int y){
        if(!region.inBounds(new Vector(x, y))) return null;
        return pixels[x][y];
    }

    public PhysicsPixel getPixel(int index){
        if(index < 0 || index >= width * height) return null;
        int x = index % width, y = index / width;
        return getPixel(x, y);
    }

    public PhysicsPixel getPixel(float x, float y){
        return getPixel(PhysicsPixel.convertInt(x), PhysicsPixel.convertInt(y));
    }

    public PhysicsPixel getPixel(Vector v){
        if(v == null) return null;
        return getPixel(v.x, v.y);
    }

    public TemperatureObject.TempGridObject getTempGridObject(int x, int y){
        if(!region.inBounds(new Vector(x, y)) || tempRegions[x][y] == null)
            return new TemperatureObject.TempGridObject(0);

        return tempRegions[x][y];
    }

    public TemperatureObject.TempGridObject getTempGridObject(int index){
        if(index < 0 || index >= width * height)
            return new TemperatureObject.TempGridObject(0);

        int x = index % width, y = index / width;
        return getTempGridObject(x, y);
    }

    public TemperatureObject.TempGridObject getTempGridObject(float x, float y){
        return getTempGridObject(PhysicsPixel.convertInt(x), PhysicsPixel.convertInt(y));
    }

    public TemperatureObject.TempGridObject getTempGridObject(Vector v){
        if(v == null) return new TemperatureObject.TempGridObject(0);
        return getTempGridObject(v.x, v.y);
    }

    public void setPixel(float x, float y, PhysicsPixel p){
        int intX = CustomPixel.convertInt(x), intY = CustomPixel.convertInt(y);
        if(!region.inBounds(new Vector(intX, intY))){
            return;
        }
        if(pixels[intX][intY] == null && p != null) pixelCount++;
        else if(pixels[intX][intY] != null && p == null){
            pixelCount--;
        }

        pixels[intX][intY] = p;

        if(p != null){
            p.setX(x);
            p.setY(y);
            nextPixelUpdates.add(p.getIndex());
        } else {
            nextPixelUpdates.add(intY * width + intX);
        }
    }

    public void setPixel(int index, PhysicsPixel p){
        if(index < 0 || index >= width * height) return;
        int x = index % width, y = index / width;
        setPixel(x, y, p);
    }

    public void setPixel(Vector v, PhysicsPixel p){
        if(v == null) return;
        setPixel(v.x, v.y, p);
    }

    public static float getEquilibriumTemperature() {
        return equilibriumTemperature;
    }

    public void clear(){
        for(int x = 0; x < width; x++){
            for(int y = 0; y < height; y++){
                setPixel(x, y, null);
                tempRegions[x][y] = null;
            }
        }

        tempRegionGraphics.beginDraw();
        tempRegionGraphics.background(0, 0);
        tempRegionGraphics.endDraw();

        pixelUpdates.clear();
        nextPixelUpdates.clear();
        for(int i = 0; i < width * height; i++){
            nextPixelUpdates.add(i);
        }
        pixelCount = 0;
    }

    public void clearClone(){
        for(int x = 0; x < width; x++){
            for(int y = 0; y < height; y++){
                PhysicsPixel currPixel = getPixel(x, y);
                if(currPixel != null && currPixel instanceof ClonePixel)
                    setPixel(x, y, null);
            }
        }
    }

    public void swapPixels(float x1, float y1, float x2, float y2){
        int intX1 = CustomPixel.convertInt(x1), intY1 = CustomPixel.convertInt(y1), intX2 = CustomPixel.convertInt(x2),
                intY2 = CustomPixel.convertInt(y2);
        if(!region.inBounds(new Vector(intX1, intY1)) && !region.inBounds(new Vector(intX2, intY2))) return;
        if(!region.inBounds(new Vector(intX1, intY1))){
            setPixel(intX2, intY2, null);
            return;
        } else if(!region.inBounds(new Vector(intX2, intY2))){
            setPixel(intX1, intY1, null);
            return;
        }

        PhysicsPixel p1 = pixels[intX1][intY1], p2 = pixels[intX2][intY2];

        if(p1 != null){
            p1.setX(x2);
            p1.setY(y2);
        }

        if(p2 != null){
            p2.setX(x1);
            p2.setY(y1);
        }

        setPixel(x1, y1, p2);
        setPixel(x2, y2, p1);
    }

    public Set<Integer> update(){
        frameCounter = (frameCounter + 1) % MAX_FRAME_COUNTER;

        if(paused){
            pixelUpdates.addAll(nextPixelUpdates);
            return pixelUpdates;
        }

        pixelUpdates.clear();
        pixelUpdates.addAll(nextPixelUpdates);
        nextPixelUpdates.clear();
        Set<Integer> randomizedUpdates = new TreeSet<>(randomComparator);
        randomizedUpdates.addAll(pixelUpdates);

        Set<Integer> returnSet = new HashSet<>();
        for(Integer i : randomizedUpdates){
            returnSet.add(i);
            PhysicsPixel p = getPixel(i);
            if(p != null){
                p.update();
                p.setLastUpdateFrame(frameCounter);
                returnSet.add(p.getIndex());
            }
        }

        return returnSet;
    }
}
