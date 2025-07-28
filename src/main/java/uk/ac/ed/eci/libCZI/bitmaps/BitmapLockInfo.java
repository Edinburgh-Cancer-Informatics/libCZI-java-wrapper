package uk.ac.ed.eci.libCZI.bitmaps;

import static java.lang.foreign.ValueLayout.*;

import java.lang.foreign.Arena;
import java.lang.foreign.MemoryLayout;
import java.lang.foreign.MemorySegment;
import java.lang.invoke.VarHandle;

import uk.ac.ed.eci.libCZI.IInterop;

public class BitmapLockInfo implements IInterop, IBitmapLockInfo {
    private final MemorySegment ptrData;
    private final MemorySegment ptrDataRoi;
    private final int stride;
    private final long size;

    // Define the layout as a static final field
    private static final MemoryLayout LAYOUT = MemoryLayout.structLayout(
        ADDRESS.withName("ptrData").withByteAlignment(4),
        ADDRESS.withName("ptrDataRoi").withByteAlignment(4),
        JAVA_INT.withName("stride"), // Natural alignment is already 4
        JAVA_LONG.withName("size").withByteAlignment(4));

    // Create VarHandles for each field from the layout. This is the key to the fix.
    private static final VarHandle PTR_DATA_HANDLE = LAYOUT.varHandle(MemoryLayout.PathElement.groupElement("ptrData"));
    private static final VarHandle PTR_DATA_ROI_HANDLE = LAYOUT.varHandle(MemoryLayout.PathElement.groupElement("ptrDataRoi"));
    private static final VarHandle STRIDE_HANDLE = LAYOUT.varHandle(MemoryLayout.PathElement.groupElement("stride"));
    private static final VarHandle SIZE_HANDLE = LAYOUT.varHandle(MemoryLayout.PathElement.groupElement("size"));

    public static MemoryLayout layout() {
        return LAYOUT;
    }

    public static BitmapLockInfo createFromMemorySegment(MemorySegment pBitmapLockInfo) {
        // Use the VarHandles to get the data. This correctly handles the unaligned access.
        return new BitmapLockInfo(
            (MemorySegment) PTR_DATA_HANDLE.get(pBitmapLockInfo, 0L),
            (MemorySegment) PTR_DATA_ROI_HANDLE.get(pBitmapLockInfo, 0L),
            (int) STRIDE_HANDLE.get(pBitmapLockInfo, 0L),
            (long) SIZE_HANDLE.get(pBitmapLockInfo, 0L)
        );
    }

    public BitmapLockInfo(MemorySegment ptrData, MemorySegment ptrDataRoi, int stride, long size) {
        this.ptrData = ptrData;
        this.ptrDataRoi = ptrDataRoi.reinterpret(size).asReadOnly();
        this.stride = stride;
        this.size = size;
    }

    public int stride() {
        return stride;
    }

    public long size() {
        return size;
    }

    public MemorySegment ptrDataRoi() {
        return ptrDataRoi;
    }

    @Override
    public MemorySegment toMemorySegment(Arena arena) {
        MemorySegment segment = arena.allocate(LAYOUT);
        // Use the VarHandles to set the data, ensuring correct alignment handling.
        PTR_DATA_HANDLE.set(segment, 0L, ptrData);
        PTR_DATA_ROI_HANDLE.set(segment, 0L, ptrDataRoi);
        STRIDE_HANDLE.set(segment, 0L, stride);
        SIZE_HANDLE.set(segment, 0L, size);
        return segment;
    }
}
