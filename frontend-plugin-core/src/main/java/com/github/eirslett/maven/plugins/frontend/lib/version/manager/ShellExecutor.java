package com.github.eirslett.maven.plugins.frontend.lib.version.manager;

import com.github.eirslett.maven.plugins.frontend.lib.InstallConfig;
import com.github.eirslett.maven.plugins.frontend.lib.ProcessExecutionException;
import com.github.eirslett.maven.plugins.frontend.lib.ProcessExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class ShellExecutor {

    final Logger logger = LoggerFactory.getLogger(getClass());
    final InstallConfig config;

    public ShellExecutor(InstallConfig config) {
        this.config = config;
    }

    public String execute(List<String> command) {
        List<String> profiledShellCommand = getShellCommand(command);

        ProcessExecutor executor = new ProcessExecutor(
            config.getWorkingDirectory(),
            Collections.emptyList(),
            profiledShellCommand,
            config.getPlatform(),
            Collections.emptyMap());

        ByteArrayOutputStream stdout = new ByteArrayOutputStream();
        ByteArrayOutputStream stderr = new ByteArrayOutputStream();

        try {
            int exitValue = executor.execute(logger, stdout, stderr);
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
        return System.getenv("SHELL");
    }
}