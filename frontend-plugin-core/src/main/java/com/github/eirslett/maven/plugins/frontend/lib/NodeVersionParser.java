package com.github.eirslett.maven.plugins.frontend.lib;

import com.google.common.annotations.VisibleForTesting;

import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toSet;

public class NodeVersionParser {

    @VisibleForTesting
    static final Set<String> UNUSUAL_VALID_VERSIONS;

    @VisibleForTesting
    static final Pattern VALID_VERSION_PATTERN = Pattern.compile("^v?\\d*\\.\\d*\\.\\d*$");

    static {
        UNUSUAL_VALID_VERSIONS = Stream.of(
                "latest",
                "latest-argon",
                "latest-boron",
                "latest-carbon",
                "latest-dubnium",
                "latest-erbium",
                "latest-fermium",
                "latest-gallium",
                "latest-hydrogen",
                "latest-iron",

                // future releases
                "latest-jod",
                "latest-krypton",
                "latest-lithium",
                "latest-magnesium",
                "latest-neon",
                "latest-oxygen",
                "latest-platinum",

                "latest-v0.10.x",
                "latest-v0.12.x",
                "latest-v10.x",
                "latest-v11.x",
                "latest-v12.x",
                "latest-v13.x",
                "latest-v14.x",
                "latest-v15.x",
                "latest-v16.x",
                "latest-v17.x",
                "latest-v18.x",
                "latest-v19.x",
                "latest-v20.x",
                "latest-v21.x",
                "latest-v22.x",
                "latest-v23.x",
                "latest-v24.x",
                "latest-v25.x",
                "latest-v26.x",
                "latest-v27.x",
                "latest-v28.x",
                "latest-v4.x",
                "latest-v5.x",
                "latest-v6.x",
                "latest-v7.x",
                "latest-v8.x",
                "latest-v9.x",
                "v0.10.16-isaacs-manual",
                "node-0.0.1",
                "node-0.0.2",
                "node-0.0.3",
                "node-0.0.4",
                "node-0.0.5",
                "node-0.0.6",
                "node-0.1.0",
                "node-0.1.1",
                "node-0.1.10",
                "node-0.1.11",
                "node-0.1.12",
                "node-0.1.13",
                "node-0.1.2",
                "node-0.1.3",
                "node-0.1.4",
                "node-0.1.5",
                "node-0.1.6",
                "node-0.1.7",
                "node-0.1.8",
                "node-0.1.9",
                "node-latest",
                "node-v0.1.100",
                "node-v0.1.101",
                "node-v0.1.102",
                "node-v0.1.103",
                "node-v0.1.104",
                "node-v0.1.14",
                "node-v0.1.15",
                "node-v0.1.16",
                "node-v0.1.17",
                "node-v0.1.18",
                "node-v0.1.19",
                "node-v0.1.20",
                "node-v0.1.21",
                "node-v0.1.22",
                "node-v0.1.23",
                "node-v0.1.24",
                "node-v0.1.25",
                "node-v0.1.26",
                "node-v0.1.27",
                "node-v0.1.28",
                "node-v0.1.29",
                "node-v0.1.30",
                "node-v0.1.31",
                "node-v0.1.32",
                "node-v0.1.33",
                "node-v0.1.90",
                "node-v0.1.91",
                "node-v0.1.92",
                "node-v0.1.93",
                "node-v0.1.94",
                "node-v0.1.95",
                "node-v0.1.96",
                "node-v0.1.97",
                "node-v0.1.98",
                "node-v0.1.99",
                "node-v0.10.14",
                "node-v0.2.0",
                "node-v0.2.1",
                "node-v0.2.2",
                "node-v0.2.3",
                "node-v0.2.4",
                "node-v0.2.5",
                "node-v0.2.6",
                "node-v0.3.0",
                "node-v0.3.1",
                "node-v0.3.2",
                "node-v0.3.3",
                "node-v0.3.4",
                "node-v0.3.5",
                "node-v0.3.6",
                "node-v0.3.7",
                "node-v0.3.8",
                "node-v0.4.0",
                "node-v0.4.1",
                "node-v0.4.10",
                "node-v0.4.11",
                "node-v0.4.12",
                "node-v0.4.2",
                "node-v0.4.3",
                "node-v0.4.4",
                "node-v0.4.5",
                "node-v0.4.6",
                "node-v0.4.7",
                "node-v0.4.8",
                "node-v0.4.9",
                "node-v0.4",
                "node-v0.5.0",
                "node-v0.6.1",
                "node-v0.6.10",
                "node-v0.6.11",
                "node-v0.6.12",
                "node-v0.6.13",
                "node-v0.6.2",
                "node-v0.6.3",
                "node-v0.6.4",
                "node-v0.6.5",
                "node-v0.6.6",
                "node-v0.6.7",
                "node-v0.6.8",
                "node-v0.6.9"
        ).collect(toSet());
    }

    public static boolean validateVersion(String version) {
        if (UNUSUAL_VALID_VERSIONS.contains(version)) {
            return true;
        }

        Matcher matcher = VALID_VERSION_PATTERN.matcher(version);
        return matcher.find();
    }

    public static String fixupMinorVersionErrors(String version) {
        version = version.toLowerCase(); // all the versions seem to be lower case

        if (UNUSUAL_VALID_VERSIONS.contains(version)) {
            return version;
        }

        if (!version.startsWith("v")) {
            version = "v" + version;
        }

        return version;
    }
}
