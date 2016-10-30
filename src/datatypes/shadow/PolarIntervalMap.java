package datatypes.shadow;

import processing.core.PApplet;

import static processing.core.PConstants.TWO_PI;

/**
 * Created by psweeney on 10/27/16.
 */
public class PolarIntervalMap<E> {
    private float minAngle, maxAngle, midAngle;
    private PolarIntervalMap<E> upperChild, lowerChild;
    private E value;

    public PolarIntervalMap(float minAngle, float maxAngle, float minInterval){
        this.minAngle = minAngle;
        this.maxAngle = maxAngle;
        while(maxAngle < minAngle) maxAngle += TWO_PI;

        midAngle = this.minAngle + (this.maxAngle - this.minAngle)/2;

        if(this.maxAngle - this.minAngle <= minInterval){
            upperChild = null;
            lowerChild = null;
        } else {
            lowerChild = new PolarIntervalMap<E>(this.minAngle, midAngle, minInterval);
            upperChild = new PolarIntervalMap<E>(midAngle, this.maxAngle, minInterval);
        }
    }

    public void put(float angle, E value){
        if(angle % TWO_PI < minAngle || angle % TWO_PI >= maxAngle) return;
        if(lowerChild == null && upperChild == null){
            this.value = value;
            return;
        }

        if(angle % TWO_PI < midAngle) lowerChild.put(angle % TWO_PI, value);
        else upperChild.put(angle % TWO_PI, value);
    }

    public E getValueForAngle(float angle){
        if(lowerChild == null && upperChild == null) return value;

        if(angle > maxAngle || angle % TWO_PI < midAngle) return lowerChild.getValueForAngle(angle % TWO_PI);
        return upperChild.getValueForAngle(angle % TWO_PI);
    }
}
