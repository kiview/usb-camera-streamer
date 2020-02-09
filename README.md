# USB Camera Streamer

[gstreamer](https://gstreamer.freedesktop.org/) based pipeline for rendering raw webcam input trying to be as low overhead as possible and providing fine grained controls of parameters such as rendering latency and capture formats. 

## Hardware

This script was written for a [See3CAM_CU135](https://www.e-consystems.com/4k-usb-camera.asp#Key-features) USB camera but should work with other devices if configured according to their specs.


## Default Operation

As a default, the gstreamer pipeline will configure the `v4l2src` webcam sink to UYVY 1080P@60FPS (which reguries a USB 3.0 connection).

## Software Requirements

Tested on Ubuntu 18.04 and Fedora 31. In order to get a fullscreen rendered image on GNOME, install [Hide Top Bar Extension](https://extensions.gnome.org/extension/545/hide-top-bar/).

`gstreamer` and `gstreamer-plugin` packages need to be installed. If this command works, the script should probably work as well:

```
$ gst-inspect-1.0 glimagesink 
```

## Appendix

### Camera

Find out video device id. This will probably different based on USB setup and other connected devices.
Run this command and compare output with and without camera attached. If in doubt, just try out different numbers, it will probably be one of the higher ones, since the lower ones normally map to an internal webcam. 
```bash 
$ ls -ltrh /dev/video*
```

To get list of supported output formats, run:
```bash
$ v4l2-ctl --list-formats-ext -d /dev/video$device_id
```

### USB 3.0

To get the necessary performance out of the camera, a USB 3.0 connection is required. 
This command lists all connected USB devices including there speed:
```bash
$ lsusb -t
```

This is possible output:
```
/:  Bus 02.Port 1: Dev 1, Class=root_hub, Driver=xhci_hcd/6p, 10000M
    |__ Port 3: Dev 5, If 0, Class=Video, Driver=uvcvideo, 5000M
    |__ Port 3: Dev 5, If 1, Class=Video, Driver=uvcvideo, 5000M
    |__ Port 3: Dev 5, If 2, Class=Human Interface Device, Driver=usbhid, 5000M
/:  Bus 01.Port 1: Dev 1, Class=root_hub, Driver=xhci_hcd/12p, 480M
    |__ Port 1: Dev 2, If 0, Class=Chip/SmartCard, Driver=, 12M
    |__ Port 4: Dev 67, If 0, Class=Hub, Driver=hub/4p, 480M
        |__ Port 3: Dev 69, If 0, Class=Hub, Driver=hub/4p, 480M
            |__ Port 3: Dev 70, If 0, Class=Hub, Driver=hub/4p, 480M
                |__ Port 4: Dev 72, If 0, Class=Human Interface Device, Driver=usbhid, 12M
                |__ Port 4: Dev 72, If 1, Class=Human Interface Device, Driver=usbhid, 12M
                |__ Port 2: Dev 71, If 1, Class=Human Interface Device, Driver=usbhid, 12M
                |__ Port 2: Dev 71, If 2, Class=Human Interface Device, Driver=, 12M
                |__ Port 2: Dev 71, If 0, Class=Human Interface Device, Driver=usbhid, 12M
        |__ Port 1: Dev 68, If 0, Class=Human Interface Device, Driver=usbhid, 1.5M
    |__ Port 7: Dev 66, If 2, Class=Communications, Driver=cdc_acm, 480M
    |__ Port 7: Dev 66, If 0, Class=Communications, Driver=cdc_mbim, 480M
    |__ Port 7: Dev 66, If 3, Class=CDC Data, Driver=cdc_acm, 480M
    |__ Port 7: Dev 66, If 1, Class=CDC Data, Driver=cdc_mbim, 480M
    |__ Port 8: Dev 4, If 3, Class=Video, Driver=uvcvideo, 480M
    |__ Port 8: Dev 4, If 1, Class=Video, Driver=uvcvideo, 480M
    |__ Port 8: Dev 4, If 2, Class=Video, Driver=uvcvideo, 480M
    |__ Port 8: Dev 4, If 0, Class=Video, Driver=uvcvideo, 480M
    |__ Port 9: Dev 5, If 0, Class=Vendor Specific Class, Driver=, 12M
    |__ Port 10: Dev 6, If 0, Class=Wireless, Driver=btusb, 12M
    |__ Port 10: Dev 6, If 1, Class=Wireless, Driver=btusb, 12M
```

Make surew, the camera is connected with `5000M` speed (meaning USB 3.0). `480M` would mean a USB 2.0 connection.


## Attributions
Thanks to [Neil C Smith](https://github.com/neilcsmith-net) for getting us started with some first gstreamer examples, this was a life saver. 

Also thanks to [Bruno Herbelin](https://sourceforge.net/u/brunoherbelin/profile/) for helping us with the initial setup of using [GLMixer](https://sourceforge.net/projects/glmixer/) and pointing us towards glstreamer. 

## Copyright
Copyright (c) 2020 Neslihan Wittek and Kevin Wittek.
