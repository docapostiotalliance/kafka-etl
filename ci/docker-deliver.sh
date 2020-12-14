#!/bin/bash

BASE_DIR="$(dirname $0)"
REPO_PATH="${BASE_DIR}/.."
IMAGE="${1}"
VERSION="${2}"

[[ $ARCH ]] || ARCH="x86"

tag_and_push() {
  docker tag "comworkio/${2}:latest" "comworkio/${2}:${1}"
  docker push "comworkio/${2}:${1}"
}

cd "${REPO_PATH}" && git pull origin master || : 

COMPOSE_DOCKER_CLI_BUILD=1 DOCKER_BUILDKIT=1 docker-compose build --no-cache "${IMAGE}"

echo "${DOCKER_ACCESS_TOKEN}" | docker login --username comworkio --password-stdin

docker-compose push "${IMAGE}"
tag_and_push "${VERSION}" "${IMAGE}"
tag_and_push "${VERSION}-${CI_COMMIT_SHORT_SHA}" "${IMAGE}"
