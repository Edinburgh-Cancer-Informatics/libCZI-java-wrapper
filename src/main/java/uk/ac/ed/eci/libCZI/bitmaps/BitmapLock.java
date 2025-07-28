package uk.ac.ed.eci.libCZI.bitmaps;

import static java.lang.foreign.ValueLayout.ADDRESS;
import static java.lang.foreign.ValueLayout.JAVA_INT;

import java.lang.foreign.Arena;
import java.lang.foreign.FunctionDescriptor;
import java.lang.foreign.MemorySegment;
import java.lang.invoke.MethodHandle;

import uk.ac.ed.eci.libCZI.CziBitmapException;
import uk.ac.ed.eci.libCZI.LibCziFFM;

public class BitmapLock implements AutoCloseable {
    private final MemorySegment bitmapHandle;
    private final Arena arena;
    private final IBitmapLockInfo bitmapLockInfo;
    
    public int stride() {
        return bitmapLockInfo.stride();
    }

    public long size() {
        return bitmapLockInfo.size();
    }

    MemorySegment ptrDataRoi() {
        return bitmapLockInfo.ptrDataRoi();
    }

    BitmapLock(MemorySegment bitmapHandle) {
        this.bitmapHandle = bitmapHandle;
        this.arena = Arena.ofConfined();
        this.bitmapLockInfo = lock(bitmapHandle);
        //this.bitmapLockInfo = new BitmapLockFake();
    }

    BitmapLockInfo lock(MemorySegment bitmapHandle) {
        if (bitmapHandle == null || bitmapHandle.address() == 0) {
            return null;
        }
        FunctionDescriptor descriptor = FunctionDescriptor.of(JAVA_INT, ADDRESS, ADDRESS);
        try {
            MemorySegment pBitmapLockInfo = arena.allocate(BitmapLockInfo.layout());
            MethodHandle bitmapLock = LibCziFFM.getMethodHandle("libCZI_BitmapLock", descriptor);
            int errorCode = (int) bitmapLock.invokeExact(bitmapHandle, pBitmapLockInfo);
            if (errorCode != 0) {
                throw new CziBitmapException("Failed to lock bitmap. Error code: " + errorCode);
            }

            return BitmapLockInfo.createFromMemorySegment(pBitmapLockInfo);
        }
        catch(Throwable e) {
            throw new RuntimeException("Failed to call native function libCZI_LockBitmap", e);
        }
    }

    void unlock() {
        FunctionDescriptor descriptor = FunctionDescriptor.ofVoid(ADDRESS);
        try {
            LibCziFFM.getMethodHandle("libCZI_BitmapUnlock", descriptor).invokeExact(bitmapHandle);
        } catch (Throwable e) {
            throw new RuntimeException("Failed to call native function libCZI_UnlockBitmap", e);
        }
    }
    @Override
    public void close() throws Exception {
        unlock();
        arena.close();
    }
    
}
