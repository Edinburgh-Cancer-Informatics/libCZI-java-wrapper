package uk.ac.ed.eci.libCZI;

import java.lang.foreign.MemoryLayout;
import java.lang.foreign.MemoryLayout.PathElement;
import java.lang.foreign.MemorySegment;

import static java.lang.foreign.ValueLayout.*;

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
