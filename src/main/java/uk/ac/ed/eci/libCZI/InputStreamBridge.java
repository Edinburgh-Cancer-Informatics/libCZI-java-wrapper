package uk.ac.ed.eci.libCZI;

import java.io.IOException;
import java.lang.foreign.Arena;
import java.lang.foreign.FunctionDescriptor;
import java.lang.foreign.Linker;
import java.lang.foreign.MemoryLayout;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;

public class InputStreamBridge implements AutoCloseable {
    private final SeekableByteChannel channel;
    private final Arena externalStreamArena;


    public InputStreamBridge(SeekableByteChannel channel) {
        this.channel = channel;
        this.externalStreamArena = Arena.ofConfined();
    }

    public int readFunctionImplementation(long opaque_handle1, long opaque_handle2, long offset, 
                                            MemorySegment pv, long size, MemorySegment ptrBytesRead, 
                                            MemorySegment error_info) {
        
        
        try {
            MemorySegment correctlySizedSegment = pv.reinterpret(size);
            channel.position((int) offset);
            ByteBuffer bufferToRead = correctlySizedSegment.asByteBuffer().slice(0, (int) size);            
            int bytesRead = channel.read(bufferToRead);
            if (bytesRead == -1) {
                bytesRead = 0;
            }
            ptrBytesRead.set(ValueLayout.JAVA_LONG, 0, (long) bytesRead);
            return 0;
        } catch (IOException e) {
            return -1;
        } catch (IndexOutOfBoundsException e) {
            return -1;
        }
    }

    public void closeFunctionImplementation(long opaque_handle1, long opaque_handle2) throws IOException {
        channel.close();
    }

    public MemorySegment createExternalInputStreamStruct(long opaque_handle1, long opaque_handle2) {
        MemorySegment segment = externalStreamArena.allocate(externalInputStreamStructInteropLayout());
        segment.set(ValueLayout.JAVA_LONG, 0, opaque_handle1);
        segment.set(ValueLayout.JAVA_LONG, 8, opaque_handle2);
        try {
            MethodHandle readCallbackHandle = MethodHandles.lookup().findVirtual(this.getClass(), "readFunctionImplementation", readFunctionDescriptor().toMethodType())
                .bindTo(this);
            MemorySegment readCallbackHandleSegment = Linker.nativeLinker().upcallStub(readCallbackHandle, readFunctionDescriptor(), externalStreamArena);
            segment.set(ValueLayout.ADDRESS, 16, readCallbackHandleSegment);
            MethodHandle closeCallbackHandle = MethodHandles.lookup().findVirtual(this.getClass(), "closeFunctionImplementation", closeFunctionDescriptor().toMethodType())
                .bindTo(this);
            MemorySegment closeCallbackHandleSegment = Linker.nativeLinker().upcallStub(closeCallbackHandle, closeFunctionDescriptor(), externalStreamArena);
            segment.set(ValueLayout.ADDRESS, 24, closeCallbackHandleSegment);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }

        return segment;
    }

    public static MemoryLayout externalInputStreamStructInteropLayout() {
        return MemoryLayout.structLayout(
            ValueLayout.ADDRESS.withName("opaque_handle1"),
            ValueLayout.ADDRESS.withName("opaque_handle2"),
            ValueLayout.ADDRESS.withName("read_function"),
            ValueLayout.ADDRESS.withName("close_function")
        );
    }
    public static FunctionDescriptor readFunctionDescriptor() {
        return FunctionDescriptor.of(
            ValueLayout.JAVA_INT,       // int
            ValueLayout.JAVA_LONG,      // uintptr_t opaque_handle1
            ValueLayout.JAVA_LONG,      // uintptr_t opaque_handle2
            ValueLayout.JAVA_LONG,      // long offset
            ValueLayout.ADDRESS,        // MemorySegment pv
            ValueLayout.JAVA_LONG,      // long size
            ValueLayout.ADDRESS.withTargetLayout(ValueLayout.JAVA_LONG),        // MemorySegment ptrBytesRead
            ValueLayout.ADDRESS         // MemorySegment error_info
        );
    }

    public static FunctionDescriptor closeFunctionDescriptor() {
        return FunctionDescriptor.ofVoid(
            ValueLayout.JAVA_LONG,    // uintptr_t opaque_handle1
            ValueLayout.JAVA_LONG     // uintptr_t opaque_handle2   
        );
    }

    @Override
    public void close() throws Exception {
        this.externalStreamArena.close();
    }
}
