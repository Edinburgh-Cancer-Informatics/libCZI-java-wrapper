package uk.ac.ed.eci.libCZI;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public class NativeUtils {

    public static void loadLibraryFromJar(String libraryFileName) throws IOException {
        // Prepend "native/" to the library file name as it is located in the "native" folder
        String fullLibraryFileName = "native/" + libraryFileName;

        // Prepare a destination file
        Path tempDir = Files.createTempDirectory("native-libs");
        File tempLib = tempDir.resolve(libraryFileName).toFile();
        tempLib.deleteOnExit(); // Delete the file when JVM exits (optional, for cleanup)

        try (InputStream in = NativeUtils.class.getClassLoader().getResourceAsStream(fullLibraryFileName)) {
            if (in == null) {
                throw new IOException("Library not found in JAR: " + fullLibraryFileName);
            }
            Files.copy(in, tempLib.toPath(), StandardCopyOption.REPLACE_EXISTING);
        }

        System.load(tempLib.getAbsolutePath());
    }
}

