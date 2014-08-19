#!/bin/bash

BASEDIR=$(cd -P -- "$(dirname -- "$0")" && pwd -P)

_command_exists() {
    type "$1" &> /dev/null;
}

_port_is_taken() {
  lsof -i :$1 &> /dev/null;
}

_fatal() {
  echo -e 1>&2 "\033[31m-->" $@ "\033[0m"
  exit 2
}

# resolve symlinks
while [ -h "$BASEDIR/$0" ]; do
    DIR=$(dirname -- "$BASEDIR/$0")
    SYM=$(readlink $BASEDIR/$0)
    BASEDIR=$(cd $DIR && cd $(dirname -- "$SYM") && pwd)
done
cd ${BASEDIR}

if ! _command_exists node ; then
    _fatal "Please install node.js before running nodeproxy.sh"
fi

if _port_is_taken 7000 ; then
    _fatal "Port 7000 is already in use"
fi

printf "\e]0;PROXY\a"
until node startnodeproxy.js $@; do
    sleep 1
done
