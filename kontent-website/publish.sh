#!/usr/bin/env bash

PROJECT_ROOT="$( cd "$( dirname "${BASH_SOURCE[0]}" )/.." && pwd )"

$PROJECT_ROOT/gradlew buildWebsite

git add docs/*
git commit -am"Update website"
git push