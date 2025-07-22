package uk.ac.ed.eci.libCZI;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.Map;

public class ScenePyramidStatistics {

    @JsonProperty("scenePyramidStatistics")
    private Map<String, List<PyramidLayerStats>> statsByScene;

    // Getters and Setters
    public Map<String, List<PyramidLayerStats>> getStatsByScene() {
        return statsByScene;
    }

    public void setStatsByScene(Map<String, List<PyramidLayerStats>> statsByScene) {
        this.statsByScene = statsByScene;
    }

    @Override
    public String toString() {
        return "ScenePyramidStatistics{" + "statsByScene=" + statsByScene + '}';
    }

    public static ScenePyramidStatistics fromJson(String jsonData) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.readValue(jsonData, ScenePyramidStatistics.class);
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse JSON", e);
        }
    }
}
