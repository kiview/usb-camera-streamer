gst-launch-1.0 -v ksvideosrc do-stats=TRUE `
! video/x-raw,format=UYVY,width=1920,height=1080,framerate=60/1 `
! videoconvert `
! video/x-raw,format=RGBA,framerate=60/1,width=1920,height=1080 `
! glupload `
! gltransformation scale-y=2.0 scale-x=2.0 `
! glimagesink sync=false brightness=-0.22 saturation=0.0 render-rectangle="<0,0,1920,1080>" rotate-method=horizontal-flip 
