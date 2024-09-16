package com.github.eirslett.maven.plugins.frontend.lib.version.manager;

public enum VersionManagerType {
    FNM("fnm"),
    NVM("nvm");

    // TODO add support for other version managers
    //    MISE("mise"),
    //    ASDF("asdf"),
    //    NVS("nvs"),


    private final String executable;

    VersionManagerType(String executable) {
        this.executable = executable;
    }

    public String getExecutable() {
        return executable;
    }
}
