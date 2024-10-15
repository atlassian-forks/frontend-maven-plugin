#!/bin/bash

# invoker env variables are not loaded yet
export HOME="$(dirname "$0")"

export ASDF_DIR="$HOME/.asdf";
mkdir "$ASDF_DIR"
echo "ASDF_DIR set: $ASDF_DIR";

git clone https://github.com/asdf-vm/asdf.git "$ASDF_DIR" --branch v0.14.1
echo "ASDF checked out";


