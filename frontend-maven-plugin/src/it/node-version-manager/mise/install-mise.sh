#!/bin/bash

# invoker env variables are not loaded yet
export HOME="$(dirname "$0")"

export MISE_INSTALL_PATH="$HOME/.mise/bin/mise"
mkdir "$MISE_INSTALL_PATH"
echo "MISE_INSTALL_PATH set: $MISE_INSTALL_PATH"

export MISE_DEBUG=true
curl https://mise.run | sh
echo "MISE has been installed"
