#!/bin/bash

# invoker env variables are not loaded yet
export HOME="$(dirname "$0")"

export FNM_DIR="$HOME/.fnm";
mkdir "$FNM_DIR"

curl -fsSL https://fnm.vercel.app/install | bash -s -- --install-dir "$FNM_DIR" --skip-shell --force-install

export DEFAULT_NODE_VERSION=16.16.0
eval "$(fnm env)" \
        && fnm install $DEFAULT_NODE_VERSION \
        && fnm alias default $DEFAULT_NODE_VERSION \
        && fnm use default