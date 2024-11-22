#!/bin/bash

# invoker env variables are not loaded yet
export HOME="$(dirname "$0")"

export FNM_DIR="$HOME/.fnm";
mkdir "$FNM_DIR"

curl -fsSL https://fnm.vercel.app/install | bash -s -- --install-dir "$FNM_DIR" --skip-shell --force-install

$HOME/.fnm/fnm env

eval "$($HOME/.fnm/fnm env)" \
        && echo $FNM_DIR \
        && $HOME/.fnm/fnm install 20.15.1 \
        && which fnm