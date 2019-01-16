#!/bin/bash

if [[ -n "$START_MIRROR_MAKER" && "$START_MIRROR_MAKER" = "YES" ]]; then
        exec start-mirrormaker.sh
    else
        exec start-kafka.sh
 fi