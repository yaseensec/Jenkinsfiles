#!/bin/bash
timestamp=$(date +%M)
if [ `expr $timestamp % 2` == 0 ]
then
	exit 0
else
  sleep 1
	exit 1
fi
