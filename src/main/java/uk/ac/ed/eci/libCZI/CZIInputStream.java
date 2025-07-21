package uk.ac.ed.eci.libCZI;

import java.lang.foreign.FunctionDescriptor;
import java.lang.foreign.MemorySegment;
import java.lang.invoke.MethodHandle;
import java.nio.channels.SeekableByteChannel;

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
 * @see InputStreamBridge
 * @author Paul Mitchell
 */
public class CZIInputStream implements AutoCloseable {

    private InputStreamResult streamResult;
    private final InputStreamBridge bridge;    

    protected CZIInputStream(InputStreamResult streamResult) {
        this.streamResult = streamResult;
        this.bridge = null;
    }

    /**
     * Private constructor for streams created from an external Java stream.
     * It holds the Java channel and the Arena that manages the native callback's
     * lifecycle.
     */
    private CZIInputStream(InputStreamResult streamResult, InputStreamBridge bridge) {
        this.streamResult = streamResult;
        this.bridge = bridge;

        connectBridge();
    }

    private void connectBridge() {
        FunctionDescriptor descriptor = FunctionDescriptor.of(JAVA_INT, ADDRESS, ADDRESS);
        MethodHandle inputStreamFromExternal = LibCziFFM.getMethodHandle("libCZI_CreateInputStreamFromExternal", descriptor);
        try (Arena arena = Arena.ofConfined()) {
            MemorySegment externalStreamStruct = bridge.createExternalInputStreamStruct(0, 0);
            MemorySegment pStream = arena.allocate(ADDRESS);
            int errorCode = (int) inputStreamFromExternal.invokeExact(externalStreamStruct, pStream);
            if (errorCode != 0) { // Non-zero indicates an error
                throw new CziStreamException("Failed to create CZI input stream from external stream. Error code: " + errorCode);
            }
            MemorySegment streamHandle = pStream.get(ADDRESS, 0);
            streamResult = new InputStreamResult(errorCode, streamHandle);
        } catch (Throwable e) {
            if (e instanceof CziStreamException) {
                throw (CziStreamException) e;
            }
            throw new CziReaderException("Failed to call native function libCZI_CreateInputStreamFromExternal", e);
        }
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
        MethodHandle createInputStream = LibCziFFM.getMethodHandle("libCZI_CreateInputStreamFromFileUTF8", descriptor);
        try (Arena arena = Arena.ofConfined()) {
            MemorySegment filenameSegment = arena.allocateFrom(string);
            MemorySegment pStream = arena.allocate(ADDRESS);
            int errorCode = (int) createInputStream.invokeExact(filenameSegment, pStream);
            if (errorCode != 0) { // Non-zero indicates an error
                throw new CziStreamException("Failed to create CZI input stream from file. Error code: " + errorCode);
            }
            MemorySegment streamHandle = pStream.get(ADDRESS, 0);
            return new CZIInputStream(new InputStreamResult(errorCode, streamHandle));
        } catch (Throwable e) {
            if (e instanceof CziStreamException) {
                throw (CziStreamException) e;
            }
            throw new CziStreamException("Failed to call native function libCZI_CreateInputStreamFromFileUTF8", e);
        }
    }

    public static CZIInputStream createInputStreamFromJavaStream(SeekableByteChannel stream) {
        InputStreamBridge bridge = new InputStreamBridge(stream);
        return new CZIInputStream(null, bridge);
    }

    public Integer errorCode() {
        return streamResult.errorCode();
    }

    public MemorySegment stream() {
        return streamResult.stream();
    }

    @Override
    public void close() throws Exception {
        if (streamResult != null && streamResult.stream() != null) {
            LibCziFFM.free(streamResult.stream());
        }
        // If this stream was created from a Java stream, we also need to close the
        // Arena, which will release the native upcall stubs.
        if (bridge != null) {
            bridge.close();
        }
    }

}
