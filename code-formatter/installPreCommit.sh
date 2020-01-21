#!/bin/bash

DIR="/Users/ineumann/uprodit"
GIT_DIR="${DIR}/.git"
HOOKS_DIR="${GIT_DIR}/hooks"
CODE_FORMATTER_DIR="${HOOKS_DIR}/code-formatter"
BIN_DIR="${CODE_FORMATTER_DIR}/bin"
CODE_STYLE_DIR="${HOOKS_DIR}/code-style"
SRC_DIR="${DIR}/prodit-batch/src/main/resources/code-formatter"

create_if_needed() {
    if [ ! -d  "$1" ]; then
        mkdir -p "$1"
    fi
}

create_if_needed "$BIN_DIR"
create_if_needed "$CODE_STYLE_DIR"

rm -rf "${BIN_DIR}/code-formatter-jar-with-dependencies.jar"
if [ ! -f "${BIN_DIR}/code-formatter-jar-with-dependencies.jar" ]; then
  mvn clean install
fi

cp -f "${SRC_DIR}/bin/code-formatter-jar-with-dependencies.jar" "$BIN_DIR"
cp "${SRC_DIR}/code-style"/*.xml "${HOOKS_DIR}/code-style"
cp "${SRC_DIR}/codeFormat.sh" "${HOOKS_DIR}/code-formatter"
cp "${SRC_DIR}/pre-commit" "$HOOKS_DIR"

chmod a+x "${CODE_FORMATTER_DIR}/codeFormat.sh"
chmod a+x "${HOOKS_DIR}/pre-commit"
