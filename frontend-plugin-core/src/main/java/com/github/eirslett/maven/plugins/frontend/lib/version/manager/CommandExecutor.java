package com.github.eirslett.maven.plugins.frontend.lib.version.manager;

import com.github.eirslett.maven.plugins.frontend.lib.InstallConfig;
import com.github.eirslett.maven.plugins.frontend.lib.ProcessExecutionException;
import com.github.eirslett.maven.plugins.frontend.lib.ProcessExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class CommandExecutor {

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final InstallConfig config;

    private String shell;
    private ProcessExecutor executor;

    private String fileToSource;
    private String pathToInclude;

    public CommandExecutor(InstallConfig config) {
        this.config = config;
    }

    public String executeAndCatchErrors(List<String> command) {
        ByteArrayOutputStream stdout = new ByteArrayOutputStream();
        ByteArrayOutputStream stderr = new ByteArrayOutputStream();
        if (shell != null) {
            command = getShellCommand(command);
        }

        try {
            int exitValue = execute(command, Collections.singletonList(pathToInclude), stdout, stderr);
            if (exitValue != 0) {
                logger.debug("Command finished with an error exit code {}", exitValue);
            }
        } catch (ProcessExecutionException e) {
            logger.debug("Command threw unexpectedly {}", stderr);
        }

        String output = parseOutput(stdout);
        logger.debug("Command output: `{}`\n error output `{}`", output, parseOutput(stderr));
        return output;
    }

    public String executeOrFail(List<String> command) {
        ByteArrayOutputStream stdout = new ByteArrayOutputStream();
        ByteArrayOutputStream stderr = new ByteArrayOutputStream();
        if (shell != null) {
            command = getShellCommand(command);
        }

        boolean hasExecutionFailed = false;
        try {
            int exitValue = execute(command, Collections.singletonList(pathToInclude), stdout, stderr);
            if (exitValue != 0) {
                hasExecutionFailed = true;
            }
        } catch (ProcessExecutionException e) {
            hasExecutionFailed = true;
        }

        if (hasExecutionFailed) {
            String commandText = String.join(" ", command);
            throw new RuntimeException(String.format("Execution of `%s` has failed" +
                "\nstdout: `%s`" +
                "\nstderr: `%s`", commandText, parseOutput(stdout), parseOutput(stderr)));
        } else {
            String output = parseOutput(stdout);
            logger.debug("Command output: `{}`", output);
            return output;
        }
    }

    public CommandExecutor withShell() {
        setCurrentUnixShell();
        return this;
    }

    public CommandExecutor withSourced(String file) {
        fileToSource = file;
        return this;
    }

    public CommandExecutor withPath(String path) {
        pathToInclude = path;
        return this;
    }

    public void initializeProcessExecutor(List<String> paths) {
        if (executor == null) {
            executor = new ProcessExecutor(
                config.getWorkingDirectory(),
                paths,
                Arrays.asList("echo", "running empty command..."),
                config.getPlatform(),
                Collections.emptyMap());
        }
    }

    private int execute(List<String> command, List<String> paths, ByteArrayOutputStream stdout, ByteArrayOutputStream stderr) throws ProcessExecutionException {
        initializeProcessExecutor(Collections.emptyList());
        return executor.execute(command, paths, logger, stdout, stderr);
    }

    private String parseOutput(ByteArrayOutputStream stream) {
        return stream.toString().trim();
    }

    private List<String> getShellCommand(List<String> commandParts) {
        String flatCommand = String.join(" ", commandParts);

        List<String> commandWithSourcedProfile = new ArrayList<>();
        commandWithSourcedProfile.add(shell);
        commandWithSourcedProfile.add("-c");

        if (fileToSource != null) {
            String sourceCommand = String.format(". %s", fileToSource);
            commandWithSourcedProfile.add(String.format("%s; %s", sourceCommand, flatCommand));
        } else {
            commandWithSourcedProfile.add(flatCommand);
        }

        return commandWithSourcedProfile;
    }

    private void setCurrentUnixShell() {
        if (shell != null) return;

        String shellFromEV = System.getenv("SHELL");
        if (shellFromEV == null || shellFromEV.isEmpty()) {
            logger.debug("SHELL variable couldn't be found. Falling back on reading the variable from /bin/sh.");
            ByteArrayOutputStream stdout = new ByteArrayOutputStream();
            try {
                execute(Arrays.asList("/bin/sh", "-c", "echo $SHELL"), Collections.emptyList(), stdout, stdout);
                String shellFromSh = parseOutput(stdout);
                logger.debug("SHELL from /bin/sh: {}", shellFromSh);

                if (shellFromSh.isEmpty()) {
                    throw new RuntimeException("SHELL is not available in environment variables. Please provide the value of $SHELL with your preferred shell.");
                } else {
                    shell = shellFromSh;
                }
            } catch (ProcessExecutionException e) {
                throw new RuntimeException(e);
            }
        } else {
            shell = shellFromEV;
        }
    }
}
