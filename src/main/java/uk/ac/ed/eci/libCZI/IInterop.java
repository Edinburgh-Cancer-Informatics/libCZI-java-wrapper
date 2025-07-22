package uk.ac.ed.eci.libCZI;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;

public interface IInterop {
    MemorySegment toMemorySegment(Arena arena);
}
