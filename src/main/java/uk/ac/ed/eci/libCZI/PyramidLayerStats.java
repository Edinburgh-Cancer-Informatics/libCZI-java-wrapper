package uk.ac.ed.eci.libCZI;

import com.fasterxml.jackson.annotation.JsonProperty;

public class PyramidLayerStats {

    @JsonProperty("layerInfo")
    private LayerInfo layerInfo;

    @JsonProperty("count")
    private int count;

    // Getters and Setters
    public LayerInfo getLayerInfo() {
        return layerInfo;
    }

    public void setLayerInfo(LayerInfo layerInfo) {
        this.layerInfo = layerInfo;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    @Override
    public String toString() {
        return "\n  PyramidLayerStats{" + "layerInfo=" + layerInfo + ", count=" + count + '}';
    }
}
