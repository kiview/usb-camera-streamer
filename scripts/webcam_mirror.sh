#!/usr/bin/env bash

set -e  

delay_in_ms=$1

if test -z $1 
then
  echo ERROR: Please specify a delay value in ms. 0 means no delay.
  exit 1
fi

# set device number according to "ls -ltrh /dev/video*" output
device_number=4

# see https://gstreamer.freedesktop.org/documentation/video4linux2/v4l2src.html?gi-language=c#v4l2src_GstV4l2IOMode
io_mode=4

# crop source image by this amount of pixels on the top and bottom
crop_y=200
crop_x=$(expr 17778 \* $crop_y / 10000) # calculated to keep aspect-ratio


if [ "$delay_in_ms" -eq "0" ]; then
    gst-launch-1.0 v4l2src device=/dev/video$device_number io-mode=$io_mode \
    ! video/x-raw, format=UYVY, framerate=60/1, width=1920, height=1080 \
    ! videobox left=$crop_x right=$crop_x top=$crop_y bottom=$crop_y \
    ! glimagesink sync=false brightness=-0.22 render-rectangle="<0,0,1920,1080>" rotate-method=horizontal-flip 
else
    delay_in_ns=$(expr 1000000 \* $delay_in_ms)
    delay_max=$(expr 2 \* $delay_in_ns)

    gst-launch-1.0 v4l2src device=/dev/video$device_number io-mode=$io_mode \
    ! video/x-raw, format=UYVY, framerate=60/1, width=1920, height=1080 \
    ! videobox left=$crop_x right=$crop_x top=$crop_y bottom=$crop_y \
    ! queue name=q min-threshold-time=$delay_in_ns max-size-buffers=0 max-size-bytes=0 max-size-time=0 \
    ! glimagesink sync=false brightness=-0.22 render-rectangle="<0,0,1920,1080>" rotate-method=horizontal-flip 
fi
