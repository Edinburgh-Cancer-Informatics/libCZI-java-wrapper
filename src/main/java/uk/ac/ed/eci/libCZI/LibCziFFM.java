package uk.ac.ed.eci.libCZI;

import java.lang.foreign.Arena;
import java.lang.foreign.FunctionDescriptor;
import java.lang.foreign.Linker;
import java.lang.foreign.MemoryLayout;
import java.lang.foreign.MemoryLayout.PathElement;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.SymbolLookup;
import java.lang.foreign.ValueLayout;
import java.lang.invoke.MethodHandle;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.UUID;

import static java.lang.foreign.ValueLayout.ADDRESS;
import static java.lang.foreign.ValueLayout.JAVA_BOOLEAN;
import static java.lang.foreign.ValueLayout.JAVA_BYTE;
import static java.lang.foreign.ValueLayout.JAVA_INT;
import static java.lang.foreign.ValueLayout.JAVA_LONG;

import java.io.IOException;

public class LibCziFFM {
    // A global, shared arena for loading the library. It lives for the duration of
    // the application.
    private static final Arena GLOBAL_ARENA = Arena.ofAuto();
    public static final SymbolLookup SYMBOL_LOOKUP = getSymbolLookup();
    public static final int K_MAX_DIMENSION_COUNT = 9;
    public static final int K_MIN_DIMENSION_COUNT = 1;

    private static SymbolLookup getSymbolLookup() {
        String libName = System.mapLibraryName("CZIAPI");
        try {
            NativeUtils.loadLibraryFromJar(libName);
            return SymbolLookup.loaderLookup();
        } catch (IOException e) {
            throw new UnsatisfiedLinkError("Count not liad CZI library from JAR: " + e.getMessage());
        }
    }

    /**
     * A simple record to hold the version information, making it easy to return
     * from our wrapper method.
     */
    public record VersionInfo(int major, int minor, int patch, int tweak) {
    }

    public static VersionInfo getLibCZIVersionInfo() {
        // 1. Define the layout of the C struct
        MemoryLayout structLayout = MemoryLayout.structLayout(
                ValueLayout.JAVA_INT.withName("major"),
                ValueLayout.JAVA_INT.withName("minor"),
                ValueLayout.JAVA_INT.withName("patch"),
                ValueLayout.JAVA_INT.withName("tweak"));

        // 2. Look up the native function symbol
        MemorySegment getVersionInfoAddr = SYMBOL_LOOKUP.find("libCZI_GetLibCZIVersionInfo")
                .orElseThrow(() -> new UnsatisfiedLinkError("Could not find symbol: libCZI_GetLibCZIVersionInfo"));

        // 3. Define the function descriptor. The C function takes a POINTER to the
        // struct.
        // The return type is an int (LibCZIApiErrorCode).
        FunctionDescriptor descriptor = FunctionDescriptor.of(JAVA_INT, ADDRESS);

        // 4. Get a MethodHandle for the function
        MethodHandle getVersionInfo = Linker.nativeLinker().downcallHandle(getVersionInfoAddr, descriptor);

        // 5. Use a confined Arena to allocate memory for the struct.
        // This memory will be automatically freed when the try-with-resources block
        // exits.
        try (Arena arena = Arena.ofConfined()) {
            // 6. Allocate the struct memory. This segment is what we pass to the native
            // function.
            MemorySegment versionInfoStruct = arena.allocate(structLayout);

            // 7. Invoke the native function. It will write the version info into our
            // MemorySegment.
            int errorCode = (int) getVersionInfo.invokeExact(versionInfoStruct);

            if (errorCode != 0) { // Assuming 0 is success, which is common.
                throw new RuntimeException("libCZI_GetLibCZIVersionInfo failed with error code: " + errorCode);
            }

            // 8. Read the values from the struct using the layout to find the correct
            // offsets.
            int major = versionInfoStruct.get(JAVA_INT, structLayout.byteOffset(PathElement.groupElement("major")));
            int minor = versionInfoStruct.get(JAVA_INT, structLayout.byteOffset(PathElement.groupElement("minor")));
            int patch = versionInfoStruct.get(JAVA_INT, structLayout.byteOffset(PathElement.groupElement("patch")));
            int tweak = versionInfoStruct.get(JAVA_INT, structLayout.byteOffset(PathElement.groupElement("tweak")));

            return new VersionInfo(major, minor, patch, tweak);
        } catch (Throwable e) {
            // Rethrow as a runtime exception to make the calling code cleaner.
            throw new RuntimeException("Failed to call native function", e);
        }
    }

    public record InputStreamResult(int errorCode, MemorySegment stream) {
    }

    /**
     * Creates an input stream from a file. This is a wrapper around the native
     * function:
     * LibCZIApiErrorCode libCZI_CreateInputStreamFromFileUTF8(const char*
     * filename_utf8, LibCZIIStream** stream);
     *
     * @param filename The UTF-8 path to the file.
     * @return An InputStreamResult containing the error code and the opaque stream
     *         handle.
     */
    public static InputStreamResult createInputStreamFromFileUTF8(String filename) {
        // 1. Define the function descriptor. It takes two pointers (const char*,
        // LibCZIIStream**) and returns an int.
        FunctionDescriptor descriptor = FunctionDescriptor.of(JAVA_INT, ADDRESS, ADDRESS);

        MethodHandle createInputStream = Linker.nativeLinker()
                .downcallHandle(
                        SYMBOL_LOOKUP.find("libCZI_CreateInputStreamFromFileUTF8").orElseThrow(
                                () -> new UnsatisfiedLinkError(
                                        "Could not find symbol: libCZI_CreateInputStreamFromFileUTF8")),
                        descriptor);

        try (Arena arena = Arena.ofConfined()) {
            // 2. Allocate memory for the input filename string.
            MemorySegment filenameSegment = arena.allocateFrom(filename);
            // 3. Allocate memory for the output pointer (the LibCZIIStream**).
            // This is where the native function will write the address of the new stream.
            MemorySegment pStream = arena.allocate(ADDRESS);
            // 4. Invoke the native function.
            int errorCode = (int) createInputStream.invokeExact(filenameSegment, pStream);
            if (errorCode != 0) {
                return new InputStreamResult(errorCode, MemorySegment.NULL);
            }
            // 5. If successful, read the pointer value from pStream to get the actual
            // stream handle.
            MemorySegment streamHandle = pStream.get(ADDRESS, 0);
            return new InputStreamResult(errorCode, streamHandle);
        } catch (Throwable e) {
            throw new RuntimeException("Failed to call native function libCZI_CreateInputStreamFromFileUTF8", e);
        }
    }

    private static MethodHandle createMemoryHandle(String name, FunctionDescriptor descriptor) {
        return Linker.nativeLinker().downcallHandle(
                SYMBOL_LOOKUP.find(name).orElseThrow(() -> new UnsatisfiedLinkError("Could not find symbol: " + name)),
                descriptor);
    }

    public record ReaderResult(int errorCode, MemorySegment reader) {
    }

    public static ReaderResult createReader() {
        FunctionDescriptor descriptor = FunctionDescriptor.of(JAVA_INT, ADDRESS);
        MethodHandle createReader = createMemoryHandle("libCZI_CreateReader", descriptor);
        try (Arena arena = Arena.ofConfined()) {
            // Allocate memory for the output pointer (CziReaderObjectHandle*)
            MemorySegment pReader = arena.allocate(ADDRESS);
            int errorCode = (int) createReader.invokeExact(pReader);
            if (errorCode != 0) {
                return new ReaderResult(errorCode, MemorySegment.NULL);
            }
            // Dereference the pointer to get the actual reader handle
            MemorySegment readerHandle = pReader.get(ADDRESS, 0);
            return new ReaderResult(errorCode, readerHandle);
        } catch (Throwable e) {
            throw new RuntimeException("Failed to call native function libCZI_CreateReader", e);
        }
    }

    public static ReaderResult readerOpen(ReaderResult readerObject, MemorySegment streamObject) {
        // The C function is:
        // libCZI_ReaderOpen(CziReaderObjectHandle reader_object, const
        // ReaderOpenInfoInterop* open_info)
        // The second argument is a pointer to a struct that contains the stream handle.
        // We must construct this struct in Java.

        // 1. Define the layout for the ReaderOpenInfoInterop struct.
        // Assuming it just contains the stream handle based on the documentation.
        MemoryLayout readerOpenInfoLayout = MemoryLayout.structLayout(
                ADDRESS.withName("stream_object"));

        FunctionDescriptor descriptor = FunctionDescriptor.of(JAVA_INT, ADDRESS, ADDRESS);
        MethodHandle openReader = createMemoryHandle("libCZI_ReaderOpen", descriptor);

        try (Arena arena = Arena.ofConfined()) {
            // 2. Allocate memory for the struct.
            MemorySegment openInfoStruct = arena.allocate(readerOpenInfoLayout);

            // 3. Populate the struct with the stream handle.
            openInfoStruct.set(ADDRESS, 0, streamObject);

            // 4. Call the native function with the reader handle and a pointer to our new
            // struct.
            int errorCode = (int) openReader.invokeExact(readerObject.reader, openInfoStruct);
            return new ReaderResult(errorCode, readerObject.reader);
        } catch (Throwable e) {
            throw new RuntimeException("Failed to call native function libCZI_ReaderOpen", e);
        }
    }

    public record IntRect(int x, int y, int w, int h) {
        public static MemoryLayout layout() {
            return MemoryLayout.structLayout(
                    JAVA_INT.withName("x"),
                    JAVA_INT.withName("y"),
                    JAVA_INT.withName("w"),
                    JAVA_INT.withName("h"));
        }
    }

    public record IntSize(int w, int h) {
        public static MemoryLayout layout() {
            return MemoryLayout.structLayout(
                    JAVA_INT.withName("w"),
                    JAVA_INT.withName("h"));
        }
    }

    public record DimBounds(int dimensionsValid, int[] start, int[] size) {
    }

    public record SubBlockStatistics(
            int subBlockCount,
            int minMIndex,
            int maxMIndex,
            IntRect boundingBox,
            IntRect boundingBoxLayer0,
            DimBounds dimBounds) {
    }

    public record StatisticsResult(int errorCode, SubBlockStatistics statistics) {
    }

    public static StatisticsResult getReaderStatisticsSimple(ReaderResult readerObject) {
        // Define layouts based on the C header files.
        // From misc_types.h:
        MemoryLayout intRectLayout = MemoryLayout.structLayout(
                JAVA_INT.withName("x"),
                JAVA_INT.withName("y"),
                JAVA_INT.withName("w"),
                JAVA_INT.withName("h"));

        MemoryLayout dimBoundsLayout = MemoryLayout.structLayout(
                JAVA_INT.withName("dimensions_valid"),
                MemoryLayout.sequenceLayout(K_MAX_DIMENSION_COUNT, JAVA_INT).withName("start"),
                MemoryLayout.sequenceLayout(K_MAX_DIMENSION_COUNT, JAVA_INT).withName("size"));

        // From subblock_statistics_struct.h:
        MemoryLayout statsLayout = MemoryLayout.structLayout(
                JAVA_INT.withName("sub_block_count"),
                JAVA_INT.withName("min_m_index"),
                JAVA_INT.withName("max_m_index"),
                intRectLayout.withName("bounding_box"),
                intRectLayout.withName("bounding_box_layer0"),
                dimBoundsLayout.withName("dim_bounds"));

        FunctionDescriptor descriptor = FunctionDescriptor.of(JAVA_INT, ADDRESS, ADDRESS);
        MethodHandle getStats = createMemoryHandle("libCZI_ReaderGetStatisticsSimple", descriptor);

        try (Arena arena = Arena.ofConfined()) {
            // Allocate memory for the output statistics struct.
            MemorySegment statsStruct = arena.allocate(statsLayout);

            // Call the native function.
            int errorCode = (int) getStats.invokeExact(readerObject.reader, statsStruct);
            if (errorCode != 0) {
                return new StatisticsResult(errorCode, null);
            }

            // Read the data from the populated struct into our Java records.
            int subBlockCount = statsStruct.get(JAVA_INT,
                    statsLayout.byteOffset(PathElement.groupElement("sub_block_count")));
            int minMIndex = statsStruct.get(JAVA_INT, statsLayout.byteOffset(PathElement.groupElement("min_m_index")));
            int maxMIndex = statsStruct.get(JAVA_INT, statsLayout.byteOffset(PathElement.groupElement("max_m_index")));

            MemorySegment bbSegment = statsStruct
                    .asSlice(statsLayout.byteOffset(PathElement.groupElement("bounding_box")));
            IntRect boundingBox = new IntRect(
                    bbSegment.get(JAVA_INT, intRectLayout.byteOffset(PathElement.groupElement("x"))),
                    bbSegment.get(JAVA_INT, intRectLayout.byteOffset(PathElement.groupElement("y"))),
                    bbSegment.get(JAVA_INT, intRectLayout.byteOffset(PathElement.groupElement("w"))),
                    bbSegment.get(JAVA_INT, intRectLayout.byteOffset(PathElement.groupElement("h"))));

            MemorySegment bb0Segment = statsStruct
                    .asSlice(statsLayout.byteOffset(PathElement.groupElement("bounding_box_layer0")));
            IntRect boundingBoxLayer0 = new IntRect(
                    bb0Segment.get(JAVA_INT, intRectLayout.byteOffset(PathElement.groupElement("x"))),
                    bb0Segment.get(JAVA_INT, intRectLayout.byteOffset(PathElement.groupElement("y"))),
                    bb0Segment.get(JAVA_INT, intRectLayout.byteOffset(PathElement.groupElement("w"))),
                    bb0Segment.get(JAVA_INT, intRectLayout.byteOffset(PathElement.groupElement("h"))));

            MemorySegment dbSegment = statsStruct
                    .asSlice(statsLayout.byteOffset(PathElement.groupElement("dim_bounds")));
            int dimensionsValid = dbSegment.get(JAVA_INT,
                    dimBoundsLayout.byteOffset(PathElement.groupElement("dimensions_valid")));
            int[] start = dbSegment.asSlice(dimBoundsLayout.byteOffset(PathElement.groupElement("start")))
                    .toArray(JAVA_INT);
            int[] size = dbSegment.asSlice(dimBoundsLayout.byteOffset(PathElement.groupElement("size")))
                    .toArray(JAVA_INT);
            DimBounds dimBounds = new DimBounds(dimensionsValid, start, size);

            SubBlockStatistics stats = new SubBlockStatistics(subBlockCount, minMIndex, maxMIndex, boundingBox,
                    boundingBoxLayer0, dimBounds);
            return new StatisticsResult(errorCode, stats);
        } catch (Throwable e) {
            throw new RuntimeException("Failed to call native function libCZI_ReaderGetStatisticsSimple", e);
        }
    }

    public record ReaderAttachmentCount(int errorCode, int count) {
    }

    public static ReaderAttachmentCount readerGetAttachmentCount(ReaderResult readerObject) {
        FunctionDescriptor descriptor = FunctionDescriptor.of(JAVA_INT, ADDRESS, ADDRESS);
        MethodHandle getAttachmentCount = createMemoryHandle("libCZI_ReaderGetAttachmentCount", descriptor);

        try (Arena arena = Arena.ofConfined()) {
            MemorySegment pCount = arena.allocate(JAVA_INT);
            int errorCode = (int) getAttachmentCount.invokeExact(readerObject.reader, pCount);
            if (errorCode != 0) {
                return new ReaderAttachmentCount(errorCode, 0);
            }
            int count = pCount.get(JAVA_INT, 0);
            return new ReaderAttachmentCount(errorCode, count);
        } catch (Throwable e) {
            throw new RuntimeException("Failed to call native function libCZI_ReaderGetAttachmentCount", e);
        }

    }

    public record AttachmentInfo(UUID guid, String contentFileType, String name) {
    }

    public record AttachmentInfoResult(
            int errorCode,
            AttachmentInfo attachmentInfo) {
    }

    // While the node and clock sequence fields are usually stored byte-for-byte 
    // in network byte order (big-endian), the first three fields 
    // (time_low, time_mid, time_hi_and_version) are often stored in little-endian
    // byte order within the 16-byte array, especially on Windows or when 
    // dealing with Intel-based systems. Java's UUID construct expects
    // both long values to be in big-endian (network) order
    //
    public static UUID GuidToUuidConvert(MemorySegment guidSegment) {
        byte[] guidBytes = new byte[16];
        guidSegment.asByteBuffer().order(ByteOrder.LITTLE_ENDIAN).get(guidBytes);

        // Then manually reorder the first 8 bytes to get the mostSigBits
        // For a typical Windows GUID (time_low, time_mid, time_hi_and_version are little-endian)
        long msb = 0;
        msb |= ((long) guidBytes[3] & 0xFF) << 56; // time_low (byte 3)
        msb |= ((long) guidBytes[2] & 0xFF) << 48; // time_low (byte 2)
        msb |= ((long) guidBytes[1] & 0xFF) << 40; // time_low (byte 1)
        msb |= ((long) guidBytes[0] & 0xFF) << 32; // time_low (byte 0)

        msb |= ((long) guidBytes[5] & 0xFF) << 24; // time_mid (byte 1)
        msb |= ((long) guidBytes[4] & 0xFF) << 16; // time_mid (byte 0)

        msb |= ((long) guidBytes[7] & 0xFF) << 8;  // time_hi_and_version (byte 1)
        msb |= ((long) guidBytes[6] & 0xFF);       // time_hi_and_version (byte 0)

        // The last 8 bytes (clock_seq and node) are typically big-endian.
        long lsb = 0;
        for (int i = 8; i < 16; i++) {
            lsb = (lsb << 8) | (guidBytes[i] & 0xFF);
        }

        return new UUID(msb, lsb);
    }

    public static AttachmentInfoResult readerGetAttachmentInfoFromDirectory(ReaderResult readerObject, int index) {
        FunctionDescriptor descriptor = FunctionDescriptor.of(JAVA_INT, ADDRESS, JAVA_INT, ADDRESS);
        MethodHandle getAttachmentInfo = createMemoryHandle("libCZI_ReaderGetAttachmentInfoFromDirectory", descriptor);
        MemoryLayout attachmentInfoLayout = MemoryLayout.structLayout(
                MemoryLayout.sequenceLayout(16, JAVA_BYTE).withName("guid"),
                MemoryLayout.sequenceLayout(9, JAVA_BYTE).withName("content_file_type"),
                MemoryLayout.sequenceLayout(255, JAVA_BYTE).withName("name"),
                JAVA_BOOLEAN.withName("name_overflow"),
                MemoryLayout.paddingLayout(7),
                ADDRESS.withName("name_in_case_of_overflow"));

        try (Arena arena = Arena.ofConfined()) {
            MemorySegment attachmentInfoStruct = arena.allocate(attachmentInfoLayout);
            int errorCode = (int) getAttachmentInfo.invokeExact(readerObject.reader, index, attachmentInfoStruct);
            if (errorCode != 0) {
                return new AttachmentInfoResult(errorCode, null);
            }

            MemorySegment guidSegment = attachmentInfoStruct.asSlice(
                attachmentInfoLayout.byteOffset(PathElement.groupElement("guid")),
                16
            );
            

            UUID uuid = GuidToUuidConvert(guidSegment);
            String contentFileType = attachmentInfoStruct.asSlice(
                    attachmentInfoLayout.byteOffset(PathElement.groupElement("content_file_type"))).getString(0);
            String name;
            boolean nameOverflow = attachmentInfoStruct.get(JAVA_BOOLEAN,
                    attachmentInfoLayout.byteOffset(PathElement.groupElement("name_overflow")));
            if (nameOverflow) {
                MemorySegment namePtr = attachmentInfoStruct.get(ADDRESS,
                        attachmentInfoLayout.byteOffset(PathElement.groupElement("name_in_case_of_overflow")));
                name = namePtr.getString(0);
                free(namePtr); // Free the memory allocated by the C library, as per the documentation.
            } else {
                name = attachmentInfoStruct.asSlice(attachmentInfoLayout.byteOffset(PathElement.groupElement("name")))
                        .getString(0);
            }
            return new AttachmentInfoResult(errorCode,
                    new AttachmentInfo(uuid, contentFileType, name));
        } catch (Throwable e) {
            throw new RuntimeException("Failed to call native function libCZI_ReaderGetAttachmentInfoFromDirectory", e);
        }
    }

    public record SubBlockObjectHandle(MemorySegment handle) {
    }

    public record ReaderReadSubBlockResult(int errorCode, SubBlockObjectHandle subBlockObject) {
    }

    public static ReaderReadSubBlockResult readerReadSubBlock(ReaderResult readerObject, int index) {
        FunctionDescriptor descriptor = FunctionDescriptor.of(JAVA_INT, ADDRESS, JAVA_INT, ADDRESS);
        MethodHandle readSubBlock = createMemoryHandle("libCZI_ReaderReadSubBlock", descriptor);
        try (Arena arena = Arena.ofConfined()) {
            MemorySegment pSubBlockObject = arena.allocate(ADDRESS);
            int errorCode = (int) readSubBlock.invokeExact(readerObject.reader, index, pSubBlockObject);
            if (errorCode != 0) {
                return new ReaderReadSubBlockResult(errorCode, null);
            }
            MemorySegment subBlockObjectHandle = pSubBlockObject.get(ADDRESS, 0);
            return new ReaderReadSubBlockResult(errorCode, new SubBlockObjectHandle(subBlockObjectHandle));
        } catch (Throwable e) {
            throw new RuntimeException("Failed to call native function libCZI_ReaderReadSubBlock", e);
        }

    }

    public static void free(MemorySegment segment) {
        if (segment == null || segment.address() == 0) {
            return;
        }
        FunctionDescriptor descriptor = FunctionDescriptor.ofVoid(ADDRESS);
        MethodHandle free = createMemoryHandle("libCZI_Free", descriptor);
        try {
            free.invokeExact(segment);
        } catch (Throwable e) {
            throw new RuntimeException("Failed to call native function libCZI_Free", e);
        }
    }

    public static void releaseReader(ReaderResult readerObject) {
        if (readerObject.reader != null && readerObject.errorCode() == 0) {
            FunctionDescriptor descriptor = FunctionDescriptor.ofVoid(ADDRESS);
            MethodHandle release = createMemoryHandle("libCZI_ReleaseReader", descriptor);
            try {
                release.invokeExact(readerObject.reader);
            } catch (Throwable e) {
                throw new RuntimeException("Failed to call native function libCZI_ReleaseReader", e);
            }

        }
    }

    public record Coordinate(int dimensionsValid, int[] value) {
        public static MemoryLayout layout() {
            return MemoryLayout.structLayout(
                    JAVA_INT.withName("dimensions_valid"),
                    MemoryLayout.sequenceLayout(K_MAX_DIMENSION_COUNT, JAVA_INT).withName("value"));
        }
    }

    public record SubBlockInfo(
            int compressionModeRaw,
            int pixelType,
            Coordinate coordinate,
            IntRect logicalRect,
            IntSize physicalSize,
            int mIndex) {
    }

    public record SubBlockInfoResult(
            int errorCode,
            SubBlockInfo subBlockInfo) {
    }

    public static SubBlockInfoResult subBlockGetInfo(SubBlockObjectHandle subBlockObject) {
        FunctionDescriptor descriptor = FunctionDescriptor.of(JAVA_INT, ADDRESS, ADDRESS);
        MethodHandle getInfo = createMemoryHandle("libCZI_SubBlockGetInfo", descriptor);
        MemoryLayout subBlockInfoLayout = MemoryLayout.structLayout(
                JAVA_INT.withName("compression_mode_raw"),
                JAVA_INT.withName("pixel_type"),
                Coordinate.layout().withName("coordinate"),
                IntRect.layout().withName("logical_rect"),
                IntSize.layout().withName("physical_size"),
                JAVA_INT.withName("m_index"));

        try (Arena arena = Arena.ofConfined()) {
            MemorySegment subBlockInfoStruct = arena.allocate(subBlockInfoLayout);
            int errorCode = (int) getInfo.invokeExact(subBlockObject.handle, subBlockInfoStruct);
            if (errorCode != 0) {
                return new SubBlockInfoResult(errorCode, null);
            }
            int compressionModeRaw = subBlockInfoStruct.get(JAVA_INT,
                    subBlockInfoLayout.byteOffset(PathElement.groupElement("compression_mode_raw")));
            int pixelType = subBlockInfoStruct.get(JAVA_INT,
                    subBlockInfoLayout.byteOffset(PathElement.groupElement("pixel_type")));

            // CoordinateInterop
            MemorySegment sbiCoordinate = subBlockInfoStruct
                    .asSlice(subBlockInfoLayout.byteOffset(PathElement.groupElement("coordinate")));
            Coordinate coordinate = new Coordinate(
                    sbiCoordinate.get(JAVA_INT,
                            Coordinate.layout().byteOffset(PathElement.groupElement("dimensions_valid"))),
                    sbiCoordinate.asSlice(Coordinate.layout().byteOffset(PathElement.groupElement("value")))
                            .toArray(JAVA_INT));

            // IntRectInterop
            MemorySegment sbiIntRect = subBlockInfoStruct
                    .asSlice(subBlockInfoLayout.byteOffset(PathElement.groupElement("logical_rect")));
            IntRect logicalRect = new IntRect(
                    sbiIntRect.get(JAVA_INT, IntRect.layout().byteOffset(PathElement.groupElement("x"))),
                    sbiIntRect.get(JAVA_INT, IntRect.layout().byteOffset(PathElement.groupElement("y"))),
                    sbiIntRect.get(JAVA_INT, IntRect.layout().byteOffset(PathElement.groupElement("w"))),
                    sbiIntRect.get(JAVA_INT, IntRect.layout().byteOffset(PathElement.groupElement("h"))));

            // IntSizeInterop
            MemorySegment sbiIntSize = subBlockInfoStruct
                    .asSlice(subBlockInfoLayout.byteOffset(PathElement.groupElement("physical_size")));
            IntSize physicalSize = new IntSize(
                    sbiIntSize.get(JAVA_INT, IntSize.layout().byteOffset(PathElement.groupElement("w"))),
                    sbiIntSize.get(JAVA_INT, IntSize.layout().byteOffset(PathElement.groupElement("h"))));

            int mIndex = subBlockInfoStruct.get(JAVA_INT,
                    subBlockInfoLayout.byteOffset(PathElement.groupElement("m_index")));

            return new SubBlockInfoResult(errorCode,
                    new SubBlockInfo(compressionModeRaw, pixelType, coordinate, logicalRect, physicalSize, mIndex));
        } catch (Throwable e) {
            throw new RuntimeException("Failed to call native function libCZI_SubBlockGetInfo", e);
        }
    }

    public record BitmapHandle(MemorySegment handle) {
    }

    public record SubBlockBitmapResult(int errorCode, BitmapHandle subBlockBitmap) {
    }

    public static SubBlockBitmapResult subBlockCreateBitmap(SubBlockObjectHandle subBlockObject) {
        FunctionDescriptor descriptor = FunctionDescriptor.of(JAVA_INT, ADDRESS, ADDRESS);
        MethodHandle createBitmap = createMemoryHandle("libCZI_SubBlockCreateBitmap", descriptor);
        try (Arena arena = Arena.ofConfined()) {
            MemorySegment pBitmap = arena.allocate(ADDRESS);
            int errorCode = (int) createBitmap.invokeExact(subBlockObject.handle, pBitmap);
            if (errorCode != 0) {
                return new SubBlockBitmapResult(errorCode, null);
            }
            MemorySegment bitmapHandle = pBitmap.get(ADDRESS, 0);
            return new SubBlockBitmapResult(errorCode, new BitmapHandle(bitmapHandle));
        } catch (Throwable e) {
            throw new RuntimeException("Failed to call native function libCZI_SubBlockCreateBitmap", e);
        }
    }

    public static void releaseBitmap(BitmapHandle bitmapHandle) {
        if (bitmapHandle.handle == null || bitmapHandle.handle.address() == 0) {
            return;
        }
        FunctionDescriptor descriptor = FunctionDescriptor.ofVoid(ADDRESS);
        MethodHandle freeBitmap = createMemoryHandle("libCZI_ReleaseBitmap", descriptor);
        try {
            freeBitmap.invokeExact(bitmapHandle.handle);
        } catch (Throwable e) {
            throw new RuntimeException("Failed to call native function libCZI_ReleaseBitmap", e);
        }
    }

    public enum PixelType {
        Invalid(0xFF),
        Gray8(0),
        Gray16(1),
        Gray32Float(2),
        Bgr24(3),
        Bgr48(4),
        Bgr96Float(8),
        Bgra32(9), // Currently not supported in libCZI.
        Gray64ComplexFloat(10), // Currently not supported in libCZI.
        Bgr192ComplexFloat(11), // Currently not supported in libCZI.
        Gray32(12), // Currently not supported in libCZI.
        Gray64Float(13); // Currently not supported in libCZI.

        private final int value;

        PixelType(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }

        // Optional: A static method to get an enum by its value, similar to C++ casting
        public static PixelType fromValue(int value) {
            for (PixelType type : PixelType.values()) {
                if (type.value == value) {
                    return type;
                }
            }
            // You might want to throw an IllegalArgumentException here or return null
            // depending on how you want to handle unknown values.
            throw new IllegalArgumentException("Unknown PixelType value: " + value);
        }
    }

    public record BitmapInfo(int width, int height, PixelType pixelType) {
        public static MemoryLayout layout() {
            return MemoryLayout.structLayout(
                    JAVA_INT.withName("width"),
                    JAVA_INT.withName("height"),
                    JAVA_INT.withName("pixel_type"));
        }
    }

    public record BitmapInfoResult(int errorCode, BitmapInfo bitmapInfo) {
    }

    public static BitmapInfoResult getBitmapInfo(BitmapHandle subBlockBitmapHandle) {
        FunctionDescriptor descriptor = FunctionDescriptor.of(JAVA_INT, ADDRESS, ADDRESS);
        MethodHandle getBitmapInfo = createMemoryHandle("libCZI_BitmapGetInfo", descriptor);
        MemoryLayout bitmapInfoLayout = BitmapInfo.layout();
        try (Arena arena = Arena.ofConfined()) {
            MemorySegment bitmapInfoStruct = arena.allocate(bitmapInfoLayout);
            int errorCode = (int) getBitmapInfo.invokeExact(subBlockBitmapHandle.handle, bitmapInfoStruct);
            if (errorCode != 0) {
                return new BitmapInfoResult(errorCode, null);
            }
            int width = bitmapInfoStruct.get(JAVA_INT, bitmapInfoLayout.byteOffset(PathElement.groupElement("width")));
            int height = bitmapInfoStruct.get(JAVA_INT,
                    bitmapInfoLayout.byteOffset(PathElement.groupElement("height")));
            PixelType pixelType = PixelType.fromValue(bitmapInfoStruct.get(JAVA_INT,
                    bitmapInfoLayout.byteOffset(PathElement.groupElement("pixel_type"))));
            return new BitmapInfoResult(errorCode, new BitmapInfo(width, height, pixelType));
        } catch (Throwable e) {
            throw new RuntimeException("Failed to call native function libCZI_BitmapGetInfo", e);
        }
    }

    public record BitmapLockInfo(MemorySegment ptrData, MemorySegment ptrDataRoi, int stride, long size) {
        public static MemoryLayout layout() {
            return MemoryLayout.structLayout(
                    ADDRESS.withName("ptrData"),
                    ADDRESS.withName("ptrDataRoi"),
                    JAVA_INT.withName("stride"),
                    MemoryLayout.paddingLayout(4),
                    JAVA_LONG.withName("size"));
        }
    }

    public record BitmapLockResult(int errorCode, BitmapLockInfo bitmapLockInfo) {
    }

    public static BitmapLockResult bitmapLock(BitmapHandle subBlockBitmapHandle) {
        FunctionDescriptor descriptor = FunctionDescriptor.of(JAVA_INT, ADDRESS, ADDRESS);
        MethodHandle lockBitmap = createMemoryHandle("libCZI_BitmapLock", descriptor);
        try (Arena arena = Arena.ofConfined()) {
            MemorySegment bitmapLockInfoStruct = arena.allocate(BitmapLockInfo.layout());
            int errorCode = (int) lockBitmap.invokeExact(subBlockBitmapHandle.handle, bitmapLockInfoStruct);
            if (errorCode != 0) {
                return new BitmapLockResult(errorCode, null);
            }
            MemorySegment ptrData = bitmapLockInfoStruct.get(ADDRESS,
                    BitmapLockInfo.layout().byteOffset(PathElement.groupElement("ptrData")));
            MemorySegment ptrDataRoi = bitmapLockInfoStruct.get(ADDRESS,
                    BitmapLockInfo.layout().byteOffset(PathElement.groupElement("ptrDataRoi")));
            int stride = bitmapLockInfoStruct.get(JAVA_INT,
                    BitmapLockInfo.layout().byteOffset(PathElement.groupElement("stride")));
            long size = bitmapLockInfoStruct.get(JAVA_LONG,
                    BitmapLockInfo.layout().byteOffset(PathElement.groupElement("size")));
            return new BitmapLockResult(errorCode, new BitmapLockInfo(ptrData, ptrDataRoi, stride, size));
        } catch (Throwable e) {
            throw new RuntimeException("Failed to call native function libCZI_BitmapLock", e);
        }
    }

    public static void bitmapUnlock(BitmapHandle subBlockBitmapHandle) {
        FunctionDescriptor descriptor = FunctionDescriptor.ofVoid(ADDRESS);
        MethodHandle unlockBitmap =createMemoryHandle("libCZI_BitmapUnlock", descriptor);
        try {
            unlockBitmap.invokeExact(subBlockBitmapHandle.handle);
        } catch (Throwable e) {
            throw new RuntimeException("Failed to call native function libCZI_BitmapUnlock", e);
        }
    }

    public record AttachmentHandle(MemorySegment handle) {
    }

    public record ReaderReadAttachmentResult(int errorCode, AttachmentHandle attachment) {
    }

    public static ReaderReadAttachmentResult readerReadAttachment(ReaderResult readerObject, int index) {
        FunctionDescriptor descriptor = FunctionDescriptor.of(JAVA_INT, ADDRESS, JAVA_INT, ADDRESS);
        MethodHandle readAttachment = createMemoryHandle("libCZI_ReaderReadAttachment", descriptor);
        try (Arena arena = Arena.ofConfined()) {
            MemorySegment pAttachment = arena.allocate(ADDRESS);
            int errorCode = (int) readAttachment.invokeExact(readerObject.reader, index, pAttachment);
            if (errorCode != 0) {
                return new ReaderReadAttachmentResult(errorCode, null);
            }
            MemorySegment attachmentHandle = pAttachment.get(ADDRESS, 0);
            return new ReaderReadAttachmentResult(errorCode, new AttachmentHandle(attachmentHandle));       
        } catch( Throwable e) {
            throw new RuntimeException("Failed to call native function libCZI_ReaderReadAttachment", e);
        }
    }

    public record AttachmentData(MemorySegment data) {
    }

    public static AttachmentData AttachmentGetRawData(AttachmentHandle attachmentHandle) {
        FunctionDescriptor descriptor = FunctionDescriptor.of(JAVA_INT, ADDRESS, ADDRESS, ADDRESS);
        MethodHandle getRawData = createMemoryHandle("libCZI_AttachmentGetRawData", descriptor);
        try (Arena arena = Arena.ofConfined()) {
            MemorySegment data = arena.allocate(ADDRESS);
            MemorySegment msSize = arena.allocate(JAVA_LONG);
            msSize.set(JAVA_LONG, 0, 0L);
            int errorCode = (int) getRawData.invokeExact(attachmentHandle.handle, msSize, data);
            if (errorCode != 0) {
                return new AttachmentData(null);
            }
            long size = msSize.get(JAVA_LONG, 0);
            System.out.printf("Allocating size: %d\n", size);
            data = GLOBAL_ARENA.allocate(size);
            errorCode = (int) getRawData.invokeExact(attachmentHandle.handle, msSize, data);
            if (errorCode != 0) {
                return new AttachmentData(null);
            }
            return new AttachmentData(data);
        } catch(Throwable e) {
            throw new RuntimeException("Failed to call native function libCZI_AttachmentGetRawData",e );
        }
    }

    public static void releaseAttachment(AttachmentHandle attachmentHandle) {
        if (attachmentHandle.handle == null || attachmentHandle.handle.address() == 0) {
            return;
        }
        FunctionDescriptor descriptor = FunctionDescriptor.ofVoid(ADDRESS);
        MethodHandle releaseAttachment = createMemoryHandle("libCZI_ReleaseAttachment", descriptor);
        try {
            releaseAttachment.invokeExact(attachmentHandle.handle);
        } catch (Throwable e) {
            throw new RuntimeException("Failed to call native function libCZI_ReleaseAttachment", e);
        }
    }
}