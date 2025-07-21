package uk.ac.ed.eci.libCZI;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.jupiter.api.Test;

public class InputStreamBridgeTest {
    private static final Path TEST_IMAGE_PATH = Paths.get("test-images", "test-image.czi");
    private final SeekableByteChannel channel;

    public InputStreamBridgeTest() throws IOException {
        this.channel = FileChannel.open(TEST_IMAGE_PATH);
    }


    @Test
    public void testOpenFileAndGetReader() {
        CZIInputStream streamResult = CZIInputStream.createInputStreamFromJavaStream(channel);
        assertEquals(0, streamResult.errorCode(), "Should open stream without error.");
    }
}
