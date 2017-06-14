package display;

import processing.core.PApplet;

import java.util.*;

/**
 * Created by psweeney on 6/13/17.
 */
public class ColorSpectrum {
    private SortedMap<Float, Color> spectrumMap;
    private Color finalColor;
    private float totalWeight = 0;

    public ColorSpectrum(List<Map.Entry<Float, Color>> weightedEntries, Color finalColor){
        this.finalColor = finalColor;
        spectrumMap = new TreeMap<>();
        for (Map.Entry<Float, Color> entry : weightedEntries){
            spectrumMap.put(totalWeight, entry.getValue());
            totalWeight += entry.getKey();
        }
    }

    public Color getColorForWeight(float weight){
        if(weight <= spectrumMap.firstKey()) return spectrumMap.get(spectrumMap.firstKey());
        else if(weight >= totalWeight) return finalColor;

        float bottomWeight = spectrumMap.firstKey(), topWeight = bottomWeight;
        Color bottomColor = spectrumMap.get(bottomWeight), topColor = null;
        for(Float cutoff : spectrumMap.keySet()){
            Color currColor = spectrumMap.get(cutoff);
            if(cutoff == weight) return currColor;
            else if(cutoff < weight){
                bottomWeight = cutoff;
                bottomColor = currColor;
            } else {
                topWeight = cutoff;
                topColor = currColor;
                break;
            }
        }

        if (topColor == null){
            bottomWeight = spectrumMap.lastKey();
            bottomColor = spectrumMap.get(bottomWeight);
            topColor = finalColor;
            topWeight = totalWeight;
        }

        float ratio = 1 - PApplet.max(0, PApplet.min(1, (topWeight - weight)/(topWeight - bottomWeight)));
        return bottomColor.combine(topColor, ratio);
    }

    public Color getColorForFraction(float normalizedWeight){
        return getColorForWeight(PApplet.max(0, PApplet.min(1, normalizedWeight)) * totalWeight);
    }
}
