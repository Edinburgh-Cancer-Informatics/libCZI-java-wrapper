package uk.ac.ed.eci.libCZI;

import java.lang.foreign.MemoryLayout;
import java.lang.foreign.MemoryLayout.PathElement;
import java.lang.foreign.MemorySegment;

import static java.lang.foreign.ValueLayout.*;
/**
 * Represents a rectangle defined by integer coordinates and dimensions.
 * This record corresponds to the `IntRect` structure in the libCZI C API.
 * It is typically used to define bounding boxes or regions of interest.
 *
 * @param x The x-coordinate of the top-left corner of the rectangle.
 * @param y The y-coordinate of the top-left corner of the rectangle.
 * @param w The width of the rectangle.
 * @param h The height of the rectangle.
 * @see <a href="https://zeiss.github.io/libczi/lib/structlib_c_z_i_1_1_int_rect.html">IntRect</a>
 * @author Paul Mitchell
 */
public record IntRect(int x, int y, int w, int h) {
    public static MemoryLayout layout() {
            return MemoryLayout.structLayout(
                    JAVA_INT.withName("x"),
                    JAVA_INT.withName("y"),
                    JAVA_INT.withName("w"),
                    JAVA_INT.withName("h"));
        }
    public static IntRect createFromMemorySegment(MemorySegment segment)  {
        return new IntRect(
                    segment.get(JAVA_INT, layout().byteOffset(PathElement.groupElement("x"))),
                    segment.get(JAVA_INT, layout().byteOffset(PathElement.groupElement("y"))),
                    segment.get(JAVA_INT, layout().byteOffset(PathElement.groupElement("w"))),
                    

segment.get(JAVA_INT, layout().byteOffset(PathElement.groupElement("h"))));
    }
}
