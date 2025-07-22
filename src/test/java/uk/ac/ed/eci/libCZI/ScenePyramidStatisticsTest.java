package uk.ac.ed.eci.libCZI;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.Test;

public class ScenePyramidStatisticsTest {
    @Test
    public void testFromJson() {
        ScenePyramidStatistics stats = ScenePyramidStatistics.fromJson(jsonTestData());        
        assertEquals(2, stats.getStatsByScene().size());

        List<PyramidLayerStats> stats0 = stats.getStatsByScene().get("0");
        PyramidLayerStats stats00 = stats0.get(0);
        assertEquals(0, stats00.getLayerInfo().getMinificationFactor());
        assertEquals(0, stats00.getLayerInfo().getPyramidLayerNo());
        assertEquals(27, stats00.getCount());
    }

    private String jsonTestData() {
        return """
{"scenePyramidStatistics":{"0":[{"layerInfo":{"minificationFactor":0,"pyramidLayerNo":0},"count":27},{"layerInfo":{"minificationFactor":2,"pyramidLayerNo":1},"count":34},{"layerInfo":{"minificationFactor":2,"pyramidLayerNo":2},"count":9},{"layerInfo":{"minificationFactor":2,"pyramidLayerNo":3},"count":4},{"layerInfo":{"minificationFactor":2,"pyramidLayerNo":4},"count":1}],"1":[{"layerInfo":{"minificationFactor":0,"pyramidLayerNo":0},"count":27},{"layerInfo":{"minificationFactor":2,"pyramidLayerNo":1},"count":34},{"layerInfo":{"minificationFactor":2,"pyramidLayerNo":2},"count":9},{"layerInfo":{"minificationFactor":2,"pyramidLayerNo":3},"count":4},{"layerInfo":{"minificationFactor":2,"pyramidLayerNo":4},"count":1}]}}                
                """;
    }
}
