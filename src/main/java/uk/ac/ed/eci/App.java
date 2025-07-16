package uk.ac.ed.eci;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.foreign.MemoryLayout;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import javax.imageio.ImageIO;

import uk.ac.ed.eci.LibCziFFM.BitmapInfo;
import uk.ac.ed.eci.LibCziFFM.BitmapLockInfo;

/**
 * Hello world!
 *
 */
public class App {
    public static void main( String[] args ) {
        if (args.length != 1) {
           System.err.println("Require one argument: filename"); 
        }
        String testFile = args[0];

        System.out.println( "Querying libCZI version..." );
        try {
            // Call the wrapper method to get the version information
            LibCziFFM.VersionInfo version = LibCziFFM.getLibCZIVersionInfo();
            // Print the version information

            System.out.println("Successfully retrieved libCZI version info:");
            System.out.printf("  Major: %d\n", version.major());
            System.out.printf("  Minor: %d\n", version.minor());
            System.out.printf("  Patch: %d\n", version.patch());
            System.out.printf("  Tweak: %d\n", version.tweak());
            MemoryLayout layout = LibCziFFM.BitmapLockInfo.layout();
            System.out.println("Java Layout total size: " + layout.byteSize() + " bytes");
            System.out.println("Java Layout ptrData offset: " + layout.byteOffset(MemoryLayout.PathElement.groupElement("ptrData")) + " bytes");
            System.out.println("Java Layout ptrDataRoi offset: " + layout.byteOffset(MemoryLayout.PathElement.groupElement("ptrDataRoi")) + " bytes");
            System.out.println("Java Layout stride offset: " + layout.byteOffset(MemoryLayout.PathElement.groupElement("stride")) + " bytes");
            System.out.println("Java Layout size offset: " + layout.byteOffset(MemoryLayout.PathElement.groupElement("size")) + " bytes");
            if (new File(testFile).exists()) {
                System.out.println("\nOpening CZI file: " + testFile);
                LibCziFFM.InputStreamResult streamResult = LibCziFFM.createInputStreamFromFileUTF8(testFile);
                if (streamResult.errorCode() != 0) {
                    System.err.println("Failed to create input stream, error code: " + streamResult.errorCode());
                    return;
                }
                System.out.println("Input stream created successfully.");

                LibCziFFM.ReaderResult readerResult = LibCziFFM.createReader();
                if (readerResult.errorCode() != 0) {
                    System.err.println("Failed to create reader, error code: " + readerResult.errorCode());
                    return;
                }
                System.out.println("Reader created successfully.");

                readerResult = LibCziFFM.readerOpen(readerResult, streamResult.stream());
                if (readerResult.errorCode() != 0) {
                    System.err.println("Failed to open reader with stream, error code: " + readerResult.errorCode());
                    return;
                }
                System.out.println("Reader opened CZI file successfully.");

                // Get and print statistics
                System.out.println("\nGetting simple sub-block statistics...");
                LibCziFFM.StatisticsResult statsResult = LibCziFFM.getReaderStatisticsSimple(readerResult);
                if (statsResult.errorCode() != 0) {
                    System.err.println("Failed to get statistics, error code: " + statsResult.errorCode());
                    return;
                }

                System.out.println("Successfully retrieved statistics:");
                LibCziFFM.SubBlockStatistics stats = statsResult.statistics();
                System.out.printf("  Sub-block count: %d\n", stats.subBlockCount());

                System.out.println("\nGetting attachment count...");
                LibCziFFM.ReaderAttachmentCount attachmentCount = LibCziFFM.readerGetAttachmentCount(readerResult);
                if (attachmentCount.errorCode() != 0) {
                    System.err.println("Failed to get attachment count");  
                }
                System.out.printf("  Attachment count: %d\n", attachmentCount.count());

                System.out.println("\nGetting attachment info...");
                System.out.println("Index\t| FileType\t| GUID \t\t\t\t | name ");
                System.out.println("------+----------+--------------------------------------+------");
                for (int i = 0; i < attachmentCount.count(); i++) {
                    LibCziFFM.AttachmentInfoResult attachmentInfoResult = LibCziFFM.readerGetAttachmentInfoFromDirectory(readerResult, i);
                    System.out.printf("%d\t| %s | %s | %s\n", 
                        i,
                         attachmentInfoResult.attachmentInfo().contentFileType(),
                         attachmentInfoResult.attachmentInfo().guid().toString(),
                         attachmentInfoResult.attachmentInfo().name()
                    );
                }
                System.out.println("\n End of attachment info");
                System.out.println("Fetching thumbnail...\n");
                LibCziFFM.ReaderReadAttachmentResult attachmentResult = LibCziFFM.readerReadAttachment(readerResult, 0);
                if (attachmentResult.errorCode() != 0) {
                    System.err.println("Failed to read attachment, error code: " + attachmentResult.errorCode());
                    return;
                }
                LibCziFFM.AttachmentData attachmentData = LibCziFFM.AttachmentGetRawData(attachmentResult.attachment());
                ByteBuffer byteBuffer = attachmentData.data().asByteBuffer();
                try (FileOutputStream fos = new FileOutputStream("Label-thumbnail.jpg")) {
                    fos.getChannel().write(byteBuffer); // Efficiently writes the buffer's content
                    System.out.println("Java: Successfully wrote " + byteBuffer.limit() + " bytes to label.data");

                } catch (IOException e) {
                    System.err.println("Java: Error writing data to file: " + e.getMessage());
                    e.printStackTrace();
                }
                LibCziFFM.releaseAttachment(attachmentResult.attachment());
                LibCziFFM.releaseReader(readerResult);
                LibCziFFM.free(streamResult.stream());

                
            } else {
                System.out.println("\nTest CZI file not found, skipping reader tests.");
            }

        } catch (Exception e) {
            System.err.println("An error occurred:");
            e.printStackTrace();
        }
    }

    public static BufferedImage toBufferedImage(BitmapInfo info, BitmapLockInfo lockInfo) throws IOException {
        // For BGR24, BufferedImage.TYPE_3BYTE_BGR is the perfect match.
        // It's packed in a byte array, with the order B, G, R.
        BufferedImage image = new BufferedImage(info.width(), info.height(), BufferedImage.TYPE_3BYTE_BGR);


        // Get the internal byte array that the BufferedImage uses
        byte[] imageData = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();

        // Wrap the native memory segment in a ByteBuffer for easier reading
        // Ensure the byte order matches your native system or the data's expected order
        // On most modern systems, it's LITTLE_ENDIAN
        ByteBuffer nativeByteBuffer = lockInfo.ptrDataRoi().asByteBuffer().order(ByteOrder.nativeOrder());
        // If your C++ data comes from a specific source with a known endianness,
        // you might explicitly set it: .order(ByteOrder.LITTLE_ENDIAN);

        int bytesPerPixel = 3; // BGR24 means 3 bytes (B, G, R) per pixel

        // Copy row by row, respecting the stride
        for (int y = 0; y < info.height(); y++) {
            // Calculate the source offset in the native buffer for the current row
            long sourceRowOffset = (long) y * lockInfo.stride();

            // Calculate the destination offset in the BufferedImage's byte array for the current row
            // BufferedImage's internal data is usually tightly packed (width * bytesPerPixel)
            int destRowOffset = y * info.width() * bytesPerPixel;

            // Check if the source offset plus the row's data exceeds the native segment's size
            if (sourceRowOffset + (long)info.width() * bytesPerPixel > lockInfo.size()) {
                // This indicates an issue with stride, width, height, or size calculation
                throw new IOException("Row data exceeds allocated memory bounds at row " + y);
            }

            // Copy the bytes for the current row
            // The segment.get(byte.class, offset) or segment.asByteBuffer().get(byte[])
            // approach depends on direct MemorySegment operations vs ByteBuffer view.
            // Using ByteBuffer for block copy is often efficient.

            nativeByteBuffer.position((int) sourceRowOffset); // Set buffer position to start of current row in native memory
            nativeByteBuffer.get(imageData, destRowOffset, info.width() * bytesPerPixel); // Read data for current row
        }

        return image;
    }

    /**
     * Saves a BufferedImage to a specified file path as a BMP.
     *
     * @param image The BufferedImage to save.
     * @param filePath The path to the output BMP file.
     * @throws IOException If there's an error writing the file.
     */
    public static void saveImageAsBmp(BufferedImage image, String filePath) throws IOException {
        File outputFile = new File(filePath);
        ImageIO.write(image, "bmp", outputFile);
        System.out.println("Image saved successfully to: " + filePath);
    }

}
