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
import java.lang.invoke.MethodHandle;

import static java.lang.foreign.ValueLayout.*;

import java.lang.foreign.Arena;

public class CziStreamReader implements AutoCloseable {
    private ReaderResult readerResult;

    public class ReaderResult {
        public int errorCode;
        public MemorySegment reader;

        public ReaderResult(int errorCode, MemorySegment reader) {
            this.errorCode = errorCode;
            this.reader = reader;
        }
    }

    public static CziStreamReader fromStream(CZIInputStream streamResult) {
        return new CziStreamReader(streamResult);
    }

    private CziStreamReader(CZIInputStream streamResult) {
        createReader();
        readerOpen(streamResult);
    }

    public Integer errorCode() {
        return readerResult.errorCode;
    }

    public MemorySegment reader() {
        return readerResult.reader;
    }

    public SubBlockStatistics simpleReaderStatistics() {
        IntRect nullRect = new IntRect(0, 0, 0, 0);
        FunctionDescriptor descriptor = FunctionDescriptor.of(JAVA_INT, ADDRESS, ADDRESS);

        MethodHandle getStats = LibCziFFM.GetMethodHandle("libCZI_ReaderGetStatisticsSimple", descriptor);
        try (Arena arena = Arena.ofConfined()) {
            MemorySegment simpleStatsStruct = arena.allocate(SubBlockStatistics.layout());
            int errorCode = (int) getStats.invokeExact(readerResult.reader, simpleStatsStruct);
            if (errorCode != 0) {
                return new SubBlockStatistics(0, 0, 0, nullRect, nullRect, null);
            }
            return SubBlockStatistics.createFromMemorySegment(simpleStatsStruct);
        } catch (Throwable e) {
            throw new RuntimeException("Failed to call native function libCZI_ReaderGetStatisticsSimple", e);
        }
    }

    private void createReader() {
        FunctionDescriptor descriptor = FunctionDescriptor.of(JAVA_INT, ADDRESS);
        MethodHandle createReader = LibCziFFM.GetMethodHandle("libCZI_CreateReader", descriptor);
        try (Arena arena = Arena.ofConfined()) {
            MemorySegment pReader = arena.allocate(ADDRESS);
            int errorCode = (int) createReader.invokeExact(pReader);
            if (errorCode != 0) {
                readerResult = new ReaderResult(errorCode, MemorySegment.NULL);
            } else {
                MemorySegment readerHandle = pReader.get(ADDRESS, 0);
                readerResult = new ReaderResult(errorCode, readerHandle);
            }
        } catch (Throwable e) {
            throw new RuntimeException("Failed to call native function libCZI_CreateReader", e);
        }
    }

    private void readerOpen(CZIInputStream inputStream) {
        MemoryLayout readerOpenInfoLayout = MemoryLayout.structLayout(
                ADDRESS.withName("stream_object"));

        FunctionDescriptor descriptor = FunctionDescriptor.of(JAVA_INT, ADDRESS, ADDRESS);
        MethodHandle openReader = LibCziFFM.GetMethodHandle("libCZI_ReaderOpen", descriptor);
        try (Arena arena = Arena.ofConfined()) {
            MemorySegment openInfoStruct = arena.allocate(readerOpenInfoLayout);
            openInfoStruct.set(ADDRESS, 0, inputStream.stream());
            readerResult.errorCode = (int) openReader.invokeExact(readerResult.reader, openInfoStruct);
        } catch (Throwable e) {
            throw new RuntimeException("Failed to call native function libCZI_ReaderOpen", e);
        }
    }

    public int attachmentCount() {
        FunctionDescriptor descriptor = FunctionDescriptor.of(JAVA_INT, ADDRESS, ADDRESS);
        MethodHandle getAttachmentCount = LibCziFFM.GetMethodHandle("libCZI_ReaderGetAttachmentCount", descriptor);
        try (Arena arena = Arena.ofConfined()) {
            MemorySegment pCount = arena.allocate(JAVA_INT);
            int errorCode = (int) getAttachmentCount.invokeExact(readerResult.reader, pCount);
            if (errorCode != 0) {
                return 0;
            }
            int count = pCount.get(JAVA_INT, 0);
            return count;
        } catch (Throwable e) {
            throw new RuntimeException("Failed to call native function libCZI_ReaderGetAttachmentCount");
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

    private AttachmentInfo getAttachmentInfo(int index) {
        FunctionDescriptor descriptor = FunctionDescriptor.of(JAVA_INT, ADDRESS, JAVA_INT, ADDRESS);
        MethodHandle getAttachmentInfo = LibCziFFM.GetMethodHandle("libCZI_ReaderGetAttachmentInfoFromDirectory",
                descriptor);
        try (Arena arena = Arena.ofConfined()) {
            MemorySegment attachmentInfoStruct = arena.allocate(AttachmentInfo.layout());
            int errorCode = (int) getAttachmentInfo.invokeExact(readerResult.reader, index, attachmentInfoStruct);
            if (errorCode != 0) {
                return null;
            }
            return AttachmentInfo.createFromMemorySegment(attachmentInfoStruct);
        } catch (Throwable e) {
            throw new RuntimeException("Failed to call native function libCZI_ReaderGetAttachmentInfoFromDirectory");
        }
    }

    @Override
    public void close() throws Exception {
        FunctionDescriptor descriptor = FunctionDescriptor.ofVoid(ADDRESS);
        MethodHandle release = LibCziFFM.GetMethodHandle("libCZI_ReleaseReader", descriptor);
        try {
            release.invokeExact(readerResult.reader);
        } catch (Throwable e) {
            throw new RuntimeException("Failed to call native function libCZI_ReleaseReader", e);
        }
    }
}
