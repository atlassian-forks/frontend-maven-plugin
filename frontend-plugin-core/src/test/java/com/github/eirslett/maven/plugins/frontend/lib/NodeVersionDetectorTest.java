package com.github.eirslett.maven.plugins.frontend.lib;

import org.junit.jupiter.api.Test;

import static com.github.eirslett.maven.plugins.frontend.lib.NodeVersionDetector.readNvmrcFileLines;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class NodeVersionDetectorTest {

    @Test
    public void testNvmrcFileParsing_shouldWorkWithACommentWithWhiteSpaceOnTheSameLineAsTheVersion() {
        assertEquals("v1.0.0", readNvmrcFileLines(singletonList("v1.0.0\t //\t comment")).get());
    }

    @Test
    public void testNvmrcFileParsing_shouldIgnoreCommentOnlyLines() {
        assertEquals("v1.0.0", readNvmrcFileLines(asList(
                "#comment",
                " ! comment",
                "\t/\tcomment",
                "v1.0.0",
                "#comment",
                " ! comment",
                "\t/\tcomment"
        )).get());
    }
}
