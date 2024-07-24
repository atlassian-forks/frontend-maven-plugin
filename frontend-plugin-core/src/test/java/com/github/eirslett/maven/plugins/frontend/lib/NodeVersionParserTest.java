package com.github.eirslett.maven.plugins.frontend.lib;

import org.junit.jupiter.api.Test;

import static com.github.eirslett.maven.plugins.frontend.lib.NodeVersionParser.UNUSUAL_VALID_VERSIONS;
import static com.github.eirslett.maven.plugins.frontend.lib.NodeVersionParser.VALID_VERSION_PATTERN;
import static com.github.eirslett.maven.plugins.frontend.lib.NodeVersionParser.fixupMinorVersionErrors;
import static com.github.eirslett.maven.plugins.frontend.lib.NodeVersionParser.validateVersion;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class NodeVersionParserTest {

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
        assertEquals("v1.0.0", fixupMinorVersionErrors("1.0.0"));
    }

    @Test
    public void testInvalidCase_shouldBeFixed() {
        assertEquals("v1.0.0", fixupMinorVersionErrors("V1.0.0"));
    }
}
