#!/bin/bash

# invoker env variables are not loaded yet
export HOME="$(dirname "$0")"

export NVS_HOME="$HOME/.nvs"
mkdir "$NVS_HOME"
echo "NVS_HOME set: $NVS_HOME";

export NVS_HOME="$HOME/.nvs"
git clone https://github.com/jasongin/nvs "$NVS_HOME"
echo "NVS checked out";

. "$NVS_HOME/nvs.sh" install
echo "NVS has installed itself into the shell"

. "$NVS_HOME/nvs.sh" \
  && nvs add v20.15.1