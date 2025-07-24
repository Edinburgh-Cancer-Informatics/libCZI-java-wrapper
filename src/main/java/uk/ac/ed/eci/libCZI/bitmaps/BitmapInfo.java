package uk.ac.ed.eci.libCZI.bitmaps;

import static java.lang.foreign.ValueLayout.JAVA_INT;

import java.lang.foreign.Arena;
import java.lang.foreign.MemoryLayout;
import java.lang.foreign.MemorySegment;

import uk.ac.ed.eci.libCZI.IInterop;
import uk.ac.ed.eci.libCZI.PixelType;

public class BitmapInfo implements IInterop {

    private final int width;
    private final int height;
    private final PixelType pixelType;

    public BitmapInfo(int width, int height, PixelType pixelType) {
        this.width = width;
        this.height = height;
        this.pixelType = pixelType;
    }

    public int width() {
        return width;
    }

    public int height() {
        return height;
    }

    public PixelType pixelType() {
        return pixelType;
    }

    public static MemoryLayout layout() {
        return MemoryLayout.structLayout(
            JAVA_INT.withName("width"),
            JAVA_INT.withName("height"),
            JAVA_INT.withName("pixelType"));
    }

    public static BitmapInfo createFromMemorySegment(MemorySegment pBitmapInfo) {
        return new BitmapInfo(
            pBitmapInfo.get(JAVA_INT, layout().byteOffset(MemoryLayout.PathElement.groupElement("width"))),
            pBitmapInfo.get(JAVA_INT, layout().byteOffset(MemoryLayout.PathElement.groupElement("height"))),
            PixelType.fromValue(pBitmapInfo.get(JAVA_INT, layout().byteOffset(MemoryLayout.PathElement.groupElement("pixelType"))))
        );      
    }

    @Override
    public MemorySegment toMemorySegment(Arena arena) {
        MemorySegment segment = arena.allocate(layout());
        segment.set(JAVA_INT, 0, width);
        segment.set(JAVA_INT, 4, height);
        segment.set(JAVA_INT, 8, pixelType.getValue());
        return segment;
    }

}
