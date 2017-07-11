package special.audioblur;

import display.Color;
import display.ColorSpectrum;
import model.AudioBlurSketchDataModel;
import processing.core.PApplet;

import java.util.ArrayList;

/**
 * Created by psweeney on 7/11/17.
 */
public class FFTSample {
    private ArrayList<Float> bands;
    private AudioBlurSketchDataModel model;
    private ColorSpectrum colorSpectrum;
    private float maxAmplitude;
    private int firstBand, lastBand;

    public FFTSample(ArrayList<Float> bands, ColorSpectrum colorSpectrum, AudioBlurSketchDataModel model,
                     float maxAmplitude, int firstBand, int lastBand){
        this.bands = bands;
        this.colorSpectrum = colorSpectrum;
        this.model = model;
        this.maxAmplitude = maxAmplitude;
        this.firstBand = firstBand;
        this.lastBand = lastBand;
    }

    public ArrayList<Float> getBands() {
        return bands;
    }

    public ColorSpectrum getColorSpectrum() {
        return colorSpectrum;
    }

    public AudioBlurSketchDataModel getModel() {
        return model;
    }

    public float getMaxAmplitude() {
        return maxAmplitude;
    }

    public int getFirstBand() {
        return firstBand;
    }

    public int getLastBand() {
        return lastBand;
    }

    public float getBand(float i){
        int lowerBand = PApplet.floor(i), upperBand = PApplet.ceil(i);
        float upperRatio = i - lowerBand;
        return bands.get(lowerBand) * (1 - upperRatio) + bands.get(upperBand) * upperRatio;
    }

    public float getVolumeMultipliedBand(float i){
        return getBand(i) * model.getVolumeMultiplier();
    }

    public float getNormalizedBand(float i){
        return getBand(i) / getMaxAmplitude();
    }

    public Color getColorForBand(float band){
        float ratio = (band) / ((float) bands.size());
        return colorSpectrum.getColorForFraction(ratio);
    }

    public int getNumBands(){
        return bands.size();
    }


}
