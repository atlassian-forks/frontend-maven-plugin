MISE_INSTALL_PATH=$HOME/.mise/bin/mise
eval "$($MISE_INSTALL_PATH activate bash)"

# disable global fnm if installed
fnm() {
    exit 1;
}