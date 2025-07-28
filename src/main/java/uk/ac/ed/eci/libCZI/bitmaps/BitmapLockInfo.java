package uk.ac.ed.eci.libCZI.bitmaps;

import static java.lang.foreign.ValueLayout.*;

import java.lang.foreign.Arena;
import java.lang.foreign.MemoryLayout;
import java.lang.foreign.MemorySegment;

import uk.ac.ed.eci.libCZI.IInterop;

public class BitmapLockInfo implements IInterop, IBitmapLockInfo {
    private final MemorySegment ptrData;
    private final MemorySegment ptrDataRoi;
    private final int stride;
    private final int size;

    public static MemoryLayout layout() {
        return MemoryLayout.structLayout(
            ADDRESS.withName("ptrData"), // Not used
            ADDRESS.withName("ptrDataRoi"),
            JAVA_INT.withName("stride"),
            // Align with padding if necessary (often is on 64-bit systems)
            MemoryLayout.paddingLayout(4),                  
            JAVA_LONG.withName("size"));
    }

    public static BitmapLockInfo createFromMemorySegment(MemorySegment pBitmapLockInfo) {
        return new BitmapLockInfo(
            pBitmapLockInfo.get(ADDRESS, layout().byteOffset(MemoryLayout.PathElement.groupElement("ptrData"))),
            pBitmapLockInfo.get(ADDRESS, layout().byteOffset(MemoryLayout.PathElement.groupElement("ptrDataRoi"))),
            pBitmapLockInfo.get(JAVA_INT, layout().byteOffset(MemoryLayout.PathElement.groupElement("stride"))),
            pBitmapLockInfo.get(JAVA_INT, layout().byteOffset(MemoryLayout.PathElement.groupElement("size")))
        );
    }

    public BitmapLockInfo(MemorySegment ptrData, MemorySegment ptrDataRoi, int stride, int size) {
        this.ptrData = ptrData;
        this.ptrDataRoi = ptrDataRoi.reinterpret(size).asReadOnly();
        this.stride = stride;
        this.size = size;
    }

    public int stride() {
        return stride;
    }

    public int size() {
        return size;
    }

    public MemorySegment ptrDataRoi() {
        return ptrDataRoi;
    }


    @Override
    public MemorySegment toMemorySegment(Arena arena) {
        MemorySegment segment = arena.allocate(layout());
        segment.set(ADDRESS, 0, MemorySegment.NULL);
        segment.set(ADDRESS, 4, MemorySegment.NULL);
        segment.set(JAVA_INT, 8, stride);
        segment.set(JAVA_INT, 12, size);
        return segment;
    }
    
}
