package com.github.eirslett.maven.plugins.frontend.lib;

import com.github.eirslett.maven.plugins.frontend.lib.IncrementalBuildExecutionDigest.Execution;
import com.github.eirslett.maven.plugins.frontend.lib.IncrementalBuildExecutionDigest.ExecutionCoordinates;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import static com.github.eirslett.maven.plugins.frontend.lib.IncrementalMojoHelper.addTrackedFile;
import static com.github.eirslett.maven.plugins.frontend.lib.IncrementalMojoHelper.addTrackedPath;
import static java.lang.Runtime.getRuntime;
import static java.lang.String.format;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class IncrementalMojoHelperTest {

    @TempDir
    Path tempDir;

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

    @Test
    void shouldUseSeparateIncrementalWorkingDirectory() throws Exception {
        Path workingDirectory = Files.createDirectories(tempDir.resolve("working"));
        Files.write(workingDirectory.resolve("ignored.js"), "ignored".getBytes());
        Path incrementalWorkingDirectory = Files.createDirectories(tempDir.resolve("incremental"));
        Path tracked = Files.write(incrementalWorkingDirectory.resolve("tracked.js"), "tracked".getBytes());
        IncrementalMojoHelper helper = new IncrementalMojoHelper(
                "true",
                new ExecutionCoordinates("yarn", "execution", "generate-resources"),
                tempDir.toFile(),
                incrementalWorkingDirectory.toFile(),
                null,
                null);

        Set<Execution.File> files = helper.createFilesDigest();

        assertEquals(1, files.size());
        assertEquals(tracked.toString(), files.iterator().next().filename);
    }

    @Test
    void shouldRecursivelyProcessTriggerDirectories() throws Exception {
        Path nested = Files.createDirectories(tempDir.resolve("nested"));
        Path tracked = Files.write(nested.resolve("tracked.js"), "tracked".getBytes());
        Files.write(nested.resolve("ignored.txt"), "ignored".getBytes());
        Path excluded = Files.createDirectories(tempDir.resolve("excluded"));
        Files.write(excluded.resolve("excluded.js"), "excluded".getBytes());
        Set<String> excludedFilenames = new HashSet<>();
        excludedFilenames.add("excluded");
        Set<Execution.File> files = new HashSet<>();

        addTrackedPath(files, tempDir, excludedFilenames);

        assertEquals(1, files.size());
        assertEquals(tracked.toString(), files.iterator().next().filename);
    }
}
