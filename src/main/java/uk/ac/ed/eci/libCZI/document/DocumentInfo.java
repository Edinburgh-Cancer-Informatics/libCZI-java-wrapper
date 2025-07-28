package uk.ac.ed.eci.libCZI.document;

import static java.lang.foreign.ValueLayout.ADDRESS;
import static java.lang.foreign.ValueLayout.JAVA_INT;

import java.lang.foreign.Arena;
import java.lang.foreign.FunctionDescriptor;
import java.lang.foreign.MemorySegment;
import java.lang.invoke.MethodHandle;

import uk.ac.ed.eci.libCZI.LibCziFFM;

public class DocumentInfo {
    private MemorySegment cziDocumentHandle;
    private final Arena classArena;

    public DocumentInfo(MemorySegment readerHandle) {
        this.classArena = Arena.ofConfined();
        this.cziDocumentHandle = getCziDocumentHandle(readerHandle);
    }

    public void close() throws Exception {
        releaseDocumentInfo();
        this.classArena.close();
    }
    
    //libCZI_CziDocumentInfoGetGeneralDocumentInfo
    public GeneralDocumentInfo generalDocumentInfo() {
        return new GeneralDocumentInfo();
    }
    
    //libCZI_CziDocumentInfoGetScalingInfo
    public ScalingInfo scalingInfo() {
        return new ScalingInfo();
    }
    
    //libCZI_CziDocumentInfoGetAvailableDimension
    public AvailableDimensions availableDimensions() {
        return new AvailableDimensions();
    }
    //libCZI_CziDocumentInfoGetDisplaySettings
    public DisplaySettings displaySettings() {
        return new DisplaySettings();
    }
    //libCZI_CziDocumentInfoGetDimensionInfo
    public DimensionInfo dimensionInfo() {
        return new DimensionInfo();
    }
    
    private MemorySegment getCziDocumentHandle(MemorySegment handle) {
        FunctionDescriptor descriptor = FunctionDescriptor.of(JAVA_INT, ADDRESS, ADDRESS);
        MethodHandle getDocumentInfo = LibCziFFM.getMethodHandle("libCZI_MetadataSegmentGetCziDocumentInfo", descriptor);
        try {
            MemorySegment pDocumentInfo = classArena.allocate(ADDRESS);
            int errorCode = (int) getDocumentInfo.invokeExact(handle, pDocumentInfo);
            if (errorCode != 0) {
                throw new RuntimeException("Failed to get CZI document info. Error code: " + errorCode);
            }
            return pDocumentInfo.get(ADDRESS, 0).asReadOnly();
        }
        catch (Throwable e) {
            throw new RuntimeException("Failed to call native function libCZI_MetadataSegmentGetCziDocumentInfo", e);
        }
    }

    private void releaseDocumentInfo() {
        FunctionDescriptor descriptor = FunctionDescriptor.of(JAVA_INT, ADDRESS);
        MethodHandle release = LibCziFFM.getMethodHandle("libCZI_ReleaseCziDocumentInfo", descriptor);
        try {
            int errorCode = (int) release.invokeExact(cziDocumentHandle);
            if (errorCode != 0) {
                throw new RuntimeException("Failed to release CZI document info. Error code: " + errorCode);
            }
        }
        catch (Throwable e) {
            throw new RuntimeException("Failed to call native function libCZI_ReleaseCziDocumentInfo", e);
        }
    }
}
