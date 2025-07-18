package uk.ac.ed.eci.libCZI;

import java.lang.foreign.MemoryLayout;
import java.lang.foreign.MemoryLayout.PathElement;
import java.lang.foreign.MemorySegment;

import static java.lang.foreign.ValueLayout.*;
/**
 * Represents the statistics of sub-blocks within a CZI image.
 * This record corresponds to the `SubBlockStatistics` structure in the libCZI C API.
 * It provides various statistical information about the sub-blocks, including counts,
 * M-index ranges, bounding boxes, and dimension bounds.
 *
 * @param subBlockCount The total number of sub-blocks.
 * @param minMIndex The minimum M-index encountered in the sub-blocks.
 * @param maxMIndex The maximum M-index encountered in the sub-blocks.
 * @param boundingBox The bounding box (IntRect) that encloses all sub-blocks.
 * @param boundingBoxLayer0 The bounding box (IntRect) that encloses sub-blocks at layer 0.
 * @param dimBounds The dimension bounds (DimBounds) for the sub-blocks.
 * @see <a href="https://zeiss.github.io/libczi/lib/structlib_c_z_i_1_1_sub_block_statistics.html">SubBlockStatistics</a>
 * @author Paul Mitchell
 */
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
