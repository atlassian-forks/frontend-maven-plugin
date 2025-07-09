package com.github.eirslett.maven.plugins.frontend.lib;

import com.github.eirslett.maven.plugins.frontend.lib.NodeVersionHelper.NodeVersionComparator;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Optional;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.StatusLine;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;

import static com.github.eirslett.maven.plugins.frontend.lib.NodeVersionHelper.UNUSUAL_VALID_VERSIONS;
import static com.github.eirslett.maven.plugins.frontend.lib.NodeVersionHelper.VALID_VERSION_PATTERN;
import static com.github.eirslett.maven.plugins.frontend.lib.NodeVersionHelper.getDownloadableVersion;
import static com.github.eirslett.maven.plugins.frontend.lib.NodeVersionHelper.validateVersion;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.any;


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

    @Test
    public void testLooselyDefinedMajorVersionsWithPrefix_shouldBeValid() {
        assertTrue(validateVersion("v14"));
    }

    @Test
    public void testGetDownloadableVersion_shouldFallbackWhenNetworkFails() {
        // Force the cache to be empty to trigger a network request
        NodeVersionHelper.nodeVersions.set(Optional.empty());

        // Create a mock HTTP client that always fails
        try (MockedStatic<HttpClients> mockedHttpClients = mockStatic(HttpClients.class)) {
            CloseableHttpClient mockClient = mock(CloseableHttpClient.class);

            mockedHttpClients.when(HttpClients::createDefault).thenReturn(mockClient);

            try {
                doThrow(new IOException("Network failure")).when(mockClient).execute(any(HttpGet.class));
            } catch (Exception e) {
                // This won't be reached, just satisfying the compiler
            }

            String result = getDownloadableVersion("12");
            assertEquals("v12", result);
        }
    }

    @Test
    public void testGetDownloadableVersion_shouldUseLatestVersionWhenFound() throws Exception {
        // Prepare mock response data
        String jsonResponse = "[{\"version\":\"v10.24.1\"},{\"version\":\"v12.22.12\"},{\"version\":\"v12.22.11\"},{\"version\":\"v14.21.3\"}]";

        try (MockedStatic<HttpClients> mockedHttpClients = mockStatic(HttpClients.class)) {
            CloseableHttpClient mockClient = mock(CloseableHttpClient.class);
            CloseableHttpResponse mockResponse = mock(CloseableHttpResponse.class);
            StatusLine mockStatusLine = mock(StatusLine.class);
            HttpEntity mockEntity = mock(HttpEntity.class);
            Header mockHeader = mock(Header.class);

            mockedHttpClients.when(HttpClients::createDefault).thenReturn(mockClient);
            when(mockClient.execute(any(HttpGet.class))).thenReturn(mockResponse);
            when(mockResponse.getStatusLine()).thenReturn(mockStatusLine);
            when(mockStatusLine.getStatusCode()).thenReturn(200);
            when(mockResponse.getEntity()).thenReturn(mockEntity);
            when(mockEntity.getContentType()).thenReturn(mockHeader);
            when(mockHeader.getValue()).thenReturn("application/json");
            when(mockEntity.getContent()).thenReturn(new ByteArrayInputStream(jsonResponse.getBytes()));

            // Clear cache to force network request
            NodeVersionHelper.nodeVersions.set(Optional.empty());

            String result = getDownloadableVersion("12");
            assertEquals("v12.22.12", result);
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
