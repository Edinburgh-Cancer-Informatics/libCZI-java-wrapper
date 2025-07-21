package uk.ac.ed.eci.libCZI;

import static java.lang.foreign.ValueLayout.ADDRESS;

import java.lang.foreign.MemoryLayout;
import java.lang.foreign.MemoryLayout.PathElement;
import java.lang.foreign.MemorySegment;

/**
 * Represents the native `ExternalInputStreamStructInterop` structure for use
 * with libCZI's external stream functions.
 * <p>
 * This class defines the memory layout for the C struct and provides methods
 * to set the opaque handles and
 * function pointers for the read and close callbacks. An instance of this
 * class, backed by a native
 * {@link MemorySegment}, is passed to
 * `libCZI_CreateInputStreamFromExternal`.
 * </p>
 *
 * @see <a href=
 *      "https://zeiss.github.io/libczi/api/struct_external_input_stream_struct_interop.html">ExternalInputStreamStructInterop</a>
 * @author Paul Mitchell
 */
public class ExternalInputStreamStruct {

    /**
     * The memory layout for the `ExternalInputStreamStructInterop` C struct.
     * <p>
     * It consists of two opaque handles and two function pointers, all of which are
     * addresses.
     * </p>
     * 
     * <pre>
     * {@code
     * struct ExternalInputStreamStructInterop {
     *     uintptr_t opaque_handle1;
     *     uintptr_t opaque_handle2;
     *     int32_t (*read_function)(...);
     *     void (*close_function)(...);
     * };
     * }
     * </pre>
     */
    public static final MemoryLayout LAYOUT = MemoryLayout.structLayout(
            ADDRESS.withName("opaque_handle1"),
            ADDRESS.withName("opaque_handle2"),
            ADDRESS.withName("read_function"),
            ADDRESS.withName("close_function"));

    private static final long OPAQUE_HANDLE1_OFFSET = LAYOUT.byteOffset(PathElement.groupElement("opaque_handle1"));
    private static final long OPAQUE_HANDLE2_OFFSET = LAYOUT.byteOffset(PathElement.groupElement("opaque_handle2"));
    private static final long READ_FUNCTION_OFFSET = LAYOUT.byteOffset(PathElement.groupElement("read_function"));
    private static final long CLOSE_FUNCTION_OFFSET = LAYOUT.byteOffset(PathElement.groupElement("close_function"));

    private final MemorySegment segment;

    public ExternalInputStreamStruct(MemorySegment segment) {
        this.segment = segment;
    }

    public void setOpaqueHandle1(MemorySegment handle) {
        segment.set(ADDRESS, OPAQUE_HANDLE1_OFFSET, handle);
    }

    public void setOpaqueHandle2(MemorySegment handle) {
        segment.set(ADDRESS, OPAQUE_HANDLE2_OFFSET, handle);
    }

    public void setReadFunction(MemorySegment functionPointer) {
        segment.set(ADDRESS, READ_FUNCTION_OFFSET, functionPointer);
    }

    public void setCloseFunction(MemorySegment functionPointer) {
        segment.set(ADDRESS, CLOSE_FUNCTION_OFFSET, functionPointer);
    }
}