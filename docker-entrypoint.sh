#!/bin/bash

target="`find . -name \"dragonite-$1\"`"

if [ "$target" = "" ]; then
    echo "no such tool 'dragonite-$1'" >&2
    exit 1
fi

shift 1

$target "$@"
