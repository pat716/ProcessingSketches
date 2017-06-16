package special.audioblur;

import display.Color;
import display.DisplayString;
import model.AudioBlurSketchDataModel;

/**
 * Created by psweeney on 6/14/17.
 */
public class AudioBlurDisplayString extends DisplayString {
    private static final int FLOAT_DISPLAY_DECIMAL_PLACE = 2;
    private String fieldName;
    private AudioBlurSketchDataModel dataModel;

    public AudioBlurDisplayString(String fieldName, Color color, float textSize, AudioBlurSketchDataModel dataModel){
        super(null, color, textSize);
        this.fieldName = fieldName;
        this.dataModel = dataModel;
    }

    @Override
    public String getText() {
        String retString = "";
        switch (fieldName){
            case "volume_multiplier":
                retString = "Volume input multiplier: " + DisplayString.getFloatStringForDecimalPlace(
                        dataModel.getVolumeMultiplier(), FLOAT_DISPLAY_DECIMAL_PLACE);
                break;
            case "num_soundballs":
                retString = "Number of circles: " + dataModel.getNumSoundballs();
                break;
            case "variable_alpha":
                retString = "Variable alpha screen clearing: ";
                if(dataModel.useVariableAlphaBuffers()) retString += "ON";
                else retString += "OFF";
                break;
            case "fast_blur":
                retString = "Fast blur rendering: ";
                if(dataModel.isFastBlurEnabled()) retString += "ON";
                else retString += "OFF";
                break;
            case "motion_draw_mode":
                retString = "Motion draw mode: " + dataModel.getMotionDrawMode().toString();
                break;
            case "amplitude_cutoff":
                retString = "Amplitude cutoff: " +
                        DisplayString.getFloatStringForDecimalPlace(dataModel.getAmplitudeCutoff(), FLOAT_DISPLAY_DECIMAL_PLACE);
                break;
        }
        return retString;
    }
}
