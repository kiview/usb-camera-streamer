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
import org.freedesktop.gstreamer.fx.FXImageSink;


public class CameraStreamer extends Application {

    public static final int WINDOW_WIDTH = 1920;
    public static final int WINDOW_HEIGHT = 1080;

    public static final int SINK_FRAME_RATE = 60;
    public static final int WEBCAM_FRAME_RATE = 60;

    public static final int MS_TO_NS_FACTOR = 1000000;

    private final ImageView imageView;

    @SuppressWarnings("FieldCanBeLocal") // keep reference to avoid GC ;)
    private Pipeline pipeline;

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
        primaryStage.show();
    }

    @Override
    public void stop() throws Exception {
        pipeline.stop();
        super.stop();
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
                "v4l2src device=/dev/video0 " +
                "! video/x-raw, format=UYVY, width=%d, height=%d, framerate=%d/1 ";
        descriptionBuilder.append(String.format(webcamSource, WINDOW_WIDTH, WINDOW_HEIGHT, WEBCAM_FRAME_RATE));

        String tee = "! tee name=t ! queue ";
        descriptionBuilder.append(tee);

        String transformations =
                "! videobalance saturation=0.0 " +
                "! videoflip method=horizontal-flip " +
                "! timeoverlay halignment=center valignment=center " +
                "! videobox top=200 bottom=200 left=355 right=355 " + // TODO: make configurable
                "! videoscale";
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

        String fileSink =
                "t. ! queue " +
                "! videoconvert ! x264enc tune=zerolatency ! flvmux ! filesink location=recording.flv";
        descriptionBuilder.append(fileSink);

        return descriptionBuilder.toString();
    }

    public static void main(String[] args) {
        String[] gstreamerArgs = new String[0];
        Gst.init("Streamer", gstreamerArgs);

        launch(args);
    }
}


