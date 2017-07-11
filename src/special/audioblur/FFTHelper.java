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
    private FFTSample currentSample;
    private FFT fft;
    private AudioBlurSketchDataModel model;
    //private ArrayList<Float> bands;
    //private float maxAmplitude = 0;
    private ColorSpectrum colorSpectrum;
    private int firstBand, lastBand;

    public FFTHelper(FFT fft, AudioBlurSketchDataModel model, ColorSpectrum colorSpectrum, float lowerCutoffPercentage,
                     float upperCutoffPercentage){
        this.colorSpectrum = colorSpectrum;
        //bands = new ArrayList<>();

        this.model = model;
        this.fft = fft;
        firstBand = PApplet.floor(((float) fft.specSize()) * lowerCutoffPercentage);
        lastBand = PApplet.ceil(fft.specSize() - ((float) fft.specSize()) * upperCutoffPercentage);
        currentSample = new FFTSample(new ArrayList<>(), colorSpectrum, model, 0, firstBand, lastBand);
        update();
    }

    public AudioBlurSketchDataModel getModel() {
        return model;
    }

    public float getMaxAmplitude() {
        return currentSample.getMaxAmplitude();
    }

    public int getNumBands(){
        return currentSample.getNumBands();
    }

    public ColorSpectrum getColorSpectrum() {
        return colorSpectrum;
    }

    public void setColorSpectrum(ColorSpectrum colorSpectrum) {
        this.colorSpectrum = colorSpectrum;
    }

    public FFTSample getCurrentSample() {
        return currentSample;
    }

    public void update(){
        ArrayList<Float> bands = new ArrayList<>();
        float maxAmplitude = 0;
        bands.clear();
        for(int i = firstBand; i < lastBand; i++){
            float currFreq = fft.getBand(i) - model.getAmplitudeCutoff();
            bands.add(currFreq);
            if(currFreq > maxAmplitude){
                maxAmplitude = currFreq;
            }
        }
        currentSample = new FFTSample(bands, colorSpectrum, model, maxAmplitude, firstBand, lastBand);
    }
}
