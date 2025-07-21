package uk.ac.ed.eci.libCZI;

import java.io.IOException;
import java.lang.foreign.FunctionDescriptor;
import java.lang.foreign.MemorySegment;
import java.lang.invoke.MethodHandle;
import java.nio.ByteBuffer;
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
 * @author Paul Mitchell
 */
public class CZIInputStream implements AutoCloseable {

    private InputStreamResult streamResult;
    // These fields are used for streams created from a Java SeekableByteChannel
    private final SeekableByteChannel channel;
    private final Arena externalStreamArena;

    protected CZIInputStream(InputStreamResult streamResult) {
        this(streamResult, null, null);
    }

    /**
     * Private constructor for streams created from an external Java stream.
     * It holds the Java channel and the Arena that manages the native callback's
     * lifecycle.
     */
    private CZIInputStream(InputStreamResult streamResult, SeekableByteChannel channel, Arena arena) {
        this.streamResult = streamResult;
        this.channel = channel;
        this.externalStreamArena = arena;
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
        CZIInputStream result = new CZIInputStream(null, stream, Arena.ofConfined());
        MemorySegment segment = LibCziFFM.GLOBAL_ARENA.allocate(ExternalInputStreamStruct.LAYOUT);
        ExternalInputStreamStruct externalStream = new ExternalInputStreamStruct(segment);
        externalStream.setReadFunction(
            LibCziFFM.getMethodHandle(
                "readCallback", 
                FunctionDescriptor.of(JAVA_INT, ADDRESS, ADDRESS, JAVA_LONG, ADDRESS, JAVA_LONG, ADDRESS, ADDRESS))
                .asPointer());

        throw new UnsupportedOperationException("Not implemented yet.");
    }

    /**
     * This is the Java method that will be called by the native libCZI library to
     * read data.
     * It's an instance method, which allows it to directly access the
     * `this.channel`.
     * The FFM API's `upcallStub` will be created from a MethodHandle bound to a
     * specific CZIInputStream instance.
     *
     * @param opaque_handle1 A user-defined handle (ignored by us).
     * @param opaque_handle2 A user-defined handle (ignored by us).
     * @param offset         The position in the stream to read from.
     * @param buffer         A native memory segment to read the data into.
     * @param size           The number of bytes to read.
     * @param bytesReadPtr   A native pointer to a long where we must write the
     *                       number of bytes actually read.
     * @param errorInfoPtr   A native pointer to an error info struct (ignored for
     *                       now).
     * @return 0 on success, non-zero on failure.
     */
    private int readCallback(MemorySegment opaque_handle1, MemorySegment opaque_handle2, long offset,
            MemorySegment buffer, long size, MemorySegment bytesReadPtr, MemorySegment errorInfoPtr) {
        try {
            this.channel.position(offset);

            // Wrap the native memory in a Java ByteBuffer to use with the Channel API
            ByteBuffer javaBuffer = buffer.asByteBuffer().limit((int) size);

            int bytesRead = this.channel.read(javaBuffer);
            if (bytesRead < 0) { // End of stream
                bytesRead = 0;
            }

            // Report the number of bytes read back to the native caller
            if (!bytesReadPtr.equals(MemorySegment.NULL)) {
                bytesReadPtr.set(JAVA_LONG, 0, (long) bytesRead);
            }
            return 0; // Success
        } catch (IOException e) {
            System.err.println("Error in CZIInputStream read callback: " + e.getMessage());
            return 1; // Indicate failure
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
        if (streamResult != null && streamResult.stream() != null) {
            LibCziFFM.free(streamResult.stream());
        }
        // If this stream was created from a Java stream, we also need to close the
        // Arena, which will release the native upcall stubs.
        if (externalStreamArena != null) {
            externalStreamArena.close();
        }
    }

}
