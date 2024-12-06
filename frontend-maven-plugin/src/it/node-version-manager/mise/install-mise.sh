#!/bin/bash

set -x

# invoker env variables are not loaded yet
export HOME="$(dirname "$0")"

export MISE_INSTALL_PATH="$HOME/.mise/bin/mise"
mkdir "$HOME/.mise/bin"
echo "MISE_INSTALL_PATH set: $MISE_INSTALL_PATH"

export MISE_DEBUG=1
export MISE_TRACE=1
curl https://mise.run | sh
echo "MISE has been installed"

export MISE_DATA_DIR=$HOME/.mise/data
echo "MISE_DATA_DIR set: $MISE_DATA_DIR"

$HOME/.mise/bin/mise --version
$HOME/.mise/bin/mise install node@20.15.1
$HOME/.mise/bin/mise where node

echo "Mise installation end"
