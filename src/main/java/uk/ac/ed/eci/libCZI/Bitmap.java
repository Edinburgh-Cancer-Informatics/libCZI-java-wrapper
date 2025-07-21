package uk.ac.ed.eci.libCZI;

import java.lang.foreign.FunctionDescriptor;
import java.lang.foreign.MemorySegment;

import static java.lang.foreign.ValueLayout.*;

public class Bitmap implements AutoCloseable {

    private MemorySegment data;
    private IntSize size;

    public Bitmap(MemorySegment data, IntSize size) {
        this.data = data;
        this.size = size;
    }

    public MemorySegment data() {
        return data;
    }

    public IntSize size() {
        return size;
    }

    public void releaseBitmap() {
        if (data == null || data.address() == 0) {
            return;
        }
        FunctionDescriptor descriptor = FunctionDescriptor.ofVoid(ADDRESS);
        try {
            LibCziFFM.getMethodHandle("libCZI_ReleaseBitmap", descriptor).invokeExact(data);
        } catch(Throwable e) {
            throw new RuntimeException("Failed to call native function libCZI_ReleaseBitmap");
        }
    }

    public void unlockBitmap() {
        FunctionDescriptor descriptor = FunctionDescriptor.ofVoid(ADDRESS);
        try {
            LibCziFFM.getMethodHandle("libCZI_UnlockBitmap", descriptor).invokeExact(data);
        } catch (Throwable e) {
            throw new RuntimeException("Failed to call native function libCZI_UnlockBitmap", e);
        }
    }

    @Override
    public void close() throws Exception {
        unlockBitmap();
        releaseBitmap();
    }
}
