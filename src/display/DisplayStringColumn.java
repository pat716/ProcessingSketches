package display;

import processing.core.PGraphics;
import sketch.Sketch;

import java.util.ArrayList;

/**
 * Created by psweeney on 3/5/17.
 */
public class DisplayStringColumn {
    private ArrayList<DisplayString> strings;

    public DisplayStringColumn(ArrayList<DisplayString> strings){
        this.strings = new ArrayList<>(strings);
    }

    public void addDisplayString(DisplayString displayString){
        strings.add(displayString);
    }

    public void draw(PGraphics sketch, Color.ColorMode colorMode, float xOffset, float yOffset, float gapSize){
        float currY = yOffset;
        for(DisplayString displayString : strings){
            displayString.draw(sketch, colorMode, xOffset, currY);
            currY += displayString.getTextSize() + gapSize;
        }
    }
}
