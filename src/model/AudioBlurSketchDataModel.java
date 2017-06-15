package model;

import controller.SketchController;
import datatypes.shape.MovingShape;
import ddf.minim.AudioInput;
import ddf.minim.Minim;
import ddf.minim.analysis.FFT;
import display.Color;
import display.ColorSpectrum;
import processing.core.PApplet;
import processing.core.PGraphics;
import sketch.AudioBlurSketch;
import sketch.Sketch;
import special.audioblur.FFTHelper;
import special.audioblur.SoundBall;
import special.audioblur.AudioBlurDisplayString;

import java.util.*;

import static processing.core.PConstants.*;

/**
 * Created by psweeney on 2/28/17.
 */
public class AudioBlurSketchDataModel extends SketchDataModel {
    private static final Color BLACK = new Color(0, 1),
            PURPLE = new Color(150, 10, 255, 255),
            BLUE = new Color(10, 50, 255, 255),
            GREEN = new Color(60, 255, 10, 255),
            YELLOW = new Color(255, 220, 20, 255),
            ORANGE = new Color(255, 150, 20, 255),
            RED = new Color(255, 10, 70, 255);

    private static final ColorSpectrum RAINBOW = new ColorSpectrum(
            Arrays.asList(
                    new AbstractMap.SimpleEntry<Float, Color>(0.5f, BLACK),
                    new AbstractMap.SimpleEntry<Float, Color>(2f, PURPLE),
                    new AbstractMap.SimpleEntry<Float, Color>(3f, BLUE),
                    new AbstractMap.SimpleEntry<Float, Color>(5f, GREEN),
                    new AbstractMap.SimpleEntry<Float, Color>(3f, YELLOW),
                    new AbstractMap.SimpleEntry<Float, Color>(3f, ORANGE)
            ),
            RED
    );

    private static final ColorSpectrum HOT = new ColorSpectrum(
            Arrays.asList(
                    new AbstractMap.SimpleEntry<Float, Color>(0.5f, BLACK),
                    new AbstractMap.SimpleEntry<Float, Color>(4f, YELLOW),
                    new AbstractMap.SimpleEntry<Float, Color>(2f, ORANGE)
            ),
            RED
    );

    private static final ColorSpectrum COOL = new ColorSpectrum(
            Arrays.asList(
                    new AbstractMap.SimpleEntry<Float, Color>(0.5f, BLACK),
                    new AbstractMap.SimpleEntry<Float, Color>(4f, GREEN),
                    new AbstractMap.SimpleEntry<Float, Color>(2f, BLUE)
            ),
            PURPLE
    );

    private static final List<ColorSpectrum> colorSpectrums = Arrays.asList(
            RAINBOW,
            HOT,
            COOL
    );

    private int spectrumIndex = 0;

    private static final float VOLUME_MULTIPLIER_INC_AMOUNT = 1f;
    private static final int START_SOUNDBALL_COUNT = 400;
    private static final int SOUNDBALL_NUM_INC_AMOUNT = 20;
    private Set<Character> newKeysPressed, newKeysReleased;
    private Set<Integer> newKeyCodesPressed, newKeyCodesReleased;
    private Minim minim;
    private AudioInput in;
    private FFT fft;
    private FFTHelper fftHelper;
    private Set<SoundBall> soundBalls;
    private ArrayList<SoundBall> soundBallList;
    private float volumeMultiplier = 100;
    private float amplitudeCutoff = 0.15f;
    private float primarySoundballGraphicsImageAlpha = 100;
    private float secondarySoundballGraphicsClearAlpha = 5;

    private boolean useVariableAlphaBuffers = false;
    private boolean paused = false;

    private MovingShape.MotionDrawMode motionDrawMode = MovingShape.MotionDrawMode.FASTEST_BLUR;

    public AudioBlurSketchDataModel(SketchController controller, Sketch sketch){
        super(controller, sketch);
        newKeysPressed = new HashSet<>();
        newKeysReleased = new HashSet<>();
        newKeyCodesPressed = new HashSet<>();
        newKeyCodesReleased = new HashSet<>();

        minim = new Minim(sketch);

        in = minim.getLineIn();
        in.enableMonitoring();
        fft = new FFT(in.bufferSize(), in.sampleRate());
        in.mute();
        fftHelper = new FFTHelper(fft, this, colorSpectrums.get(spectrumIndex), 0, .6f);

        soundBalls = new TreeSet<>(new Comparator<SoundBall>() {
            @Override
            public int compare(SoundBall o1, SoundBall o2) {
                if(o1.getRadius() > o2.getRadius()) return 1;
                else if(o1.getRadius() < o2.getRadius()) return -1;
                return 0;
            }
        });
        soundBallList = new ArrayList<>();
        for(int i = 0; i < START_SOUNDBALL_COUNT; i++){
            SoundBall randomSoundBall = SoundBall.generateRandomSoundBall(fftHelper);
            soundBalls.add(randomSoundBall);
            soundBallList.add(randomSoundBall);
        }

        sketch.addDisplayStringToDiagInfoColumn(
                new AudioBlurDisplayString("volume_multiplier",
                        new Color(1, 1), 16, this));
        sketch.addDisplayStringToDiagInfoColumn(
                new AudioBlurDisplayString("num_soundballs",
                        new Color(1, 1), 16, this));
        sketch.addDisplayStringToDiagInfoColumn(
                new AudioBlurDisplayString("variable_alpha",
                        new Color(1, 1), 16, this));
        sketch.addDisplayStringToDiagInfoColumn(
                new AudioBlurDisplayString("motion_draw_mode",
                        new Color(1, 1), 16, this));
        sketch.addDisplayStringToDiagInfoColumn(
                new AudioBlurDisplayString("amplitude_cutoff",
                        new Color(1, 1), 16, this));
    }

    public boolean isPaused() {
        return paused;
    }

    public MovingShape.MotionDrawMode getMotionDrawMode() {
        return motionDrawMode;
    }

    public int getNumSoundballs(){
        return soundBalls.size();
    }

    public float getVolumeMultiplier() {
        return volumeMultiplier;
    }

    public float getAmplitudeCutoff() {
        return amplitudeCutoff;
    }

    public float getPrimarySoundballGraphicsImageAlpha() {
        return primarySoundballGraphicsImageAlpha;
    }

    public float getSecondarySoundballGraphicsClearAlpha() {
        return secondarySoundballGraphicsClearAlpha;
    }

    public boolean useVariableAlphaBuffers() {
        return useVariableAlphaBuffers;
    }

    @Override
    public void applyControllerStateToModel() {
        newKeysPressed.clear();
        newKeysReleased.clear();
        newKeyCodesPressed.clear();
        newKeyCodesReleased.clear();

        HashMap<Character, Boolean> newKeys = getController().getNewKeys();
        HashMap<Integer, Boolean> newKeyCodes = getController().getNewKeyCodes();

        for(Character c : newKeys.keySet()){
            if(newKeys.get(c)) newKeysPressed.add(c);
            else newKeysReleased.add(c);
        }

        for(Integer i : newKeyCodes.keySet()){
            if(newKeyCodes.get(i)) newKeyCodesPressed.add(i);
            else newKeyCodesReleased.add(i);
        }

        if(newKeysPressed.contains('a') || newKeysPressed.contains('A')){
            useVariableAlphaBuffers = !useVariableAlphaBuffers;
        }

        if(newKeysPressed.contains('e') || newKeysPressed.contains('E')){
            for(int i = 0; i < SOUNDBALL_NUM_INC_AMOUNT; i++){
                SoundBall randomSoundBall = SoundBall.generateRandomSoundBall(fftHelper);
                soundBalls.add(randomSoundBall);
                soundBallList.add(randomSoundBall);
            }
        }

        if(newKeysPressed.contains('f') || newKeysPressed.contains('F')){
            motionDrawMode = MovingShape.getNextMotionDrawMode(motionDrawMode);
        }

        if(newKeysPressed.contains('i') || newKeysPressed.contains('I')){
            getSketch().toggleShowDiagInfo();
        }

        if(newKeysPressed.contains('o') || newKeysPressed.contains('O')){
            spectrumIndex = (spectrumIndex + 1) % colorSpectrums.size();
            fftHelper.setColorSpectrum(colorSpectrums.get(spectrumIndex));
        }

        if(newKeysPressed.contains('p') || newKeysPressed.contains('P')){
            paused = !paused;
        }

        if(newKeysPressed.contains('q') || newKeysPressed.contains('Q')){
            for(int i = 0; i < SOUNDBALL_NUM_INC_AMOUNT; i++){
                int index = Math.round(Sketch.randomFloat(soundBallList.size() - 1));
                SoundBall soundBall = soundBallList.get(index);
                soundBalls.remove(soundBall);
                soundBallList.remove(index);
            }
        }

        if(newKeysPressed.contains('w') || newKeysPressed.contains('W')){
            ((AudioBlurSketch) getSketch()).toggleWhiteMode();
        }

        if(newKeyCodesPressed.contains(UP)){
            volumeMultiplier += VOLUME_MULTIPLIER_INC_AMOUNT;
        }

        if(newKeyCodesPressed.contains(DOWN)){
            volumeMultiplier = PApplet.max(0, volumeMultiplier - VOLUME_MULTIPLIER_INC_AMOUNT);
        }
    }

    private void drawBands(PGraphics canvas, Color.ColorMode colorMode, float height, Color color){
        float rectWidth = canvas.width/((float) fftHelper.getNumBands());
        float rectStart = 0;
        canvas.fill(color.convert(canvas, colorMode));
        canvas.noStroke();

        for(int i = 0; i < fftHelper.getNumBands(); i++){
            float currFreq = fftHelper.getBand(i);
            canvas.rect(rectStart, canvas.height/2 - currFreq * (height/2), rectWidth, currFreq * height);
            rectStart += rectWidth;
        }
    }

    @Override
    public void applyModelStateToSketch(PGraphics canvas, Color.ColorMode colorMode) {
        if(((AudioBlurSketch) getSketch()).isWhiteModeActivated()) canvas.background(255, 0);
        else canvas.background(0, 0);
        fft.forward(in.mix.toArray());
        fftHelper.update();

        for(SoundBall soundBall : soundBalls){
            soundBall.update();
            soundBall.draw(canvas, colorMode, motionDrawMode);
        }
    }
}
