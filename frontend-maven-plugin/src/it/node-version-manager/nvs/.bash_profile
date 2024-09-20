export NVS_HOME="$HOME/.nvs"
[ -s "$NVS_HOME/nvs.sh" ] && \. "$NVS_HOME/nvs.sh" # This loads nvs into the shell

# disable global fnm if installed
fnm() {
    exit 1;
}