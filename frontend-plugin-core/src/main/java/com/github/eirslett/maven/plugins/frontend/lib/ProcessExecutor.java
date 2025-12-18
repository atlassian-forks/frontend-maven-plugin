package com.github.eirslett.maven.plugins.frontend.lib;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteException;
import org.apache.commons.exec.ExecuteStreamHandler;
import org.apache.commons.exec.ExecuteWatchdog;
import org.apache.commons.exec.Executor;
import org.apache.commons.exec.LogOutputStream;
import org.apache.commons.exec.PumpStreamHandler;
import org.apache.commons.exec.ShutdownHookProcessDestroyer;
import org.apache.commons.exec.StreamPumper;
import org.slf4j.Logger;

import javax.annotation.Nonnull;

import static java.util.Objects.requireNonNull;

public final class ProcessExecutor {
    private final static String PATH_ENV_VAR = "PATH";

    private Map<String, String> environment;
    private CommandLine commandLine;
    private final Executor executor;
    private final Platform platform;

    public ProcessExecutor(File workingDirectory, List<String> paths, List<String> command, Platform platform, Map<String, String> additionalEnvironment){
        this(workingDirectory, paths, command, platform, additionalEnvironment, 0);
    }

    public ProcessExecutor(File workingDirectory, List<String> paths, List<String> command, Platform platform, Map<String, String> additionalEnvironment, long timeoutInSeconds) {
        this.platform = platform;
        this.environment = createEnvironment(paths, platform, additionalEnvironment);
        this.commandLine = createCommandLine(command);
        this.executor = createExecutor(workingDirectory, timeoutInSeconds);
    }

    public String executeAndGetResult(final Logger logger) throws ProcessExecutionException {
        ByteArrayOutputStream stdout = new ByteArrayOutputStream();
        ByteArrayOutputStream stderr = new ByteArrayOutputStream();

        int exitValue = execute(logger, stdout, stderr);
        if (exitValue == 0) {
            return stdout.toString().trim();
        } else {
            throw new ProcessExecutionException(stdout + " " + stderr);
        }
    }

    public int executeAndRedirectOutput(final Logger logger) throws ProcessExecutionException {
        OutputStream stdout = new LoggerOutputStream(logger, 0);
        return execute(logger, stdout, stdout);
    }

    public int execute(List<String> command, List<String> paths, final Logger logger, final OutputStream stdout, final OutputStream stderr)
        throws ProcessExecutionException {
        this.commandLine = createCommandLine(command);
        this.environment = createEnvironment(paths, platform, Collections.emptyMap());
        return execute(logger, stdout, stderr);
    }

    public int execute(final Logger logger, final OutputStream stdout, final OutputStream stderr)
            throws ProcessExecutionException {
        logger.debug("Executing command line {}", commandLine);
        try {
            // This will normally include the current maven module in multithreaded builds
            // Kept deliberately short as Maven logs are already very wide
            String executorName = Thread.currentThread().getName() + "-FE";
            ExecuteStreamHandler streamHandler = new NamedPumpStreamHandler(stdout, stderr, executorName);
            executor.setStreamHandler(streamHandler);

            int exitValue = executor.execute(commandLine, environment);
            logger.debug("Exit value {}", exitValue);

            return exitValue;
        } catch (ExecuteException e) {
            if (executor.getWatchdog() != null && executor.getWatchdog().killedProcess()) {
                throw new ProcessExecutionException("Process killed after timeout");
            }
            throw new ProcessExecutionException(e);
        } catch (IOException e) {
            throw new ProcessExecutionException(e);
        }
    }

    private CommandLine createCommandLine(List<String> command) {
        CommandLine commmandLine = new CommandLine(command.get(0));

        for (int i = 1;i < command.size();i++) {
            String argument = command.get(i);
            commmandLine.addArgument(argument, false);
        }

        return commmandLine;
    }

    private Map<String, String> createEnvironment(final List<String> paths, final Platform platform, final Map<String, String> additionalEnvironment) {
        final Map<String, String> environment = new HashMap<>(System.getenv());

        if (additionalEnvironment != null) {
            environment.putAll(additionalEnvironment);
        }

        if (platform.isWindows()) {
            for (final Map.Entry<String, String> entry : environment.entrySet()) {
                final String pathName = entry.getKey();
                if (PATH_ENV_VAR.equalsIgnoreCase(pathName)) {
                    final String pathValue = entry.getValue();
                    environment.put(pathName, extendPathVariable(pathValue, paths));
                }
            }
        } else {
            final String pathValue = environment.get(PATH_ENV_VAR);
            environment.put(PATH_ENV_VAR, extendPathVariable(pathValue, paths));
        }

        return environment;
    }

    private String extendPathVariable(final String existingValue, final List<String> paths) {
        final StringBuilder pathBuilder = new StringBuilder();
        for (final String path : paths) {
            pathBuilder.append(path).append(File.pathSeparator);
        }
        if (existingValue != null) {
            pathBuilder.append(existingValue).append(File.pathSeparator);
        }
        return pathBuilder.toString();
    }

    private Executor createExecutor(File workingDirectory, long timeoutInSeconds) {
        DefaultExecutor executor = new DefaultExecutor();
        executor.setWorkingDirectory(workingDirectory);
        executor.setProcessDestroyer(new ShutdownHookProcessDestroyer());   // Fixes #41

        if (timeoutInSeconds > 0) {
            executor.setWatchdog(new ExecuteWatchdog(timeoutInSeconds * 1000));
        }

        return executor;
    }

    private static class LoggerOutputStream extends LogOutputStream {
        private final Logger logger;

        LoggerOutputStream(Logger logger, int logLevel) {
            super(logLevel);
            this.logger = logger;
        }

        @Override
        public final void flush() {
            // buffer processing on close() only
        }

        @Override
        protected void processLine(final String line, final int logLevel) {
            logger.info(line);
        }
    }

    // Minimal replacement for PumpStreamHandler that names its threads
    private static final class NamedPumpStreamHandler extends PumpStreamHandler {
        private final String name;

        public NamedPumpStreamHandler(final OutputStream out, final OutputStream err, @Nonnull final String name) {
            super(out, err, null);
            this.name = requireNonNull(name, "name");
        }

        /**
         * Should match the parent implementation, but need to override the thread name.
         */
        @Override
        protected Thread createPump(final InputStream is, final OutputStream os, final boolean closeWhenExhausted) {
            final Thread result = new Thread(new StreamPumper(is, os, closeWhenExhausted), name);
            result.setDaemon(true);
            return result;
        }
    }
}
