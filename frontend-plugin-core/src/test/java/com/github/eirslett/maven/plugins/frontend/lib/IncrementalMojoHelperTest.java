package com.github.eirslett.maven.plugins.frontend.lib;

import com.github.eirslett.maven.plugins.frontend.lib.IncrementalBuildExecutionDigest.Execution;
import org.junit.jupiter.api.Test;

import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;

import static com.github.eirslett.maven.plugins.frontend.lib.IncrementalMojoHelper.addTrackedFile;
import static java.lang.Runtime.getRuntime;
import static java.lang.String.format;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class IncrementalMojoHelperTest {

    @Test
    public void shouldProcessLargeFilesAndNotCauseMemoryAllocationSpikes() throws Exception {
        Path tempDir = Files.createTempDirectory("large-file-test");
        Path largeFile = tempDir.resolve("large-test-file.bin");
        final long largeFileSize = 2L * 1024 * 1024 * 1024; // 2GiB

        try {
            // Given
            try (RandomAccessFile file = new RandomAccessFile(largeFile.toFile(), "rw")) {
                file.setLength(largeFileSize);

                file.seek(0);
                file.write("header content".getBytes());
                file.seek(largeFileSize - 100);
                file.write("footer content".getBytes());
            }

            long memoryBefore = getRuntime().totalMemory() - getRuntime().freeMemory();
            Collection<Execution.File> files = new ArrayList<>();
            // When
            addTrackedFile(files, largeFile);
            long memoryAfter = getRuntime().totalMemory() - getRuntime().freeMemory();

            long memoryDifference = memoryAfter - memoryBefore;
            // Allow some tolerance for GC
            long maxAllowedIncrease = 32 * 1024 * 1024;

            // Then
            assertTrue(memoryDifference < maxAllowedIncrease,
                    format("Memory usage increased too much: %.2f MiB, expected less than %.2f MiB",
                            memoryDifference / (1024 * 1024.0),
                            maxAllowedIncrease / (1024 * 1024.0)));

            assertEquals(1, files.size());
            Execution.File processedFile = files.iterator().next();
            assertEquals(largeFile.toString(), processedFile.filename);
            assertEquals(largeFileSize, processedFile.byteLength);

        } finally {
            Files.deleteIfExists(largeFile);
            Files.deleteIfExists(tempDir);
        }
    }
}
