#!/usr/bin/env bash

set -e  

device_number=4
delay_in_ms=1000

delay_in_ns=$(expr 1000000 \* $delay_in_ms)

gst-launch-1.0 v4l2src device=/dev/video$device_number \
! video/x-raw, format=UYVY, framerate=60/1, width=1920, height=1080 \
! glimagesink sync=false rotate-method=horizontal-flip render-rectangle="<0,0,1920,1080>"
# ! queue name=q min-threshold-time=$delay_in_ns max-size-buffers=0 max-size-bytes=0  \

