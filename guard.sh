#!/bin/sh
while true; do
  clear;
  clj -M:test bowling;
  inotifywait -e close_write bowling.clj;
done
