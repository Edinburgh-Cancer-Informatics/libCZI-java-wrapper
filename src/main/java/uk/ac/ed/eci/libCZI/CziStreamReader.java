/**
 * Represents a CZI stream reader that provides methods to interact with CZI files.
 * This class allows opening CZI streams, retrieving statistics, accessing attachments,
 * and managing the native reader object's lifecycle.
 * <p>
 * It acts as a wrapper around the native libCZI reader functions, handling memory
 * management and data conversion between Java and native types.
 * </p>
 * <p>
 * Instances of this class should be created using the {@link #fromStream(CZIInputStream)}
 * factory method. The reader should be closed after use to release native resources,
 * ideally using a try-with-resources statement.
 * </p>
 *
 * @see CZIInputStream
 * @see SubBlockStatistics
 * @see AttachmentInfo
 * @author Paul Mitchell
 */
package uk.ac.ed.eci.libCZI;

import java.lang.foreign.FunctionDescriptor;
import java.lang.foreign.MemoryLayout;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
import java.lang.invoke.MethodHandle;

import uk.ac.ed.eci.libCZI.metadata.Metadata;

import static java.lang.foreign.ValueLayout.*;

import java.lang.foreign.Arena;

public class CziStreamReader implements AutoCloseable {
    private MemorySegment readerHandle;
    private final Arena classArena;
    private Metadata metadata = null;


    public static CziStreamReader fromStream(CZIInputStream streamResult) {
        return new CziStreamReader(streamResult);
    }

    public SubBlockStatistics simpleReaderStatistics() {
        FunctionDescriptor descriptor = FunctionDescriptor.of(JAVA_INT, ADDRESS, ADDRESS);

        MethodHandle getStats = LibCziFFM.getMethodHandle("libCZI_ReaderGetStatisticsSimple", descriptor);
        try (Arena arena = Arena.ofConfined()) {
            MemorySegment simpleStatsStruct = arena.allocate(SubBlockStatistics.layout());
            int errorCode = (int) getStats.invokeExact(readerHandle, simpleStatsStruct);
            if (errorCode != 0) {
                throw new CziReaderException("Failed to get simple reader statistics. Error code: " + errorCode);
            }
            return SubBlockStatistics.createFromMemorySegment(simpleStatsStruct);
        } catch (Throwable e) {
            if (e instanceof CziReaderException) {
                throw (CziReaderException) e;
            }
            throw new CziReaderException("Failed to call native function libCZI_ReaderGetStatisticsSimple", e);
        }
    }

    public int attachmentCount() {
        FunctionDescriptor descriptor = FunctionDescriptor.of(JAVA_INT, ADDRESS, ADDRESS);
        MethodHandle getAttachmentCount = LibCziFFM.getMethodHandle("libCZI_ReaderGetAttachmentCount", descriptor);
        try (Arena arena = Arena.ofConfined()) {
            MemorySegment pCount = arena.allocate(JAVA_INT);
            int errorCode = (int) getAttachmentCount.invokeExact(readerHandle, pCount);
            if (errorCode != 0) {
                throw new CziReaderException("Failed to get attachment count. Error code: " + errorCode);
            }
            return pCount.get(JAVA_INT, 0);
        } catch (Throwable e) {
            throw new RuntimeException("Failed to call native function libCZI_ReaderGetAttachmentCount");
        }
    }

    public ScenePyramidStatistics pyramidStatistics() {
        FunctionDescriptor descriptor = FunctionDescriptor.of(JAVA_INT, ADDRESS, ADDRESS);
        MethodHandle getPyramidStatistics = LibCziFFM.getMethodHandle("libCZI_ReaderGetPyramidStatistics", descriptor);
        try (Arena arena = Arena.ofConfined()) {
            MemorySegment pSegment = arena.allocate(ValueLayout.ADDRESS);
            int errorCode = (int) getPyramidStatistics.invokeExact(readerHandle, pSegment);
            if (errorCode != 0) {
                throw new CziReaderException("Failed to get pyramid statistics. Error code: " + errorCode);
            }
            MemorySegment cString = pSegment.get(ADDRESS, 0);
            if (cString.equals(MemorySegment.NULL)) {
                return new ScenePyramidStatistics();
            }
            String jsonData = cString.reinterpret(Long.MAX_VALUE).getString(0);
            LibCziFFM.free(cString);

            return ScenePyramidStatistics.fromJson(jsonData);
        } catch(Throwable e) {
            if (e instanceof CziReaderException) {
                throw (CziReaderException) e;
            }
            throw new CziReaderException("Failed to call native function libCZI_ReaderGetPyramidStatistics");
        }
    }

    public AttachmentInfo[] getAttachments() {
        int count = attachmentCount();
        AttachmentInfo[] attachments = new AttachmentInfo[count];
        for (int i = 0; i < count; i++) {
            attachments[i] = getAttachmentInfo(i);
        }
        return attachments;
    }

    public Metadata metadata() {
        if (metadata == null) {
            metadata = new Metadata(readerHandle);
        }
        return metadata;
    }

    public MemorySegment readerHandle() {
        return readerHandle;
    }

    @Override
    public void close() throws Exception {
        releaseReader();
        if (metadata != null) {
            metadata.close();
        }
        this.classArena.close();
    }

    private CziStreamReader(CZIInputStream inputStream) {
        classArena = Arena.ofConfined();
        readerHandle = createReader();
        try {
            openReaderWithStream(inputStream);
        } catch (Exception e) {
            releaseReader();
            classArena.close();
            throw e;
        }
    }

    private void openReaderWithStream(CZIInputStream inputStream) {
        MemoryLayout readerOpenInfoLayout = MemoryLayout.structLayout(
                ADDRESS.withName("stream_object"));

        FunctionDescriptor descriptor = FunctionDescriptor.of(JAVA_INT, ADDRESS, ADDRESS);
        MethodHandle openReader = LibCziFFM.getMethodHandle("libCZI_ReaderOpen", descriptor);
        try (Arena arena = Arena.ofConfined()) {
            MemorySegment openInfoStruct = arena.allocate(readerOpenInfoLayout);
            openInfoStruct.set(ADDRESS, 0, inputStream.stream());
            int errorCode = (int) openReader.invokeExact(readerHandle, openInfoStruct);
            if (errorCode != 0) {
                throw new CziReaderException("Failed to open CZI stream with reader. Error code: " + errorCode);
            }
        } catch (Throwable e) {
            if (e instanceof CziReaderException) {
                throw (CziReaderException) e;
            }
            throw new CziReaderException("Failed to call native function libCZI_ReaderOpen", e);
        }
    }

    private MemorySegment createReader() {
        FunctionDescriptor descriptor = FunctionDescriptor.of(JAVA_INT, ADDRESS);
        MethodHandle createReader = LibCziFFM.getMethodHandle("libCZI_CreateReader", descriptor);
        try  {
            MemorySegment pReader = classArena.allocate(ADDRESS);
            int errorCode = (int) createReader.invokeExact(pReader);
            if (errorCode != 0) {
                throw new CziReaderException("Failed to create CZI reader. Error code: " + errorCode);
            } else {
                MemorySegment readerHandle = pReader.get(ADDRESS, 0);
                return readerHandle;
            }
        } catch (Throwable e) {
            throw new RuntimeException("Failed to call native function libCZI_CreateReader", e);
        }
    }

    private AttachmentInfo getAttachmentInfo(int index) {
        FunctionDescriptor descriptor = FunctionDescriptor.of(JAVA_INT, ADDRESS, JAVA_INT, ADDRESS);
        MethodHandle getAttachmentInfo = LibCziFFM.getMethodHandle("libCZI_ReaderGetAttachmentInfoFromDirectory",
                descriptor);
        try (Arena arena = Arena.ofConfined()) {
            MemorySegment attachmentInfoStruct = arena.allocate(AttachmentInfo.layout());
            int errorCode = (int) getAttachmentInfo.invokeExact(readerHandle, index, attachmentInfoStruct);
            if (errorCode != 0) {
                throw new CziReaderException("Failed to get attachment info. Error code: " + errorCode);
            }
            return AttachmentInfo.createFromMemorySegment(attachmentInfoStruct);
        } catch (Throwable e) {
            throw new RuntimeException("Failed to call native function libCZI_ReaderGetAttachmentInfoFromDirectory");
        }
    }

    private void releaseReader() {
        FunctionDescriptor descriptor = FunctionDescriptor.ofVoid(ADDRESS);
        MethodHandle release = LibCziFFM.getMethodHandle("libCZI_ReleaseReader", descriptor);
        try {
            release.invokeExact(readerHandle);
        } catch (Throwable e) {
            throw new RuntimeException("Failed to call native function libCZI_ReleaseReader", e);
        } finally {
            readerHandle = null;
        }
    }
}