package model;

import controller.SketchController;
import datatypes.shape.MovingShape;
import ddf.minim.AudioInput;
import ddf.minim.AudioPlayer;
import ddf.minim.AudioSource;
import ddf.minim.Minim;
import ddf.minim.analysis.FFT;
import display.Color;
import display.ColorSpectrum;
import processing.core.PApplet;
import processing.core.PGraphics;
import sketch.AudioBlurSketch;
import sketch.Sketch;
import special.audioblur.FFTHelper;
import special.audioblur.FFTSample;
import special.audioblur.SoundBall;
import special.audioblur.AudioBlurDisplayString;

import java.util.*;

import static processing.core.PConstants.*;

/**
 * Created by psweeney on 2/28/17.
 */
public class AudioBlurSketchDataModel extends SketchDataModel {
    private static final Color BLACK = new Color(0, 1),
            WHITE = new Color(255, 255, 255, 255),
            PURPLE = new Color(150, 10, 255, 255),
            BLUE = new Color(10, 50, 255, 255),
            GREEN = new Color(60, 255, 10, 255),
            YELLOW = new Color(255, 220, 20, 255),
            ORANGE = new Color(255, 150, 20, 255),
            RED = new Color(255, 10, 70, 255);

    private static final ColorSpectrum RAINBOW = new ColorSpectrum(
            Arrays.asList(

                    new AbstractMap.SimpleEntry<Float, Color>(5f, PURPLE),
                    new AbstractMap.SimpleEntry<Float, Color>(4f, BLUE),
                    new AbstractMap.SimpleEntry<Float, Color>(3f, GREEN),
                    new AbstractMap.SimpleEntry<Float, Color>(4f, YELLOW),
                    new AbstractMap.SimpleEntry<Float, Color>(5f, ORANGE)
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

    private static final ColorSpectrum WXT = new ColorSpectrum(
            Arrays.asList(
                    new AbstractMap.SimpleEntry<Float, Color>(1f, BLUE),
                    new AbstractMap.SimpleEntry<Float, Color>(2f, WHITE),
                    new AbstractMap.SimpleEntry<Float, Color>(4f, ORANGE),
                    new AbstractMap.SimpleEntry<Float, Color>(2f, RED)
            ),
            WHITE
    );

    private static final ColorSpectrum FEVER = new ColorSpectrum(
            Arrays.asList(
                    new AbstractMap.SimpleEntry<Float, Color>(2f, RED),
                    new AbstractMap.SimpleEntry<Float, Color>(6f, PURPLE),
                    new AbstractMap.SimpleEntry<Float, Color>(5f, BLUE),
                    new AbstractMap.SimpleEntry<Float, Color>(2f, GREEN),
                    new AbstractMap.SimpleEntry<Float, Color>(0.5f, YELLOW)
            ),
            WHITE
    );

    private static final List<ColorSpectrum> colorSpectrums = Arrays.asList(
            RAINBOW,
            HOT,
            COOL,
            WXT,
            FEVER
    );

    private int spectrumIndex = 0;

    private static int startSoundballCount = 1200;
    public static final String RAWM_FILENAME = "model/data/input/Carly Rae Jepsen - Run Away With Me.mp3";
    public static final String TYB_FILENAME = "model/data/input/Glassjaw - Tip Your Bartender.mp3";
    public static final String FEVER_FILENAME = "model/data/input/Carly Rae Jepsen - Fever.mp3";
    public static String mp3FileName = RAWM_FILENAME;

    public void applySketchModeSettings(AudioBlurSketch.AudioBlurSketchMode mode){
        switch (mode){
            case REALTIME_MIC:
            case REALTIME_MP3:
                recordingMode = false;
                bloom = false;
                fastBlur = true;
                startSoundballCount = 400;
                SoundBall.maxBottomZ = 30;
                SoundBall.minRadius = 1;
                SoundBall.maxRadius = 12;
                SoundBall.randomRadiusAmount = 1;
                SoundBall.zMultiplier = 2;
                break;
            case RECORD:
                recordingMode = true;
                bloom = true;
                fastBlur = false;
                startSoundballCount = 1500;
                SoundBall.maxBottomZ = 40;
                SoundBall.minRadius = 3;
                SoundBall.maxRadius = 30;
                SoundBall.randomRadiusAmount = 2;
                SoundBall.zMultiplier = 4;
                break;
            case RECORD_TEST:
                recordingMode = false;
                bloom = false;
                fastBlur = true;
                startSoundballCount = 200;
                SoundBall.maxBottomZ = 40;
                SoundBall.minRadius = 3;
                SoundBall.maxRadius = 30;
                SoundBall.randomRadiusAmount = 2;
                SoundBall.zMultiplier = 4;
                motionDrawMode = MovingShape.MotionDrawMode.START;
                break;
        }
    }



    private static final float VOLUME_MULTIPLIER_INC_AMOUNT = 0.1f;
    private static final int SOUNDBALL_NUM_INC_AMOUNT = 20;

    private Set<Character> newKeysPressed, newKeysReleased;
    private Set<Integer> newKeyCodesPressed, newKeyCodesReleased;
    private Minim minim;
    private AudioPlayer player;
    private AudioInput input;
    private AudioSource source;
    private FFT fftCenter, fftLeft, fftRight;
    private FFTHelper fftHelper;
    private Set<SoundBall> soundBalls;
    private ArrayList<SoundBall> soundBallList;

    private ArrayList<FFTSample> recordingSnapshots = new ArrayList<>();
    private int totalSnapshotsRecorded = 0;

    private float volumeMultiplier = 1.6f;
    private float amplitudeCutoff = 0.15f;
    private float primarySoundballGraphicsImageAlpha = 100;
    private float secondarySoundballGraphicsClearAlpha = 5;

    private boolean useVariableAlphaBuffers = false;
    private boolean whiteMode = false;
    private boolean paused = false;
    private boolean fastBlur = false;
    private boolean bloom = true;
    private boolean lowestBandsOnTop = false;

    public boolean recordingMode = true;
    public float recordingFrameRate = 24;
    public boolean recordingFinished = false;
    public boolean snapshotRenderingFinished = false;

    private MovingShape.MotionDrawMode motionDrawMode = MovingShape.MotionDrawMode.BLUR;

    private Comparator<SoundBall> soundBallComparator = new Comparator<SoundBall>() {
        @Override
        public int compare(SoundBall o1, SoundBall o2) {
            if (o1.getRadius() > o2.getRadius()) return 1;
            else if (o1.getRadius() < o2.getRadius()) return -1;
            return 0;
        }
    };

    public AudioBlurSketchDataModel(SketchController controller, Sketch sketch, AudioBlurSketch.AudioBlurSketchMode mode){
        super(controller, sketch);
        newKeysPressed = new HashSet<>();
        newKeysReleased = new HashSet<>();
        newKeyCodesPressed = new HashSet<>();
        newKeyCodesReleased = new HashSet<>();

        applySketchModeSettings(mode);
        minim = new Minim(sketch);

        input = minim.getLineIn();
        player = minim.loadFile(mp3FileName);

        if(mode == AudioBlurSketch.AudioBlurSketchMode.REALTIME_MP3 ||
                mode == AudioBlurSketch.AudioBlurSketchMode.RECORD ||
                mode == AudioBlurSketch.AudioBlurSketchMode.RECORD_TEST ||
                input == null){
            player.play();
            source = player;
        } else {
            input.enableMonitoring();
            input.mute();
            source = input;
        }

        fftCenter = new FFT(source.bufferSize(), source.sampleRate());
        fftLeft = new FFT(source.bufferSize(), source.sampleRate());
        fftRight = new FFT(source.bufferSize(), source.sampleRate());

        fftHelper = new FFTHelper(fftCenter, fftLeft, fftRight, this, colorSpectrums.get(spectrumIndex), .0f, .5f);

        soundBalls = new TreeSet<>(soundBallComparator);
        soundBallList = new ArrayList<>();
        for(int i = 0; i < startSoundballCount; i++){
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
                new AudioBlurDisplayString("fast_blur",
                        new Color(1, 1), 16, this));
        sketch.addDisplayStringToDiagInfoColumn(
                new AudioBlurDisplayString("amplitude_cutoff",
                        new Color(1, 1), 16, this));
    }

    public void playSong(String fileName){
        mp3FileName = fileName;
        if(source == player){
            player.close();
        } else if(source == input && input != null){
            input.disableMonitoring();
        }

        player = minim.loadFile(fileName);
        player.play();
        source = player;

        if(fileName.equals(FEVER_FILENAME)){
            spectrumIndex = 4;
        } else if(fileName.equals(TYB_FILENAME)){
            spectrumIndex = 3;
        } else if(fileName.equals(RAWM_FILENAME)){
            spectrumIndex = 0;
            //lowestBandsOnTop = true;
        }

        fftHelper.setColorSpectrum(colorSpectrums.get(spectrumIndex));
    }

    public boolean isPaused() {
        return paused;
    }

    public boolean isFastBlurEnabled(){
        return fastBlur;
    }

    public boolean isBloomEnabled(){
        return bloom;
    }

    public boolean isWhiteModeActivated() {
        return whiteMode;
    }

    public void toggleWhiteMode(){
        whiteMode = !whiteMode;
    }

    public void toggleSoundballStackingMode(){
        lowestBandsOnTop = !lowestBandsOnTop;

        Set<SoundBall> newSoundBalls;
        if(lowestBandsOnTop) newSoundBalls = new TreeSet<>(soundBallComparator);
        else newSoundBalls = new TreeSet<>(soundBallComparator.reversed());
        newSoundBalls.addAll(soundBalls);
        soundBalls = newSoundBalls;
    }

    public void nextSong(){
        if(source == player){
            if(mp3FileName.equals(RAWM_FILENAME)){
                mp3FileName = TYB_FILENAME;
            } else if(mp3FileName.equals(TYB_FILENAME)){
                mp3FileName = FEVER_FILENAME;
            } else {
                mp3FileName = RAWM_FILENAME;
            }
        }
        playSong(mp3FileName);
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

        if(newKeysPressed.contains('b') || newKeysPressed.contains('B')){
            bloom = !bloom;
        }

        if(newKeysPressed.contains('e') || newKeysPressed.contains('E')){
            for(int i = 0; i < SOUNDBALL_NUM_INC_AMOUNT; i++){
                SoundBall randomSoundBall = SoundBall.generateRandomSoundBall(fftHelper);
                soundBalls.add(randomSoundBall);
                soundBallList.add(randomSoundBall);
            }
        }

        if(newKeysPressed.contains('f') || newKeysPressed.contains('F')){
            fastBlur = !fastBlur;
        }

        if(newKeysPressed.contains('i') || newKeysPressed.contains('I')){
            getSketch().toggleShowDiagInfo();
        }

        if(newKeysPressed.contains('m') || newKeysPressed.contains('M')){
            motionDrawMode = MovingShape.getNextMotionDrawMode(motionDrawMode);
        }

        if(newKeysPressed.contains('n') || newKeysPressed.contains('N')){
            nextSong();
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

        if(newKeysPressed.contains('s') || newKeysPressed.contains('S')){
            toggleSoundballStackingMode();
        }

        if(newKeysPressed.contains('w') || newKeysPressed.contains('W')){
            toggleWhiteMode();
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
            float currFreq = fftHelper.getCurrentSample().getBand(i);
            canvas.rect(rectStart, canvas.height/2 - currFreq * (height/2), rectWidth, currFreq * height);
            rectStart += rectWidth;
        }
    }

    public float getRecordedSnapshotProgress(){
        float snapshotsProcessed = totalSnapshotsRecorded - recordingSnapshots.size();
        return snapshotsProcessed/((float) totalSnapshotsRecorded);
    }

    @Override
    public void applyModelStateToSketch(PGraphics canvas, Color.ColorMode colorMode) {
        if (whiteMode) canvas.background(255, 0);
        else canvas.background(0, 0);

        if(recordingMode){
            if(!recordingFinished){
                if(!((AudioPlayer) source).isPlaying()){
                    recordingFinished = true;
                    getSketch().resetFrameCount();
                } else {
                    fftCenter.forward(source.mix.toArray());
                    fftLeft.forward(source.left.toArray());
                    fftRight.forward(source.right.toArray());
                    fftHelper.update();
                    recordingSnapshots.add(fftHelper.getCurrentSample());
                    totalSnapshotsRecorded++;
                }
            } else {
                if(snapshotRenderingFinished || recordingSnapshots.size() == 0){
                    snapshotRenderingFinished = true;
                    return;
                }
                FFTSample currentSample = recordingSnapshots.remove(0);

                for (SoundBall soundBall : soundBalls) {
                    soundBall.update(currentSample, recordingFrameRate);
                    soundBall.draw(canvas, colorMode, motionDrawMode);
                }
            }
        } else {

            fftCenter.forward(source.mix.toArray());
            fftLeft.forward(source.left.toArray());
            fftRight.forward(source.right.toArray());
            fftHelper.update();
            FFTSample currentSample = fftHelper.getCurrentSample();

            for (SoundBall soundBall : soundBalls) {
                soundBall.update(currentSample, getSketch().frameRate);
                soundBall.draw(canvas, colorMode, motionDrawMode);
            }
        }
    }
}
