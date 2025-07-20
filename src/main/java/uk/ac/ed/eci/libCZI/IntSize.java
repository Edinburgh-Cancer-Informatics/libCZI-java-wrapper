package uk.ac.ed.eci.libCZI;

import java.lang.foreign.MemoryLayout;

import static java.lang.foreign.ValueLayout.*;

/**
 * Represents a size defined by integer width and height.
 * This record corresponds to the `IntSize` structure in the libCZI C API.
 * It is typically used to define dimensions of images or regions.
 *
 * @param w The width.
 * @param h The height.
 * @see <a href="https://zeiss.github.io/libczi/lib/structlib_c_z_i_1_1_int_size.html">IntSize</a>
 * @author Paul Mitchell
 */

public record IntSize(int w, int h) {
        public static MemoryLayout layout() {
            return MemoryLayout.structLayout(
                    JAVA_INT.withName("w"),
                    JAVA_INT.withName("h"));
        }    
}
