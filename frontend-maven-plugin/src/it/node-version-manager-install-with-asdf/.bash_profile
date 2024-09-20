export ASDF_DIR="$HOME/.asdf"
[ -s "$ASDF_DIR/asdf.sh" ] && \. "$ASDF_DIR/asdf.sh" # This loads asdf into the shell

# disable global fnm if installed
fnm() {
    exit 1;
}