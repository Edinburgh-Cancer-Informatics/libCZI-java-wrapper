package uk.ac.ed.eci.libCZI;

import java.lang.foreign.MemoryLayout;

import static java.lang.foreign.ValueLayout.*;

public record DimBounds(int dimensionsValid, int[] start, int[] size) {
    public static MemoryLayout layout() {
        return MemoryLayout.structLayout(
                JAVA_INT.withName("dimensions_valid"),
                MemoryLayout.sequenceLayout(LibCziFFM.K_MAX_DIMENSION_COUNT, JAVA_INT).withName("start"),
                MemoryLayout.sequenceLayout(LibCziFFM.K_MAX_DIMENSION_COUNT, JAVA_INT).withName("size"));
    }
}
