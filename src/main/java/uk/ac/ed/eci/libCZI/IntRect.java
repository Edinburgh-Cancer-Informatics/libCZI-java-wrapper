package uk.ac.ed.eci.libCZI;

import java.lang.foreign.MemoryLayout;

import static java.lang.foreign.ValueLayout.*;

public record IntRect(int x, int y, int w, int h) {
    public static MemoryLayout layout() {
            return MemoryLayout.structLayout(
                    JAVA_INT.withName("x"),
                    JAVA_INT.withName("y"),
                    JAVA_INT.withName("w"),
                    JAVA_INT.withName("h"));
        }    
}
