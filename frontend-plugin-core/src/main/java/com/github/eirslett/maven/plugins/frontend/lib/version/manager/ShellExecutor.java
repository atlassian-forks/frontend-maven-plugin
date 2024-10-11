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

public class ShellExecutor {

    final Logger logger = LoggerFactory.getLogger(getClass());
    final InstallConfig config;

    public ShellExecutor(InstallConfig config) {
        this.config = config;
    }

    public String executeAndCatchErrors(List<String> command) {
        ByteArrayOutputStream stdout = new ByteArrayOutputStream();
        ByteArrayOutputStream stderr = new ByteArrayOutputStream();
        List<String> profiledShellCommand = getShellCommand(command);

        try {
            int exitValue = execute(profiledShellCommand, stdout, stderr);
            if (exitValue != 0) {
                logger.debug("Command finished with error exit code {}, error output `{}`", exitValue, parseOutput(stderr));
            }
        } catch (ProcessExecutionException e) {
            logger.debug("Command threw unexpectedly, error output: `{}`", parseOutput(stderr));
        }

        String output = parseOutput(stdout);
        logger.debug("Command output: `{}`", output);
        return output;
    }

    public String executeOrFail(List<String> command) {
        ByteArrayOutputStream stdout = new ByteArrayOutputStream();
        ByteArrayOutputStream stderr = new ByteArrayOutputStream();
        List<String> profiledShellCommand = getShellCommand(command);

        boolean hasExecutionFailed = false;
        try {
            int exitValue = execute(profiledShellCommand, stdout, stderr);
            if (exitValue != 0) {
                hasExecutionFailed = true;
            }
        } catch (ProcessExecutionException e) {
            hasExecutionFailed = true;
        }

        if (hasExecutionFailed) {
            String commandText = String.join(" ", profiledShellCommand);
            throw new RuntimeException(String.format("Execution of `%s` has failed" +
                "\nstdout: `%s`" +
                "\nstderr: `%s`", commandText, parseOutput(stdout), parseOutput(stderr)));
        } else {
            String output = parseOutput(stdout);
            logger.debug("Command output: `{}`", output);
            return output;
        }
    }

    private int execute(List<String> command, ByteArrayOutputStream stdout, ByteArrayOutputStream stderr) throws ProcessExecutionException {
        ProcessExecutor executor = new ProcessExecutor(
            config.getWorkingDirectory(),
            Collections.emptyList(),
            command,
            config.getPlatform(),
            Collections.emptyMap());

        return executor.execute(logger, stdout, stderr);
    }

    private List<String> getShellCommand(List<String> command) {
        List<String> profiledShellCommand =  new ArrayList<>();
        profiledShellCommand.add(getCurrentShell());
        profiledShellCommand.add("--login");
        profiledShellCommand.add("-c");
        profiledShellCommand.add(String.join(" ", command));

        return profiledShellCommand;
    }

    private String parseOutput(ByteArrayOutputStream stream) {
        return stream.toString().trim();
    }

    private String getCurrentShell() {
        String shell = System.getenv("SHELL");
        if (shell == null || shell.isEmpty()) {
            logger.debug("SHELL is not available in environment variables. Trying to get it from child process.");

            ByteArrayOutputStream stdout = new ByteArrayOutputStream();
            try {
                execute(Arrays.asList("echo", "$SHELL"), stdout, stdout);
                shell = parseOutput(stdout);
            } catch (ProcessExecutionException e) {
                throw new RuntimeException(e);
            }

            if (shell.isEmpty()) {
                throw new RuntimeException("SHELL was not available in child process. Falling back to bin/sh");
            }
        }

        return shell;
    }
}
