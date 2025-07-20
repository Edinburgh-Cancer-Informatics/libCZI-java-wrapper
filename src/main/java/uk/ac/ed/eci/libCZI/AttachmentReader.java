package uk.ac.ed.eci.libCZI;

import java.lang.foreign.Arena;
import java.lang.foreign.FunctionDescriptor;
import java.lang.foreign.MemorySegment;
import java.lang.invoke.MethodHandle;

import static java.lang.foreign.ValueLayout.*;

/**
 * Represents a CZI Attachment reader that provides method to interact with the files
 * that are within the main file.  Currently, this is only via the RAW interface
 * 
 * <p>
 * It acts as a wrapper around the native libCZI attachment reader functions, handling memory
 * management and data conversion between Java and native types.
 * </p>
 * <p>
 * Instances of this class should be create using the {@link #fromReader(CziStreamReader, int)}
 * static method.  The reader should be closed after use to relase native resources
 * idelly using a try-with-resources statement.
 * </p>
 * 
 * @see CziStreamReader
 * @author Paul Mitchell
 */
public class AttachmentReader implements AutoCloseable {
    private MemorySegment attachmentHandle;
    private MemorySegment readerHandle;
    private int index;
    private static int MAX_SIZE = 1048576; //(2^20)


	public static AttachmentReader fromReader(CziStreamReader reader, int i) {
        return new AttachmentReader(reader, i);
	}

    public AttachmentReader(CziStreamReader reader, int i) {
        readerHandle = reader.readerHandle();
        index = i;
        attachmentHandle = getAttachmentHandle();
    }

    public void closeHandle() {
        if (attachmentHandle == null || attachmentHandle.address() == 0) {
            return;
        }
        FunctionDescriptor descriptor = FunctionDescriptor.ofVoid(ADDRESS);
        MethodHandle releaseAttachment = LibCziFFM.getMethodHandle("libCZI_ReleaseAttachment", descriptor);
        try {
            releaseAttachment.invokeExact(attachmentHandle);
        } catch (Throwable e) {
            throw new RuntimeException("Failed to call native function libCZI_ReleaseAttachment", e);
        }
    }
    @Override
    public void close() throws Exception {
        closeHandle();
    }

    public MemorySegment getAttachmentRawData() {
        FunctionDescriptor descriptor = FunctionDescriptor.of(JAVA_INT, ADDRESS, ADDRESS, ADDRESS);
        MethodHandle getRawData = LibCziFFM.getMethodHandle("libCZI_AttachmentGetRawData", descriptor);

        long size;
        // First, call the native function with a NULL buffer to get the required size.
        try (Arena arena = Arena.ofConfined()) {
            MemorySegment pSize = arena.allocate(JAVA_LONG);
            int errorCode = (int) getRawData.invokeExact(attachmentHandle, pSize, MemorySegment.NULL);
            if (errorCode != 0) {
                throw new AttachmentReaderException("Failed to get attachment raw data buffer size. Error code: " + errorCode);
            }
            size = pSize.get(JAVA_LONG, 0);
            if (size > MAX_SIZE) {
                throw new AttachmentReaderSizeException(String.format("Attachment size (%d) exceeds maximum allowed size (%d)", size, MAX_SIZE));
            }

            if (size == 0) {
                return MemorySegment.NULL; // Or an empty segment if preferred
            }
            MemorySegment data = LibCziFFM.GLOBAL_ARENA.allocate(size);
            errorCode = (int) getRawData.invokeExact(attachmentHandle, pSize, data);
            if (errorCode != 0) {
                throw new AttachmentReaderException("Failed to get attachment raw data. Error code: " + errorCode);
            }
            return data;
    
        } catch (Throwable e) {
            if (e instanceof AttachmentReaderSizeException)
                throw (AttachmentReaderSizeException) e;
            throw new AttachmentReaderException("Failed to query attachment data size", e);
        }
    }
    
    private MemorySegment getAttachmentHandle(){
        FunctionDescriptor descriptor = FunctionDescriptor.of(JAVA_INT, ADDRESS, JAVA_INT, ADDRESS);
        MethodHandle readAttachment = LibCziFFM.getMethodHandle("libCZI_ReaderReadAttachment", descriptor);
        try (Arena arena = Arena.ofConfined()) {
            MemorySegment pAttachment = arena.allocate(ADDRESS);
            int errorCode = (int) readAttachment.invokeExact(readerHandle, index, pAttachment);
            if (errorCode != 0) {
                throw new AttachmentReaderException("Failed to read attachment. Error code: " + errorCode);
            }
            // Dereference the pointer to get the actual handle. The handle itself is not tied to the arena.
            return pAttachment.get(ADDRESS, 0);
        } catch (Throwable e) {
            if (e instanceof AttachmentReaderException) {
                throw (AttachmentReaderException) e;
            }
            throw new AttachmentReaderException("Failed to call native function libCZI_ReaderReadAttachment", e);
        }
    }
}
