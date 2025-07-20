package uk.ac.ed.eci.libCZI;

import java.lang.foreign.MemoryLayout;
import java.lang.foreign.MemoryLayout.PathElement;
import java.lang.foreign.MemorySegment;
import java.util.UUID;

import static java.lang.foreign.ValueLayout.*;

/**
 * Represents information about an attachment within a CZI stream.
 * This class provides access to the attachment's GUID, content file type, and name.
 * It also handles memory management for the name string in case of overflow,
 * freeing the native memory when the AttachmentInfo object is created.
 * <p>
 * This class is designed to mirror the `AttachmentInfoInterop` structure from the libCZI C API.
 * </p>
 * 
 * @see <a href="https://zeiss.github.io/libczi/api/struct_attachment_info_interop.html#_CPPv421AttachmentInfoInterop">
 * AttachmentInfoInterop
 * </a>
 * @author Paul Mitchell
 */
public class AttachmentInfo implements Comparable<AttachmentInfo> {
    private final UUID guid;
    private final String contentFileType;
    private final String name;

    public AttachmentInfo(UUID guid, String contentFileType, String name) {
        this.guid = guid;
        this.contentFileType = contentFileType;
        this.name = name;
    }

    public static MemoryLayout layout() {
        return MemoryLayout.structLayout(
                MemoryLayout.sequenceLayout(16, JAVA_BYTE).withName("guid"),
                MemoryLayout.sequenceLayout(9, JAVA_BYTE).withName("content_file_type"),
                MemoryLayout.sequenceLayout(255, JAVA_BYTE).withName("name"),
                JAVA_BOOLEAN.withName("name_overflow"),
                MemoryLayout.paddingLayout(7),
                ADDRESS.withName("name_in_case_of_overflow"));
    }

    public static AttachmentInfo createFromMemorySegment(MemorySegment segment) {
        MemorySegment guidSegment = segment.asSlice(
                layout().byteOffset(PathElement.groupElement("guid")),
                16);

        UUID uuid = LibCziFFM.guidToUuidConvert(guidSegment);
        String contentFileType = segment.asSlice(
                layout().byteOffset(PathElement.groupElement("content_file_type"))).getString(0);
        String name;
        boolean nameOverflow = segment.get(JAVA_BOOLEAN,
                layout().byteOffset(PathElement.groupElement("name_overflow")));
        if (nameOverflow) {
            MemorySegment namePtr = segment.get(ADDRESS,
                    layout().byteOffset(PathElement.groupElement("name_in_case_of_overflow")));
            name = namePtr.getString(0);
            LibCziFFM.free(namePtr); // Free the memory allocated by the C library, as per the documentation.
        } else {
            name = segment.asSlice(layout().byteOffset(PathElement.groupElement("name")))
                    .getString(0);
        }
        return new AttachmentInfo(uuid, contentFileType, name);
    }

    /**
     * Attachment GUID in UUID
     * 
     * @return the UUID of the attachment
     */
    public UUID guid() {
        return guid;
    }

    /**
     * Content File Type
     * 
     * @return the name of the file type
     */
    public String contentFileType() {
        return contentFileType;
    }

    /**
     * The name of the attachment, manages the oversize from the C API
     * 
     * @return the full file name
     */
    public String name() {
        return name;
    }

    @Override
    public int compareTo(AttachmentInfo o) {
        return this.name.compareTo(o.name);
    }
}

