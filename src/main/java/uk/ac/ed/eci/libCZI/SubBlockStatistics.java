package uk.ac.ed.eci.libCZI;

import java.lang.foreign.MemoryLayout;

import static java.lang.foreign.ValueLayout.*;

public record SubBlockStatistics(
    int subBlockCount,
    int minMIndex,
    int maxMIndex,
    IntRect boundingBox,
    IntRect boundingBoxLayer0,
    DimBounds dimBounds) {
    
    public static MemoryLayout layout() {
        return MemoryLayout.structLayout(
                JAVA_INT.withName("sub_block_count"),
                JAVA_INT.withName("min_m_index"),
                JAVA_INT.withName("max_m_index"),
                IntRect.layout().withName("bounding_box"),
                IntRect.layout().withName("bounding_box_layer0"),
                DimBounds.layout().withName("dim_bounds"));
    }
}
