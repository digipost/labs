#!/bin/bash -e
#
# Install/update all npm packages.
#

# Change dir
BASEDIR=$(cd -P -- "$(dirname -- "$0")" && pwd -P)
cd ${BASEDIR}

# Install global npm packages
sudo npm install -g jshint pacbot bower

# Install local npm packages
npm install colors http-proxy@0.10.3 request
