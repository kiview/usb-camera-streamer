package dev.wittek.usbcamerastreamer;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.freedesktop.gstreamer.Gst;
import org.freedesktop.gstreamer.Pipeline;
import org.freedesktop.gstreamer.elements.AppSink;
import org.freedesktop.gstreamer.event.EOSEvent;
import org.freedesktop.gstreamer.fx.FXImageSink;


public class CameraStreamer extends Application {

    public static final int WINDOW_WIDTH = 1920;
    public static final int WINDOW_HEIGHT = 1080;

    public static final int SINK_FRAME_RATE = 60;
    public static final int WEBCAM_FRAME_RATE = 60;

    public static final int MS_TO_NS_FACTOR = 1000000;

    private final ImageView imageView;

    @SuppressWarnings("FieldCanBeLocal") // keep reference to avoid GC ;)
    private static Pipeline pipeline;

    public CameraStreamer() {
        imageView = new ImageView();
    }

    @Override
    public void start(Stage primaryStage) {
        initPipeline();

        BorderPane grid = new BorderPane();
        grid.setCenter(imageView);

        imageView.fitWidthProperty().bind(grid.widthProperty());
        imageView.fitHeightProperty().bind(grid.heightProperty());
        imageView.setPreserveRatio(true);

        primaryStage.initStyle(StageStyle.UNDECORATED);
        primaryStage.setScene(new Scene(grid, WINDOW_WIDTH, WINDOW_HEIGHT));
        primaryStage.setFullScreen(true);
        primaryStage.show();
    }

    private void initPipeline() {
        String pipelineDescription = buildPipelineDescription();
        System.out.println(pipelineDescription);
        pipeline = (Pipeline) Gst.parseLaunch(pipelineDescription);

        AppSink appSink = (AppSink) pipeline.getElementByName("sink");
        FXImageSink imageSink = new FXImageSink(appSink);

        imageSink.requestFrameRate(SINK_FRAME_RATE);
        imageSink.requestFrameSize(WINDOW_WIDTH, WINDOW_HEIGHT);

        imageView.imageProperty().bind(imageSink.imageProperty());

        pipeline.play();
    }

    private String buildPipelineDescription() {
        StringBuilder descriptionBuilder = new StringBuilder();

        String webcamSource =
                "v4l2src device=/dev/video0 " + // TODO: Make video id configurable
                "! video/x-raw, format=UYVY, width=%d, height=%d, framerate=%d/1 ";
        descriptionBuilder.append(String.format(webcamSource, WINDOW_WIDTH, WINDOW_HEIGHT, WEBCAM_FRAME_RATE));

        String tee = "! tee name=t ! queue ";
        descriptionBuilder.append(tee);

        String transformations =
                "! videobalance saturation=0.0 " +
                "! videoflip method=horizontal-flip ";

        if (getParameters().getNamed().containsKey("timeoverlay")) {
            String timerOverlay = "! timeoverlay halignment=center valignment=center ";
            transformations += timerOverlay;
        }

        transformations += "! videobox top=200 bottom=200 left=355 right=355 " + // TODO: make configurable
                "! videoscale ";

        descriptionBuilder.append(transformations);

        if (getParameters().getNamed().containsKey("delay")) {
            int delayInMs = Integer.parseInt(getParameters().getNamed().get("delay"));
            long delayInNs = delayInMs * MS_TO_NS_FACTOR;

            String queue = String.format("! queue name=q min-threshold-time=%d max-size-buffers=0 max-size-bytes=0 max-size-time=0 ", delayInNs);
            descriptionBuilder.append(queue);
        }

        String javaFxSink =
                "! videoconvert " +
                "! appsink name=sink ";
        descriptionBuilder.append(javaFxSink);

        // TODO: select by CLI argument
        String cpuEncoding = "x264enc tune=zerolatency";
        String gpuNvidiaEncoding = "nvh264enc ! h264parse";

        String recordingFileName = System.currentTimeMillis() + ".flv";
        String fileSink =
                "t. ! queue " +
                "! videoconvert ! " + gpuNvidiaEncoding + " ! flvmux ! filesink location=" + recordingFileName;

        descriptionBuilder.append(fileSink);

        return descriptionBuilder.toString();
    }

    public static void main(String[] args) {
        String[] gstreamerArgs = new String[0];
        Gst.init("Streamer", gstreamerArgs);
        Runtime.getRuntime().addShutdownHook(new Thread(CameraStreamer::shutdownPipeline));
        launch(args);
    }

    static void shutdownPipeline() {
        System.out.println("Stop!");

        pipeline.getSources().get(0).sendEvent(new EOSEvent());
        try {
            System.out.println("1 sec wait to flush out recording file.");
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            System.err.println("Something went terribly wrong!");
        }
        pipeline.stop();
        Gst.deinit();
        Gst.quit();
    }
}


