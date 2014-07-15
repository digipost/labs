#!/bin/bash

BASEDIR=$(cd -P -- "$(dirname -- "$0")" && pwd -P)

# resolve symlinks
while [ -h "$BASEDIR/$0" ]; do
    DIR=$(dirname -- "$BASEDIR/$0")
    SYM=$(readlink $BASEDIR/$0)
    BASEDIR=$(cd $DIR && cd $(dirname -- "$SYM") && pwd)
done
cd ${BASEDIR}

# --------------------------------------

_debug() {
  echo -e 1>&2 "\033[34m-->" $@ "\033[0m"
}

_info() {
  echo -e 1>&2 "\033[32m-->" $@ "\033[0m"
}

_line() {
  printf %80s |tr " " "-"; echo ""
}

_line
_info "Started Labs Screens"
echo ""
_debug "Swap between screens:  F1-F4 (F5=work)"
_debug "Restarte a screen:     ctrl-c, enter, r"
_debug "Start a new screen:    ctrl-x, c"
_debug "Close a screen:        ctrl-x, k"
_debug "Kill everything:     ctrl-x, q"
echo ""
_debug "Scrolling: ctrl-x, esc. Use the up and down arraow keys (hold down fn to jump). Esc to escape."
echo ""
_info "This window is yours, do whatever you want with it ;)"
_line