#!/bin/bash

REPO_PATH="${PROJECT_HOME}/kafka-etl/"

cd "${REPO_PATH}" && git pull origin master || :
git push github master 
git push pgitlab master
git push bitbucket master
git push froggit master
exit 0
