#!/bin/sh
while true; do
  clear;
  clj -M:test;
  git ls-files | xargs inotifywait -e close_write;
done
