package uk.ac.ed.eci.libCZI;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.foreign.MemorySegment;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class AttachmentReaderTest {
    private CziStreamReader reader;
    private CZIInputStream streamResult;
    private static final Path TEST_IMAGE_PATH = Paths.get("test-images", "test-image.czi");

    @BeforeEach
    public void setup() {
        assertTrue(Files.exists(TEST_IMAGE_PATH), "Test image should have been downloaded by Maven.");

        streamResult = CZIInputStream.createInputStreamFromFileUTF8(TEST_IMAGE_PATH.toString());
        reader = CziStreamReader.fromStream(streamResult);
    }

    @AfterEach
    public void teardown() throws Exception {
        reader.close();
        streamResult.close();
    }

    @Test    
    public void testGetAttachmentRawDataEventList() throws Exception {
        try (AttachmentReader attachment = AttachmentReader.fromReader(reader, 0)) {
            MemorySegment data = attachment.getAttachmentRawData();
            assertEquals(data.byteSize(), 8);
        }
    }

    @Test    
    public void testGetAttachmentRawDataTimeStamps() throws Exception {
        try (AttachmentReader attachment = AttachmentReader.fromReader(reader, 1)) {
            MemorySegment data = attachment.getAttachmentRawData();
            assertEquals(data.byteSize(), 16);
        }
    }

    @Test    
    public void testGetAttachmentRawDataLabel() throws Exception {
        try (AttachmentReader attachment = AttachmentReader.fromReader(reader, 2)) {
            assertThrows(AttachmentReaderSizeException.class, () -> {
                attachment.getAttachmentRawData();
            });
        }
    }

    @Test    
    public void testGetAttachmentRawDataSlidePreview() throws Exception {
        try (AttachmentReader attachment = AttachmentReader.fromReader(reader, 3)) {
            assertThrows(AttachmentReaderSizeException.class, () -> {
                attachment.getAttachmentRawData();
            });
        }
    }

    @Test    
    public void testGetAttachmentRawProfile() throws Exception {
        try (AttachmentReader attachment = AttachmentReader.fromReader(reader, 4)) {
            MemorySegment data = attachment.getAttachmentRawData();
            assertEquals(data.byteSize(), 55612);
            byte[] gZipHeader = {(byte) 0x1F,(byte) 0x8B};
            byte[] dataHeader = new byte[2];
            ByteBuffer bb = data.asByteBuffer().order(ByteOrder.LITTLE_ENDIAN);
            bb.get(dataHeader, 0, 2);
            assertTrue(java.util.Arrays.equals(gZipHeader, dataHeader));
        }
    }

    @Test    
    public void testGetAttachmentRawThumbnail() throws Exception {
        try (AttachmentReader attachment = AttachmentReader.fromReader(reader,5)) {
            MemorySegment data = attachment.getAttachmentRawData();
            assertEquals(data.byteSize(), 2584);
            byte[] jpegHeader = {(byte) 0xFF,(byte) 0xD8, (byte)0xFF};
            byte[] dataHeader = new byte[3];
            ByteBuffer bb = data.asByteBuffer().order(ByteOrder.LITTLE_ENDIAN);
            bb.get(dataHeader, 0, 3);
            assertTrue(java.util.Arrays.equals(jpegHeader, dataHeader));
        }
    }
}
