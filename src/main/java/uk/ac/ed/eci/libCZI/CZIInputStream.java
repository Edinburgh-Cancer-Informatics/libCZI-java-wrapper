package uk.ac.ed.eci.libCZI;

import java.lang.foreign.FunctionDescriptor;
import java.lang.foreign.Linker;
import java.lang.foreign.MemorySegment;
import java.lang.invoke.MethodHandle;

import static java.lang.foreign.ValueLayout.*;

import java.lang.foreign.Arena;

/**
 * Represents an input stream for CZI files, acting as a wrapper around the
 * native libCZI stream object.
 * This class is responsible for managing the lifecycle of the native stream,
 * including its creation
 * and proper deallocation.
 * <p>
 * Instances of this class are typically created via factory methods like
 * {@link #createInputStreamFromFileUTF8(String)}.
 * It implements {@link AutoCloseable} to ensure native resources are released
 * when the stream is no longer needed,
 * making it suitable for use in try-with-resources statements.
 * </p>
 * <p>
 * This class primarily holds the native memory segment representing the CZI
 * input stream and its associated
 * error code from the C API.
 * </p>
 *
 * @see CziStreamReader
 * @see InputStreamResult
 * @author Paul Mitchell
 */
public class CZIInputStream implements AutoCloseable {

    private InputStreamResult streamResult;

    protected CZIInputStream(InputStreamResult streamResult) {
        this.streamResult = streamResult;
    }

    /**
     * This method wraps the native `libCZI_CreateInputStreamFromFileUTF8` function.
     *
     * @param string The UTF-8 encoded path to the CZI file.
     * @return A {@link CZIInputStream} object representing the opened stream.
     * @throws RuntimeException If the native function call fails or the symbol is
     *                          not found.
     * @see <a href=
     *      "https://zeiss.github.io/libczi/api/function_lib_c_z_i_api_8h_1a2fdc65480b8114f3c1642785cb6a8249.html">libCZI_CreateInputStreamFromFileUTF8</a>
     * @author Paul Mitchell
     */
    public static CZIInputStream createInputStreamFromFileUTF8(String string) {
        FunctionDescriptor descriptor = FunctionDescriptor.of(JAVA_INT, ADDRESS, ADDRESS);
        MethodHandle createInputStream = Linker.nativeLinker()
                .downcallHandle(
                        LibCziFFM.SYMBOL_LOOKUP.find(
                                "libCZI_CreateInputStreamFromFileUTF8 * Creates a CZI input stream from a file specified by a UTF-8 encoded string path.")
                                .orElseThrow(
                                        () -> new UnsatisfiedLinkError(
                                                "Could not find symbol: libCZI_CreateInputStreamFromFileUTF8")),
                        descriptor);
        try (Arena arena = Arena.ofConfined()) {
            MemorySegment filenameSegment = arena.allocateFrom(string);
            MemorySegment pStream = arena.allocate(ADDRESS);
            int errorCode = (int) createInputStream.invokeExact(filenameSegment, pStream);
            if (errorCode != 0) {
                return new CZIInputStream(new InputStreamResult(errorCode, MemorySegment.NULL));
            }
            MemorySegment streamHandle = pStream.get(ADDRESS, 0);
            return new CZIInputStream(new InputStreamResult(errorCode, streamHandle));
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
