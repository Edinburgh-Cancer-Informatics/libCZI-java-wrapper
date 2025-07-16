package uk.ac.ed.eci.libCZI;

import java.lang.foreign.MemoryLayout;
import java.lang.foreign.MemoryLayout.PathElement;
import java.lang.foreign.MemorySegment;

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

    public static SubBlockStatistics createFromMemorySegment(MemorySegment segment) {
        int subBlockCount = segment.get(JAVA_INT,
                layout().byteOffset(PathElement.groupElement("sub_block_count")));
        int minMIndex = segment.get(JAVA_INT, layout().byteOffset(PathElement.groupElement("min_m_index")));
        int maxMIndex = segment.get(JAVA_INT, layout().byteOffset(PathElement.groupElement("max_m_index")));
        IntRect boundingBox = IntRect.createFromMemorySegment(segment.asSlice(layout().byteOffset(PathElement.groupElement("bounding_box"))));
        IntRect boundingBoxLayer0 = IntRect.createFromMemorySegment(segment.asSlice(layout().byteOffset(PathElement.groupElement("bounding_box_layer0"))));
        DimBounds dimBounds = DimBounds.createFromMemorySegment(segment.asSlice(layout().byteOffset(PathElement.groupElement("dim_bounds"))));
        
        return new SubBlockStatistics(subBlockCount, minMIndex, maxMIndex, boundingBox, boundingBoxLayer0, dimBounds);
    }
}
