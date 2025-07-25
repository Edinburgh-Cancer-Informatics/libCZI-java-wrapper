package uk.ac.ed.eci.libCZI.bitmaps;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;

public class BitmapLockFake implements IBitmapLockInfo {

    @Override
    public int stride() {
        return 4;
    }

    @Override
    public int size() {
        return 16;
    }

    @Override
    public MemorySegment ptrDataRoi() {
        return Arena.global().allocate(size()).fill((byte) 128);
    }
    
}
