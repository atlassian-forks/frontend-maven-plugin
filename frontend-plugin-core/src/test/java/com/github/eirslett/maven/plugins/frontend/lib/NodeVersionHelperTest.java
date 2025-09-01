package com.github.eirslett.maven.plugins.frontend.lib;

import com.github.eirslett.maven.plugins.frontend.lib.NodeVersionHelper.NodeVersionComparator;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import static com.github.eirslett.maven.plugins.frontend.lib.NodeVersionHelper.UNUSUAL_VALID_VERSIONS;
import static com.github.eirslett.maven.plugins.frontend.lib.NodeVersionHelper.VALID_VERSION_PATTERN;
import static com.github.eirslett.maven.plugins.frontend.lib.NodeVersionHelper.getDownloadableVersion;
import static com.github.eirslett.maven.plugins.frontend.lib.NodeVersionHelper.validateVersion;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class NodeVersionHelperTest {

    @Test
    public void testUnusualPatterns_shouldNotMatchThePattern_toKeepTheListSmall() {
        UNUSUAL_VALID_VERSIONS.forEach(version -> {
            assertFalse(VALID_VERSION_PATTERN.matcher(version).find());
        });
    }

    @Test
    public void testUnusualPreviousVersions_shouldBeTreatedAsValid() {
        UNUSUAL_VALID_VERSIONS.forEach(version -> {
            assertTrue(validateVersion(version));
        });
    }

    @Test
    public void testVersionsMissingV_shouldBeFixed() {
        assertEquals("v1.0.0", getDownloadableVersion("1.0.0"));
    }

    @Test
    public void testInvalidCase_shouldBeFixed() {
        assertEquals("v1.0.0", getDownloadableVersion("V1.0.0"));
    }

    @Test
    public void testLooselyDefinedMajorVersions_shouldBeValid() {
        assertTrue(validateVersion("12"));
    }

    @Disabled("We need to figure out a better way than blocking on an HTTP request near the start")
    @Test
    public void testGetDownloadableVersion_shouldGiveUsTheLatestDownloadableVersion_forAGivenLooselyDefinedMajorVersion() {
        // Using Node 12 since there shouldn't be anymore releases
        assertEquals("v12.22.12", getDownloadableVersion("12"));
    }

    @Test
    public void testFindHighestMatchingInstalledVersion_returnsHighestVersion() {
        Path mockDir = Mockito.mock(Path.class);

        Path[] versions = {
            Paths.get("v18.0.0"),
            Paths.get("v18.9.9"),
            Paths.get("v19.5.0"),
            Paths.get("v20.0.0"),
            Paths.get("v20.1.0"),
        };

        try (MockedStatic<Files> filesMock = Mockito.mockStatic(Files.class)) {
            filesMock.when(() -> Files.exists(mockDir)).thenReturn(true);
            filesMock.when(() -> Files.list(mockDir)).thenReturn(Stream.of(versions));
            for (Path version : versions) {
                filesMock.when(() -> Files.isDirectory(version)).thenReturn(true);
            }

            String result = NodeVersionHelper.findHighestMatchingInstalledVersion(mockDir, "v18");
            assertEquals("v18.9.9", result);
        }
    }

    @Test
    public void testFindHighestMatchingInstalledVersion_returnsNullIfNoMatch() {
        Path mockDir = Mockito.mock(Path.class);

        Path[] versions = {
                Paths.get("v19.5.0"),
                Paths.get("v20.0.0"),
                Paths.get("v20.1.0"),
        };

        try (MockedStatic<Files> filesMock = Mockito.mockStatic(Files.class)) {
            filesMock.when(() -> Files.exists(mockDir)).thenReturn(true);
            filesMock.when(() -> Files.list(mockDir)).thenReturn(Stream.of(versions));
            for (Path version : versions) {
                filesMock.when(() -> Files.isDirectory(version)).thenReturn(true);
            }

            String result = NodeVersionHelper.findHighestMatchingInstalledVersion(mockDir, "v18");
            assertNull(result);
        }
    }

    @Test
    public void testFindHighestMatchingInstalledVersion_returnsNullIfDirDoesNotExist() {
        Path mockDir = Mockito.mock(Path.class);

        try (MockedStatic<Files> filesMock = Mockito.mockStatic(Files.class)) {
            filesMock.when(() -> Files.exists(mockDir)).thenReturn(false);

            String result = NodeVersionHelper.findHighestMatchingInstalledVersion(mockDir, "v20");
            assertNull(result);
        }
    }

    @Test
    public void testNodeVersionComparator_shouldCompareByNumbers() {
        assertEquals(-1, new NodeVersionComparator().compare("v1.1.9", "v1.1.10"));
    }

    @Test
    public void testNodeVersionComparator_shouldHandleEqualVersions() {
        assertEquals(0, new NodeVersionComparator().compare("v1.1.1", "v1.1.1"));
    }
}
