package special.audioblur;

import ddf.minim.analysis.FFT;
import display.Color;
import display.ColorSpectrum;
import model.AudioBlurSketchDataModel;
import processing.core.PApplet;

import java.util.ArrayList;

/**
 * Created by psweeney on 6/13/17.
 */
public class FFTHelper {
    private FFT fft;
    private AudioBlurSketchDataModel model;
    private ArrayList<Float> bands;
    private float maxAmplitude = 0;
    private ColorSpectrum colorSpectrum;
    private int firstBand, lastBand;

    public FFTHelper(FFT fft, AudioBlurSketchDataModel model, ColorSpectrum colorSpectrum, float lowerCutoffPercentage,
                     float upperCutoffPercentage){
        this.colorSpectrum = colorSpectrum;
        bands = new ArrayList<>();
        this.model = model;
        this.fft = fft;
        firstBand = PApplet.floor(((float) fft.specSize()) * lowerCutoffPercentage);
        lastBand = PApplet.ceil(fft.specSize() - ((float) fft.specSize()) * upperCutoffPercentage);
        update();
    }

    public AudioBlurSketchDataModel getModel() {
        return model;
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
        return getBand(i) / maxAmplitude;
    }

    public int getNumBands(){
        return bands.size();
    }

    public float getMaxAmplitude() {
        return maxAmplitude;
    }

    public Color getColorForBand(float band){
        float ratio = (band) / ((float) bands.size());
        return colorSpectrum.getColorForFraction(ratio);
    }

    public ColorSpectrum getColorSpectrum() {
        return colorSpectrum;
    }

    public void setColorSpectrum(ColorSpectrum colorSpectrum) {
        this.colorSpectrum = colorSpectrum;
    }

    public void update(){
        maxAmplitude = 0;
        bands.clear();
        for(int i = firstBand; i < lastBand; i++){
            float currFreq = fft.getBand(i) - model.getAmplitudeCutoff();
            this.bands.add(currFreq);
            if(currFreq > maxAmplitude){
                maxAmplitude = currFreq;
            }
        }
    }
}
