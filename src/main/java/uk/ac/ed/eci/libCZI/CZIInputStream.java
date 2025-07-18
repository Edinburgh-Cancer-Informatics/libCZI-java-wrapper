package uk.ac.ed.eci.libCZI;

import java.lang.foreign.FunctionDescriptor;
import java.lang.foreign.Linker;
import java.lang.foreign.MemorySegment;
import java.lang.invoke.MethodHandle;

import static java.lang.foreign.ValueLayout.*;

import java.lang.foreign.Arena;

public class CZIInputStream implements AutoCloseable {

    private LibCziFFM.InputStreamResult streamResult;
    protected CZIInputStream(LibCziFFM.InputStreamResult streamResult) {
        this.streamResult = streamResult;
    }

    public static CZIInputStream createInputStreamFromFileUTF8(String string) {
        FunctionDescriptor descriptor = FunctionDescriptor.of(JAVA_INT, ADDRESS, ADDRESS);
                MethodHandle createInputStream = Linker.nativeLinker()
                .downcallHandle(
                        LibCziFFM.SYMBOL_LOOKUP.find("libCZI_CreateInputStreamFromFileUTF8").orElseThrow(
                                () -> new UnsatisfiedLinkError(
                                        "Could not find symbol: libCZI_CreateInputStreamFromFileUTF8")),
                        descriptor);
        try (Arena arena = Arena.ofConfined()) {
            MemorySegment filenameSegment = arena.allocateFrom(string);
            MemorySegment pStream = arena.allocate(ADDRESS);
            int errorCode = (int) createInputStream.invokeExact(filenameSegment, pStream);
            if (errorCode != 0) {
                return new CZIInputStream(new LibCziFFM.InputStreamResult(errorCode, MemorySegment.NULL));
            }
            MemorySegment streamHandle = pStream.get(ADDRESS, 0);   
            return new CZIInputStream(new LibCziFFM.InputStreamResult(errorCode, streamHandle));
        } catch (Throwable e) {
            throw new RuntimeException("Failed to call native function libCZI_CreateInputStreamFromFileUTF8", e);
        }
    }

    public Integer errorCode() {
        return streamResult.errorCode();
    }

    public MemorySegment stream() {
        return streamResult.stream();
    }

    @Override
    public void close() throws Exception {
        LibCziFFM.free(streamResult.stream());
    }
    
}
