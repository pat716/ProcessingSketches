package model;

import controller.SketchController;
import datatypes.geom.linear.twodimensional.Line2D;
import display.Color;
import processing.core.PGraphics;
import sketch.Sketch;

/**
 * Created by psweeney on 2/7/17.
 */
public abstract class SketchDataModel {
    private SketchController controller;
    private Sketch sketch;

    public SketchDataModel(SketchController controller, Sketch sketch){
        this.controller = controller;
        this.sketch = sketch;
    }

    public SketchController getController() {
        return controller;
    }

    public Sketch getSketch() {
        return sketch;
    }

    public abstract void applyControllerStateToModel();
    public abstract void applyModelStateToSketch(PGraphics canvas, Color.ColorMode colorMode);
}
