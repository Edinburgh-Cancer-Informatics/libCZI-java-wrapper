package uk.ac.ed.eci.libCZI.bitmaps;

import java.lang.foreign.FunctionDescriptor;
import java.lang.foreign.MemorySegment;
import java.lang.invoke.MethodHandle;

import uk.ac.ed.eci.libCZI.CziBitmapException;
import uk.ac.ed.eci.libCZI.LibCziFFM;

import static java.lang.foreign.ValueLayout.*;

import java.lang.foreign.Arena;

public class Bitmap implements AutoCloseable {

    private final MemorySegment bitmapHandle;

    public Bitmap(MemorySegment bitmapHandle) {
        this.bitmapHandle = bitmapHandle;
    }

    public MemorySegment handle() {
        return bitmapHandle;
    }

    public BitmapLockInfo lock() {
        if (bitmapHandle == null || bitmapHandle.address() == 0) {
            return null;
        }
        FunctionDescriptor descriptor = FunctionDescriptor.of(JAVA_INT, ADDRESS, ADDRESS);
        try (Arena arena = Arena.ofConfined()) {
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
    public void release() {
        if (bitmapHandle == null || bitmapHandle.address() == 0) {
            return;
        }
        FunctionDescriptor descriptor = FunctionDescriptor.ofVoid(ADDRESS);
        try {
            LibCziFFM.getMethodHandle("libCZI_ReleaseBitmap", descriptor).invokeExact(bitmapHandle);
        } catch(Throwable e) {
            throw new RuntimeException("Failed to call native function libCZI_ReleaseBitmap");
        }
    }

    public void unlock() {
        FunctionDescriptor descriptor = FunctionDescriptor.ofVoid(ADDRESS);
        try {
            LibCziFFM.getMethodHandle("libCZI_BitmapUnlock", descriptor).invokeExact(bitmapHandle);
        } catch (Throwable e) {
            throw new RuntimeException("Failed to call native function libCZI_UnlockBitmap", e);
        }
    }

    public BitmapInfo getBitmapInfo() {
        FunctionDescriptor descriptor = FunctionDescriptor.of(JAVA_INT, ADDRESS, ADDRESS);
        try (Arena arena = Arena.ofConfined()) {
            MethodHandle getBitmapInfo = LibCziFFM.getMethodHandle("libCZI_BitmapGetInfo", descriptor);
            MemorySegment pBitmapInfo = arena.allocate(BitmapInfo.layout());
            int errorCode = (int) getBitmapInfo.invokeExact(bitmapHandle, pBitmapInfo);
            if (errorCode != 0) {
                throw new CziBitmapException("Failed to get bitmap info. Error code: " + errorCode);
            }
            return BitmapInfo.createFromMemorySegment(pBitmapInfo);
        } catch (Throwable e) {
            throw new RuntimeException("Failed to call native function libCZI_BitmapGetInfo", e);
        }
    }

    public BitmapData getBitmapData() {
        BitmapInfo info = getBitmapInfo();
        BitmapData data = BitmapData.create(this);
        MemorySegment segment = data.data();

        FunctionDescriptor descriptor = FunctionDescriptor.of(
            JAVA_INT, //Return
            ADDRESS, //Bitmap Handle
            JAVA_INT, //height
            JAVA_INT, //width
            JAVA_INT, //pixel
            JAVA_INT, //stride
            ADDRESS);
        try {                        
            MethodHandle copyTo = LibCziFFM.getMethodHandle("libCZI_BitmapCopyTo", descriptor);
            int errorCode = (int) copyTo.invokeExact(
                bitmapHandle, 
                info.width(), 
                info.height(), 
                info.pixelType().getValue(), 
                data.stride(), 
                segment);
            if (errorCode != 0) {
                throw new CziBitmapException("Failed to get bitmap data. Error code: " + errorCode);
            }

            return data;
        }
        catch(Throwable e) {
            throw new RuntimeException("Failed to call native function libCZI_BitmapGetData", e);
        }
    }

    @Override
    public void close() throws Exception {
        release();
    }
}
