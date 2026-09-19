#!/bin/bash
set -e

PROJECT_DIR="$(cd "$(dirname "$0")" && pwd)"
cd "$PROJECT_DIR"

if ! command -v brew >/dev/null 2>&1; then
  echo "Homebrew is required. Install it from https://brew.sh and try again."
  read -r -p "Press Return to close..."
  exit 1
fi

export JAVA_HOME="$(brew --prefix openjdk@21)/libexec/openjdk.jdk/Contents/Home"
export PATH="$JAVA_HOME/bin:$PATH"

if ! command -v mvn >/dev/null 2>&1; then
  echo "Maven is missing. Run: brew install maven"
  read -r -p "Press Return to close..."
  exit 1
fi

exec mvn -q -DskipTests compile exec:java -Dexec.mainClass=PresidentsServer.Presidents
