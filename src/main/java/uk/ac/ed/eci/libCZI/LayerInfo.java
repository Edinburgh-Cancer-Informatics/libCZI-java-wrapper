package uk.ac.ed.eci.libCZI;

import com.fasterxml.jackson.annotation.JsonProperty;

public class LayerInfo {

    @JsonProperty("minificationFactor")
    private int minificationFactor;

    @JsonProperty("pyramidLayerNo")
    private int pyramidLayerNo;

    // Getters and Setters
    public int getMinificationFactor() {
        return minificationFactor;
    }

    public void setMinificationFactor(int minificationFactor) {
        this.minificationFactor = minificationFactor;
    }

    public int getPyramidLayerNo() {
        return pyramidLayerNo;
    }

    public void setPyramidLayerNo(int pyramidLayerNo) {
        this.pyramidLayerNo = pyramidLayerNo;
    }

    @Override
    public String toString() {
        return "LayerInfo{" + "minificationFactor=" + minificationFactor + ", pyramidLayerNo=" + pyramidLayerNo + '}';
    }
}
