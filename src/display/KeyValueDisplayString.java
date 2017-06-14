package display;

import java.util.Map;

/**
 * Created by psweeney on 3/5/17.
 */
public class KeyValueDisplayString extends DisplayString {
    private String value;

    public KeyValueDisplayString(String key, String value, Color color, float textSize){
        super(key, color, textSize);
        this.value = value;
    }

    @Override
    public String getText() {
        return super.getText() + ": " + value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
