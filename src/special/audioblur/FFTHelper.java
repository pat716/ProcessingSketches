package special.audioblur;

import ddf.minim.analysis.FFT;
import display.ColorSpectrum;
import model.AudioBlurSketchDataModel;
import processing.core.PApplet;

import java.util.ArrayList;

/**
 * Created by psweeney on 6/13/17.
 */
public class FFTHelper {
    private FFTSample currentSample;
    private FFT fftCenter, fftLeft, fftRight;
    private AudioBlurSketchDataModel model;
    //private ArrayList<Float> bands;
    //private float maxAmplitude = 0;
    private ColorSpectrum colorSpectrum;
    private int firstBand, lastBand;

    public FFTHelper(FFT fftCenter, FFT fftLeft, FFT fftRight, AudioBlurSketchDataModel model,
                     ColorSpectrum colorSpectrum, float lowerCutoffPercentage,
                     float upperCutoffPercentage){
        this.colorSpectrum = colorSpectrum;
        //bands = new ArrayList<>();

        this.model = model;
        this.fftCenter = fftCenter;
        this.fftLeft = fftLeft;
        this.fftRight = fftRight;
        firstBand = PApplet.floor(((float) fftCenter.specSize()) * lowerCutoffPercentage);
        lastBand = PApplet.ceil(fftCenter.specSize() - ((float) fftCenter.specSize()) * upperCutoffPercentage);
        currentSample = new FFTSample(new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), colorSpectrum, model, 0, firstBand, lastBand);
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
        ArrayList<Float> centerBands = new ArrayList<>(), leftBands = new ArrayList<>(), rightBands = new ArrayList<>();
        float maxAmplitude = 0;
        for(int i = firstBand; i < lastBand; i++){
            float currFreq = fftCenter.getBand(i) - model.getAmplitudeCutoff();
            centerBands.add(currFreq);
            if(currFreq > maxAmplitude){
                maxAmplitude = currFreq;
            }

            currFreq = fftLeft.getBand(i) - model.getAmplitudeCutoff();
            leftBands.add(currFreq);
            if(currFreq > maxAmplitude){
                maxAmplitude = currFreq;
            }

            currFreq = fftRight.getBand(i) - model.getAmplitudeCutoff();
            rightBands.add(currFreq);
            if(currFreq > maxAmplitude){
                maxAmplitude = currFreq;
            }
        }
        currentSample = new FFTSample(centerBands, leftBands, rightBands, colorSpectrum, model, maxAmplitude, firstBand, lastBand);
    }
}
