package datatypes.pixel;

import processing.core.PApplet;

/**
 * Created by psweeney on 9/20/16.
 */
public class Color {
    public enum BLEND_MODE{
        NORMAL, ADD
    }

    private float r, g, b, a;

    public Color(int r, int g, int b, int a){
        this.r = PApplet.min(255, PApplet.max(0, r));
        this.g = PApplet.min(255, PApplet.max(0, g));;
        this.b = PApplet.min(255, PApplet.max(0, b));;
        this.a = PApplet.min(255, PApplet.max(0, a));;
    }

    public Color(int r, int g, int b){
        this(r, g, b, 255);
    }

    public Color(int gray, int a){
        this(gray, gray, gray, a);
    }

    public Color(int gray){
        this(gray, 255);
    }

    public Color(float r, float g, float b, float a){
        this((int) r, (int) g, (int) b, (int) a);
    }

    public Color(float r, float g, float b){
        this(r, g, b, 255);
    }

    public Color(float gray, float a){
        this(gray, gray, gray, a);
    }

    public Color(float gray){
        this(gray, 255);
    }

    public float getR() {
        return r;
    }

    public float getG() {
        return g;
    }

    public float getB() {
        return b;
    }

    public float getA() {
        return a;
    }

    public Color add(Color newColor, BLEND_MODE mode){
        if(newColor == null || newColor.getA() <= 0)
            return new Color(r, g, b, a);

        float cr = r, cg = g, cb = b, ca = a, nr = newColor.r, ng = newColor.g, nb = newColor.b, na = newColor.a;
        float naRatio = PApplet.min(1, PApplet.max(0, na/255f));
        float nTotal = (nr * naRatio + ng * naRatio + nb * naRatio);


        float sr = cr, sg = cg, sb = cb, sa = ca + nTotal * naRatio;

        switch (mode){
            case ADD:
                sr += naRatio * nr;
                sg += naRatio * ng;
                sb += naRatio * nb;
                break;
            default:
                sr = (1 - naRatio) * cr + naRatio * nr;
                sg = (1 - naRatio) * cg + naRatio * ng;
                sb = (1 - naRatio) * cb + naRatio * nb;
                break;
        }

        return new Color(sr, sg, sb, sa);
    }

    public Color flatten(){
        float alphaRatio = a/255;
        return new Color(r * alphaRatio, g * alphaRatio, b * alphaRatio, 255);
    }
}
