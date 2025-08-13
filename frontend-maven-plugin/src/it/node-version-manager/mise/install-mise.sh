#!/bin/bash

set -x

# invoker env variables are not loaded yet
export HOME="$(dirname "$0")"

export MISE_INSTALL_PATH="$HOME/.mise/bin/mise"
mkdir "$HOME/.mise/bin"
echo "MISE_INSTALL_PATH set: $MISE_INSTALL_PATH"

export MISE_DEBUG=1
export MISE_VERBOSE=1
curl https://mise.run | sh
echo "MISE has been installed"

eval "$(${HOME}/.mise/bin/mise activate bash --shims)"
export MISE_NODE_GPG_VERIFY=0
mise --version
mise install node@12.4.0
