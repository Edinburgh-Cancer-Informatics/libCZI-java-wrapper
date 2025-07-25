package uk.ac.ed.eci.libCZI.bitmaps;

import java.lang.foreign.MemorySegment;

public interface IBitmapLockInfo {
    int stride();
    int size();
    MemorySegment ptrDataRoi();
}
