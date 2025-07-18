package uk.ac.ed.eci.libCZI;

import java.lang.foreign.MemorySegment;

public record InputStreamResult(int errorCode, MemorySegment stream) {
    
}
