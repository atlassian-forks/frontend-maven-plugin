package com.github.eirslett.maven.plugins.frontend.lib;

public final class ProcessExecutionException extends Exception {
    private static final long serialVersionUID = 1L;

    public ProcessExecutionException(String message) {
        super(message);
    }

    public ProcessExecutionException(Throwable cause) {
        super(cause);
    }
}
