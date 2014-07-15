#!/bin/bash -e
#
# Install/update all npm packages.
#

# Change dir
BASEDIR=$(cd -P -- "$(dirname -- "$0")" && pwd -P)
cd ${BASEDIR}

# Check env vars
[ -z "$DPOST_WEBAPP_REPO" ] && { echo "Missing DPOST_WEBAPP_REPO"; exit 1; }
[ -z "$DPOST_POSTIT_REPO" ] && { echo "Missing DPOST_POSTIT_REPO"; exit 1; }

# Install global npm packages
sudo npm install -g jshint pacbot

# Install local npm packages
npm install colors http-proxy@0.10.3 request
