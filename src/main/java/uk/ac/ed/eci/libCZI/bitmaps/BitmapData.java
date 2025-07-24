package uk.ac.ed.eci.libCZI.bitmaps;

import static java.lang.foreign.ValueLayout.JAVA_BYTE;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;

public class BitmapData implements AutoCloseable {

    private final MemorySegment data;
    private final BitmapInfo info;
    private final Arena arena;

    private BitmapData(BitmapInfo info) {        
        this.info = info;
        this.arena = Arena.ofConfined();
        this.data = arena.allocate(info.width() * info.height() * pixelSize());
    }

    private int pixelSize() {
        switch (info.pixelType()) {
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
                throw new UnsupportedOperationException("Unsupported pixel type: " + info.pixelType());                
        }
    }

    static BitmapData create(BitmapInfo info) {
        return new BitmapData(info);
    }

    MemorySegment data() {
        return data;
    }

    public BitmapInfo info() {
        return info;
    }

    public byte[] getBytes() {
        return data.toArray(JAVA_BYTE);
    }

    @Override
    public void close() throws Exception {
        arena.close();
    }
}