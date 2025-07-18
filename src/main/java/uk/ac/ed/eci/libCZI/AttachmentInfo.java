package uk.ac.ed.eci.libCZI;

import java.lang.foreign.MemoryLayout;
import java.lang.foreign.MemoryLayout.PathElement;
import java.lang.foreign.MemorySegment;
import java.util.UUID;

import static java.lang.foreign.ValueLayout.*;

public record AttachmentInfo(UUID guid, String contentFileType, String name) {
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

        UUID uuid = LibCziFFM.GuidToUuidConvert(guidSegment);
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
}
