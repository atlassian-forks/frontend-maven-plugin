package com.github.eirslett.maven.plugins.frontend.lib;

import org.junit.jupiter.api.Test;

import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;

import static com.github.eirslett.maven.plugins.frontend.lib.IncrementalMojoHelper.addTrackedFile;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class IncrementalMojoHelperTest {

    @Test
    public void shouldProcessLargeFilesAndNotCauseMemoryAllocationSpikes() throws Exception {
        // Create a temporary directory
        Path tempDir = Files.createTempDirectory("large-file-test");
        Path largeFile = tempDir.resolve("large-test-file.bin");
        final long largeFileSize = 100 * 1024 * 1024; // 500 MB

        try {
            // Create a sparse file that appears larger than 4GB
            try (RandomAccessFile file = new RandomAccessFile(largeFile.toFile(), "rw")) {
                file.setLength(largeFileSize);

                // Write some actual content at the beginning and end to make it a valid file
                file.seek(0);
                file.write("header content".getBytes());
                file.seek(largeFileSize - 100);
                file.write("footer content".getBytes());
            }

            // Monitor memory before processing
            long memoryBefore = java.lang.Runtime.getRuntime().totalMemory() - java.lang.Runtime.getRuntime().freeMemory();

            // Process the file - this should work with streaming and not load the entire file
            Collection<IncrementalBuildExecutionDigest.Execution.File> files = new ArrayList<>();
            addTrackedFile(files, largeFile);

            // Monitor memory after processing
            long memoryAfter = java.lang.Runtime.getRuntime().totalMemory() - java.lang.Runtime.getRuntime().freeMemory();

            // Memory should not have increased dramatically (allow for some overhead)
            // This is a simple check - memory usage might fluctuate due to GC
            assertTrue(memoryAfter - memoryBefore < 10 * 1024 * 1024, "Memory usage increased too much");

            assertEquals(1, files.size());
            IncrementalBuildExecutionDigest.Execution.File processedFile = files.iterator().next();
            assertEquals(largeFile.toString(), processedFile.filename);
            assertEquals(largeFileSize, processedFile.byteLength);
            assertEquals("upAFairMcyh+mleCRZx3UCiDoa0HhcNOP5d6ZUkdMHk=", processedFile.hash);

        } finally {
            // Clean up
            Files.deleteIfExists(largeFile);
            Files.deleteIfExists(tempDir);
        }
    }
}
