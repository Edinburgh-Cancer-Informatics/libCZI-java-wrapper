package uk.ac.ed.eci.libCZI;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.OffsetDateTime;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import uk.ac.ed.eci.libCZI.document.DocumentInfo;
import uk.ac.ed.eci.libCZI.document.GeneralDocumentInfo;

public class DocumentInfoTest {
    private static final Path TEST_IMAGE_PATH = Paths.get("test-images", "test-image.czi");
    private CziStreamReader reader;
    private CZIInputStream streamResult;

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
    public void testOpenDocumentInfo() {
        DocumentInfo documentInfo = reader.metadata().documentInfo();
        assertNotNull(documentInfo, "Document info should not be null.");
    }

    @Test
    public void testGeneralDocumentInfo() {
        DocumentInfo documentInfo = reader.metadata().documentInfo();
        GeneralDocumentInfo generalDocumentInfo = documentInfo.generalDocumentInfo();
        assertNotNull(generalDocumentInfo, "General document info should not be null.");
        assertEquals("zeiss", generalDocumentInfo.username());

        // Compare the Instant on the timeline, which is robust against timezone/offset differences.
        Instant expectedInstant = OffsetDateTime.parse("2022-10-05T10:06:46.2913112-05:00").toInstant();
        Instant actualInstant = generalDocumentInfo.creationDateTime().toInstant();
        assertEquals(expectedInstant, actualInstant);
    }
}
