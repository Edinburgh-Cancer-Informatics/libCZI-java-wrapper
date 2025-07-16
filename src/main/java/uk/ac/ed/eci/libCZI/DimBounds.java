package uk.ac.ed.eci.libCZI;

import java.lang.foreign.MemoryLayout;
import java.lang.foreign.MemoryLayout.PathElement;
import java.lang.foreign.MemorySegment;

import static java.lang.foreign.ValueLayout.*;

public record DimBounds(int dimensionsValid, int[] start, int[] size) {
    public static MemoryLayout layout() {
        return MemoryLayout.structLayout(
                JAVA_INT.withName("dimensions_valid"),
                MemoryLayout.sequenceLayout(LibCziFFM.K_MAX_DIMENSION_COUNT, JAVA_INT).withName("start"),
                MemoryLayout.sequenceLayout(LibCziFFM.K_MAX_DIMENSION_COUNT, JAVA_INT).withName("size"));
    }

    public static DimBounds createFromMemorySegment(MemorySegment segment) {
            int dimensionsValid = segment.get(JAVA_INT,
                    layout().byteOffset(PathElement.groupElement("dimensions_valid")));
            int[] start = segment.asSlice(layout().byteOffset(PathElement.groupElement("start")))
                    .toArray(JAVA_INT);
            int[] size = segment.asSlice(layout().byteOffset(PathElement.groupElement("size")))
                    .toArray(JAVA_INT);
                    
            return new DimBounds(dimensionsValid, start, size);        
    }
}
