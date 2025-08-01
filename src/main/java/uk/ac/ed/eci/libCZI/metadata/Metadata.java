package uk.ac.ed.eci.libCZI.metadata;

import static java.lang.foreign.ValueLayout.ADDRESS;
import static java.lang.foreign.ValueLayout.JAVA_INT;

import java.lang.foreign.Arena;
import java.lang.foreign.FunctionDescriptor;
import java.lang.foreign.MemorySegment;
import java.lang.invoke.MethodHandle;

import uk.ac.ed.eci.libCZI.LibCziFFM;
import uk.ac.ed.eci.libCZI.document.DocumentInfo;

public class Metadata {
    private MemorySegment handle;
    private Arena classArena;
    private DocumentInfo documentInfo = null;


    public Metadata(MemorySegment reader) {
        classArena = Arena.ofConfined();
        handle = getHandleFromReader(reader);
    }

    public DocumentInfo documentInfo() {
        if (documentInfo == null) {
            documentInfo = new DocumentInfo(handle);
        }
        return documentInfo;
    }
    
    public void close() throws Exception {
        if (documentInfo != null) {
            documentInfo.close();
        }
        releaseMetadata();
        this.classArena.close();
    }

    private MemorySegment getHandleFromReader(MemorySegment reader) {
        FunctionDescriptor descriptor = FunctionDescriptor.of(JAVA_INT, ADDRESS, ADDRESS);
        MethodHandle getHandle = LibCziFFM.getMethodHandle("libCZI_ReaderGetMetadataSegment", descriptor);
        try {
            MemorySegment pHandle = classArena.allocate(ADDRESS);
            int errorCode = (int) getHandle.invokeExact(reader, pHandle);
            if (errorCode != 0) {
                throw new RuntimeException("Failed to get metadata segment. Error code: " + errorCode);
            }
            return pHandle.get(ADDRESS, 0);
        } catch (Throwable e) {
            throw new RuntimeException("Failed to call native function libCZI_ReaderGetMetadataSegment", e);
        }

    }

    private void releaseMetadata() {
        FunctionDescriptor descriptor = FunctionDescriptor.of(JAVA_INT, ADDRESS);
        MethodHandle release = LibCziFFM.getMethodHandle("libCZI_ReleaseMetadataSegment", descriptor);
        try {
            int errorCode = (int) release.invokeExact(handle);
            if (errorCode != 0) {
                throw new RuntimeException("Failed to release metadata segment. Error code: " + errorCode);
            }
        } catch (Throwable e) {
            throw new RuntimeException("Failed to call native function libCZI_ReleaseMetadataSegment", e);
        }
    }
}
