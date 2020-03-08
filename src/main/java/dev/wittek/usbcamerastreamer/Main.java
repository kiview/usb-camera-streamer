package dev.wittek.usbcamerastreamer;

import javafx.application.Application;
import org.freedesktop.gstreamer.Gst;

/**
 * Non-modular applications must specify the application class explicitly rather
 * than allowing the launcher to use reflection to try to instantiate and start
 * an application.
 *
 * @see <a href="https://openjfx-dev.openjdk.java.narkive.com/aFiw9uqi/error-javafx-runtime-components-are-missing-and-are-required-to-run-this-application">Error: JavaFX runtime components are missing, and are required to run this application </a>
 */
public class Main {

    private Main() {}

    public static void main(final String[] args) {
        Gst.init("Streamer", args);
        Application.launch(CameraStreamer.class, args);
    }
}
