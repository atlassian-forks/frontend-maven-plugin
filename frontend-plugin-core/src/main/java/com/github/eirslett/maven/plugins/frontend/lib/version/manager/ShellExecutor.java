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
                logger.debug("Command finished with an error exit code {}", exitValue);
            }
        } catch (ProcessExecutionException e) {
            logger.debug("Command threw unexpectedly");
        }

        String output = parseOutput(stdout);
        logger.debug("Command output: `{}`\n error output `{}`", output, parseOutput(stderr));
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

        if (config.getPlatform().isWindows()) {
            logger.warn("Windows is currently not supported");
            profiledShellCommand.add(String.join(" ", command));
        } else {
            profiledShellCommand.add(getCurrentUnixShell());
//            profiledShellCommand.add("--login");
            profiledShellCommand.add("-c");
            profiledShellCommand.add(String.join(" ", command));
        }

        return profiledShellCommand;
    }

    private String parseOutput(ByteArrayOutputStream stream) {
        return stream.toString().trim();
    }

    private String getCurrentUnixShell() {
        String shell = System.getenv("SHELL");
        if (shell == null || shell.isEmpty()) {
            logger.debug("SHELL variable couldn't be found. Falling back on reading the variable from /bin/sh.");
            ByteArrayOutputStream stdout = new ByteArrayOutputStream();
            try {
                execute(Arrays.asList("/bin/sh", "-c", "echo $SHELL"), stdout, stdout);
                shell = parseOutput(stdout);
                logger.debug("SHELL from /bin/sh: {}", shell);
            } catch (ProcessExecutionException e) {
                throw new RuntimeException(e);
            }

            if (shell.isEmpty()) {
                throw new RuntimeException("SHELL is not available in environment variables. Please provide the value of $SHELL with your preferred shell.");
            }
        }

        return shell;
    }
}
