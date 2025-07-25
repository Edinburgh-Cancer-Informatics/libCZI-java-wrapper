package uk.ac.ed.eci.libCZI.bitmaps;


import static java.lang.foreign.ValueLayout.JAVA_BYTE;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;

import uk.ac.ed.eci.libCZI.PixelType;

public class BitmapData implements AutoCloseable {

    private final MemorySegment data;
    private final Arena arena;
    private final int stride;
    private final int size;
    private final PixelType pixelType;

    BitmapData(BitmapInfo bitmapInfo, BitmapLock lock) {        
        this.arena = Arena.ofConfined();
        this.stride = lock.stride();
        this.size = lock.size();
        this.pixelType = bitmapInfo.pixelType();
        this.data = arena.allocate(this.size); 
        MemorySegment.copy(lock.ptrDataRoi(), 0, this.data, 0, this.size);       
    }

    public int pixelSize() {
        switch (pixelType) {
            case Gray8:
                return 1;
            case Gray16:
                return 2;
            case Gray32Float:
                return 4;
            case Bgr24:
                return 3;
            case Bgr48:
                return 4;
            case Bgr96Float:
                return 8;
            case Bgra32:
                return 4;
            case Gray64ComplexFloat:
                return 8;
            case Bgr192ComplexFloat:
                return 16;
            case Gray32:
                return 4;
            case Gray64Float:
                return 8;
            default:
                throw new UnsupportedOperationException("Unsupported pixel type: " + pixelType);                
        }
    }


    public int stride() {
        return stride;
    }

    public int size() {
        return size;
    }

    public byte[] getBytes() {
        return data.toArray(JAVA_BYTE);
    }

    @Override
    public void close() throws Exception {
        arena.close();
    }
}