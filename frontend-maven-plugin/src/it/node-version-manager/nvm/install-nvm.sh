#!/bin/bash

# invoker env variables are not loaded yet
export HOME="$(dirname "$0")"

export NVM_DIR="$HOME/.nvm";
mkdir "$NVM_DIR"
echo "NVM_DIR set";

(
  git clone https://github.com/nvm-sh/nvm.git "$NVM_DIR" &&
  cd "$NVM_DIR" &&
  git checkout `git describe --abbrev=0 --tags --match "v[0-9]*" $(git rev-list --tags --max-count=1)`
);
echo "NVM checked out";


