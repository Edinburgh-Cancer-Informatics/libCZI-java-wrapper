package uk.ac.ed.eci.libCZI;

import java.lang.foreign.FunctionDescriptor;
import java.lang.foreign.MemoryLayout;
import java.lang.foreign.MemorySegment;
import java.lang.invoke.MethodHandle;

import static java.lang.foreign.ValueLayout.*;

import java.lang.foreign.Arena;

public class CziStreamReader {
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
        IntRect nullRect = new IntRect(0,0,0,0);
        FunctionDescriptor descriptor = FunctionDescriptor.of(JAVA_INT, ADDRESS, ADDRESS);

        MethodHandle getStats = LibCziFFM.GetMethodHandle("libCZI_ReaderGetStatisticsSimple", descriptor);
        try (Arena arena = Arena.ofConfined()) {
            MemorySegment simpleStatsStruct = arena.allocate(SubBlockStatistics.layout());
            int errorCode = (int) getStats.invokeExact(readerResult.reader, simpleStatsStruct);
            if (errorCode != 0) {                
                return new SubBlockStatistics(0, 0, 0, nullRect, nullRect, null);
            }
            return SubBlockStatistics.createFromMemorySegment(simpleStatsStruct);
        } catch(Throwable e) {
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
}
