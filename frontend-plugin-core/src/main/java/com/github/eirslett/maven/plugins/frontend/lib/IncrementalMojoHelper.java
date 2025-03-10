package com.github.eirslett.maven.plugins.frontend.lib;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.eirslett.maven.plugins.frontend.lib.AtlassianDevMetricsReporter.Timer;
import com.github.eirslett.maven.plugins.frontend.lib.IncrementalBuildExecutionDigest.Execution;
import com.github.eirslett.maven.plugins.frontend.lib.IncrementalBuildExecutionDigest.Execution.Runtime;
import com.github.eirslett.maven.plugins.frontend.lib.IncrementalBuildExecutionDigest.ExecutionCoordinates;
import org.apache.commons.codec.digest.MurmurHash3;
import org.codehaus.plexus.util.StringUtils;
import org.slf4j.Logger;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import static com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES;
import static com.fasterxml.jackson.databind.SerializationFeature.INDENT_OUTPUT;
import static com.github.eirslett.maven.plugins.frontend.lib.IncrementalBuildExecutionDigest.CURRENT_DIGEST_VERSION;
import static java.lang.String.format;
import static java.time.Instant.now;
import static java.util.Arrays.asList;
import static java.util.Collections.emptySet;
import static java.util.Objects.isNull;
import static java.util.Objects.requireNonNull;
import static java.util.Optional.empty;
import static org.slf4j.LoggerFactory.getLogger;

public class IncrementalMojoHelper {
    private static final String MVN_LIST_SEPARATOR = ",";
    public static final String DEFAULT_EXCLUDED_FILENAMES =
                    ".node" + MVN_LIST_SEPARATOR +
                    "node_modules" + MVN_LIST_SEPARATOR +
                    "lcov-report" + MVN_LIST_SEPARATOR +
                    "coverage" + MVN_LIST_SEPARATOR +
                    "screenshots" + MVN_LIST_SEPARATOR +
                    "build" + MVN_LIST_SEPARATOR +
                    "dist" + MVN_LIST_SEPARATOR +
                    "target" + MVN_LIST_SEPARATOR +
                    ".idea" + MVN_LIST_SEPARATOR +
                    ".history" + MVN_LIST_SEPARATOR +
                    "tmp" + MVN_LIST_SEPARATOR +
                    ".settings" + MVN_LIST_SEPARATOR +
                    ".vscode" + MVN_LIST_SEPARATOR +
                    ".git" + MVN_LIST_SEPARATOR +
                    "dependency-reduced-pom.xml" + MVN_LIST_SEPARATOR +
                    ".flattened-pom.xml";

    private static final Logger log = getLogger(IncrementalMojoHelper.class);
    private static final String SEE_DEBUG_LOGS_MSG = " See the Maven debug logs (run with -X) for more info";

    private static ObjectMapper objectMapper;

    private final ExecutionCoordinates coordinates;
    private final File targetDirectory;
    private final File workingDirectory;
    private final boolean isActive;
    private final Set<File> triggerFiles;
    private final Set<String> excludedFilenames;

    private IncrementalBuildExecutionDigest digest;
    private Optional<Instant> startTimeForSavedTimeUpdate = empty();

    public IncrementalMojoHelper(String activationFlag, ExecutionCoordinates coordinates, File targetDirectory, File workingDirectory, Set<File> triggerFiles, Set<String> excludedFilenames) {
        this.coordinates = requireNonNull(coordinates, "coordinates");
        this.targetDirectory = requireNonNull(targetDirectory, "targetDirectory");
        this.workingDirectory = requireNonNull(workingDirectory, "workingDirectory");
        this.triggerFiles = isNull(triggerFiles) ? emptySet() : triggerFiles;
        this.excludedFilenames = isNull(excludedFilenames) ? emptySet() : excludedFilenames;

        this.isActive = "true".equals(activationFlag);
    }

    public boolean incrementalEnabled() {
        return isActive;
    }

    public boolean canBeSkipped(String arguments, Optional<Runtime> runtime, Map<String, String> suppliedEnvVars, String artifactId, String forkVersion) {
        Timer timer = new Timer();
        boolean failed = false;

        if (!isActive) {
            return false;
        }

        if (!runtime.isPresent()) {
            log.warn("Failed to do incremental compilation because the runtime version couldn't be fetched." + SEE_DEBUG_LOGS_MSG);
            return false;
        }

        try {
            try {
                File digestFileLocation = getDigestFile();
                if (digestFileLocation.exists()) {
                    digest = readDigest(digestFileLocation);
                }
            } catch (FileNotFoundException exception) {
                log.debug("No existing digest file", exception);
            }

            if (isNull(digest)) {
                digest = new IncrementalBuildExecutionDigest(CURRENT_DIGEST_VERSION, new HashMap<>());
            }

            boolean digestVersionsMatch = Objects.equals(digest.digestVersion, CURRENT_DIGEST_VERSION);

            Execution thisExecution = new Execution(
                    arguments,
                    getAllEnvVars(suppliedEnvVars),
                    createFilesDigest(),
                    runtime.get());

            // The clock starts now
            startTimeForSavedTimeUpdate = Optional.of(now());

            boolean canSkipExecution = false;
            Execution previousExecution = digest.executions.get(coordinates);
            if (digestVersionsMatch && previousExecution != null) {
                // patch it forward so we don't lose it and so the equality check works
                thisExecution.millisecondsSaved = previousExecution.millisecondsSaved;
                canSkipExecution = previousExecution.equals(thisExecution);
                if (canSkipExecution) {
                    // Clear the time, we're about to skip execution so it'd report lower than it actually would save
                    startTimeForSavedTimeUpdate = Optional.empty();

                    log.info("Saving {} by skipping execution of frontend-maven-plugin! No changes were detected. If it should " +
                            "have executed, adjust the triggerFiles and excludedFilenames in the configuration.", Duration.ofMillis(previousExecution.millisecondsSaved));
                } else {
                    log.info("Didn't do incremental compilation because a change was detected for executionId:  {} in artifactId: {}" + SEE_DEBUG_LOGS_MSG, coordinates.id, artifactId);

                    if (log.isDebugEnabled()) {
                        String argumentsDifference = StringUtils.difference(previousExecution.arguments, thisExecution.arguments);
                        String envVarDifference = StringUtils.difference(previousExecution.environmentVariables.toString(), thisExecution.environmentVariables.toString());
                        String runtimeDifference = StringUtils.difference(previousExecution.runtime.toString(), thisExecution.runtime.toString());

                        Set<Execution.File> newFiles = new HashSet<>(thisExecution.files);
                        newFiles.removeAll(previousExecution.files);
                        Set<Execution.File> goneFiles = new HashSet<>(previousExecution.files);
                        goneFiles.removeAll(thisExecution.files);

                        if (!argumentsDifference.trim().isEmpty()) {
                            log.debug("Difference in arguments was: <{}> previously: <{}>, currently <{}>",  argumentsDifference, previousExecution.arguments, thisExecution.arguments);
                        }
                        if (!envVarDifference.trim().isEmpty()) {
                            log.debug("Difference in environment variables was: <{}> previously: <{}>, currently <{}>",  envVarDifference, previousExecution.environmentVariables, thisExecution.environmentVariables);
                        }
                        if (!runtimeDifference.trim().isEmpty()) {
                            log.debug("Difference in runtime was: <{}> previously: <{}>, currently <{}>",  runtimeDifference, previousExecution.runtime, thisExecution.runtime);
                        }
                        if (!newFiles.isEmpty()) {
                            log.debug("Some files are \"new\" (may have changed meta-data), there were: {}",  newFiles);
                        }
                        if (!goneFiles.isEmpty()) {
                            log.debug("Some files are \"gone\" (may have changed meta-data), there were: {}",  goneFiles);
                        }
                    }
                }
            }

            digest.digestVersion = CURRENT_DIGEST_VERSION;
            digest.executions.put(coordinates, thisExecution);

            return canSkipExecution;
        } catch (Exception exception) {
            log.error("Failure while determining if an incremental build is possible." + SEE_DEBUG_LOGS_MSG);
            log.debug("Failure while determining if an incremental build is possible, because: ", exception);
            return false;
        } finally {
            timer.stop("execute.incremental.check", artifactId, forkVersion, "",
                    new HashMap<String, String>() {{
                        put("failed", Boolean.toString(failed));
            }});
        }
    }

    public void acceptIncrementalBuildDigest() {
        if (!isActive) {
            return;
        }

        startTimeForSavedTimeUpdate.ifPresent(instant ->
                digest.executions.get(coordinates).millisecondsSaved =
                        Duration.between(instant, now()).toMillis());

        try {
            log.debug("Accepting the incremental build digest after a successful execution");
            File digestFile = getDigestFile();
            if (digestFile.exists()) {
                if (!digestFile.delete()) {
                    log.warn("Failed to delete the previous incremental build digest. You'll have to delete it manually @ {}", digestFile.getAbsolutePath());
                }
            }

            digestFile.getParentFile().mkdirs();
            saveDigest(digest);
        } catch (Exception exception) {
            log.warn("Failed to save the incremental build digest." + SEE_DEBUG_LOGS_MSG);
            log.debug("Failed to save the incremental build digest, because: ", exception);
        }
    }

    static class IncrementalVisitor extends SimpleFileVisitor<Path> {
        private final Set<Execution.File> files;
        private final Set<String> excludedFilenames;

        public IncrementalVisitor(Set<Execution.File> files, Set<String> excludedFilenames) {
            this.files = files;
            this.excludedFilenames = excludedFilenames;
        }

        private static final Set<String> DIGEST_EXTENSIONS = new HashSet<>(asList(
                // JS
                "js",
                "jsx",
                "cjs",
                "mjs",
                "ts",
                "tsx",
                // patches
                "patch",
                // CSS
                "css",
                "scss",
                "sass",
                "less",
                "styl",
                "stylus",
                // templates
                "ejs",
                "hbs",
                "handlebars",
                "pug",
                "soy",
                "html",
                "vm",
                "vmd",
                "vtl",
                "ftl",
                // config
                "graphql",
                "json",
                "xml",
                "yaml",
                "yml",
                "csv",
                "lock",
                // Images
                "apng",
                "png",
                "jpg",
                "jpeg",
                "gif",
                "webp",
                "svg",
                "ico",
                "bmp",
                "tiff",
                "tif",
                "avif",
                "eps",
                // Fonts
                "ttf",
                "otf",
                "woff",
                "woff2",
                "eot",
                "sfnt",
                // Audio and Video
                "mp3",
                "mp4",
                "webm",
                "wav",
                "flac",
                "aac",
                "ogg",
                "oga",
                "opus",
                "m4a",
                "m4v",
                "mov",
                "avi",
                "wmv",
                "flv",
                "mkv",
                "flac"
        ));

        // Files that are to be included in the digest but are not of the above extensions
        private static final Set<String> DIGEST_FILES = new HashSet<>(asList(
                ".parcelrc",
                ".babelrc",
                ".eslintrc",
                ".eslintignore",
                ".prettierrc",
                ".prettierrc.js", // this would otherwise get skpped over
                ".prettierignore",
                ".stylelintrc",
                ".stylelintignore",
                ".browserslistrc",
                ".npmrc",
                ".swcrc"
        ));

        @Override
        public FileVisitResult preVisitDirectory(Path file, BasicFileAttributes attrs) {
            String filename = file.getFileName().toString();
            if (excludedFilenames.contains(filename)) {
                return FileVisitResult.SKIP_SUBTREE;
            }


            return FileVisitResult.CONTINUE;
        }

        /**
         * PERF NOTES:
         * <ul>
         *     <li>Yes, we're mixing the walking with the work,  but due to the
         *     underlying library calls, we're 300ms better off in JSM DC</li>
         *     <li>Yes, this removes parallelism, but even in JSM DC (worst case),
         *     it's negligible, while for Stash and Jira DC it's faster to be single
         *     threaded. Combined with these all usually being run in Maven
         *     reactors, this is more likely to pay off by allowing for better
         *     overall parallelism</li>
         * </ul>
         */
        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
            String fileName = file.getFileName().toString();

            if (excludedFilenames.contains(fileName)) {
                return FileVisitResult.CONTINUE;
            }

            if (DIGEST_FILES.contains(fileName) ||
                    DIGEST_EXTENSIONS.contains(getFileExtension(fileName))) {
                addTrackedFile(files, file);
            }

            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult visitFileFailed(Path path, IOException exception) throws IOException {
            log.debug("Failed to visit {}", path, exception);
            return super.visitFileFailed(path, exception);
        }

        private static String getFileExtension(String fileName) {
            int dotIndex = fileName.lastIndexOf('.');
            if (dotIndex > 0 // skip over dot-files like .babelrc
                    // check if the '.' is the last character == no extension
                    && dotIndex < fileName.length() - 1) {
                return fileName.substring(dotIndex + 1);
            } else {
                return null;
            }
        }
    }

    private Set<Execution.File> createFilesDigest() throws IOException {
        final Set<Execution.File> files = new HashSet<>();

        IncrementalVisitor visitor = new IncrementalVisitor(files, excludedFilenames);
        Files.walkFileTree(workingDirectory.toPath(), visitor);
        triggerFiles.forEach(file -> addTrackedFile(files, file.toPath()));

        return files;
    }

    private static void addTrackedFile(Collection<Execution.File> files, Path file) {
        try {
            byte[] fileBytes = Files.readAllBytes(file);
            // Requirements for hash function: 1 - single byte change is
            // highly likely to result in a different hash, 2 - fast, baby fast!
            long[] hash = MurmurHash3.hash128x64(fileBytes);
            String hashString = Arrays.toString(hash);
            files.add(new Execution.File(file.toString(), fileBytes.length, hashString));
        } catch (IOException exception) {
            throw new RuntimeException(format("Failed to read file: %s", file), exception);
        }
    }

    private static Map<String, String> getAllEnvVars(Map<String, String> userDefinedEnvVars) {
        final  Map<String, String> effectiveEnvVars = new HashMap<>();

        List<String> defaultEnvVars = asList(
                "NODE_ENV",
                "BABEL_ENV",
                "OS",
                "OS_VERSION",
                "OS_ARCH",
                "OS_NAME",
                "OS_FAMILY"
        );
        defaultEnvVars.forEach(envVarKey -> {
            String envVarValue = System.getenv(envVarKey);
            effectiveEnvVars.put(envVarKey, nullStringIsEmpty(envVarValue));
        });

        if (userDefinedEnvVars != null) {
            // These would override our defaults
            effectiveEnvVars.putAll(userDefinedEnvVars);
        }

        return effectiveEnvVars;
    }

    /**
     * Most stuff treats empty and unset as the same
     */
    private static String nullStringIsEmpty(String string) {
        if (isNull(string)) {
            return "";
        }
        return string;
    }

    /**
     * This is expensive to init (200ms), should only do it once and as needed
     */
    static ObjectMapper getObjectMapper() {
        if (isNull(objectMapper)) {
            objectMapper = new ObjectMapper()
                // Allow for reading without blowing up
                .configure(FAIL_ON_UNKNOWN_PROPERTIES, false)
                // for serialisation performance
                .configure(INDENT_OUTPUT, false);
        }
        return objectMapper;
    }

    private void saveDigest(IncrementalBuildExecutionDigest digest) throws IOException {
        getObjectMapper().writeValue(getDigestFile(), digest);
    }

    private IncrementalBuildExecutionDigest readDigest(File digest) throws IOException {
        return getObjectMapper().readValue(digest, IncrementalBuildExecutionDigest.class);
    }

    private File getDigestFile() {
        return new File(targetDirectory, "frontend-maven-plugin-incremental-build-digest.json");
    }
}
