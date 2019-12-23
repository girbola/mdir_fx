package com.girbola.vlcj;

import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.util.concurrent.Semaphore;

import javafx.animation.AnimationTimer;
import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Slider;
import javafx.scene.control.ToggleButton;
import javafx.scene.image.PixelFormat;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.image.WritablePixelFormat;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.transform.Affine;
import javafx.stage.Stage;
import javafx.util.Duration;
import uk.co.caprica.vlcj.factory.MediaPlayerFactory;
import uk.co.caprica.vlcj.media.MediaRef;
import uk.co.caprica.vlcj.media.TrackType;
import uk.co.caprica.vlcj.player.base.MediaPlayer;
import uk.co.caprica.vlcj.player.base.MediaPlayerEventListener;
import uk.co.caprica.vlcj.player.embedded.EmbeddedMediaPlayer;
import uk.co.caprica.vlcj.player.embedded.videosurface.CallbackVideoSurface;
import uk.co.caprica.vlcj.player.embedded.videosurface.VideoSurfaceAdapters;
import uk.co.caprica.vlcj.player.embedded.videosurface.callback.BufferFormat;
import uk.co.caprica.vlcj.player.embedded.videosurface.callback.BufferFormatCallback;
import uk.co.caprica.vlcj.player.embedded.videosurface.callback.RenderCallback;
import uk.co.caprica.vlcj.player.embedded.videosurface.callback.format.RV32BufferFormat;

public class VLCPlayerController {
    private Path file;
    private Stage vlc_stage;

    /**
     * Pixel writer to update the canvas.
     */
    private PixelWriter pixelWriter;

    private WritablePixelFormat<ByteBuffer> pixelFormat;

    private MediaPlayerFactory mediaPlayerFactory;

    private EmbeddedMediaPlayer mediaPlayer;
    private WritableImage img;
    private AnimationTimer timer;

    @FXML
    private VBox bottomBar;

    @FXML
    private Canvas vlc_canvas;

    @FXML
    private Slider volume_slider;

    @FXML
    private Slider slider;

    @FXML
    private Button play_btn;

    @FXML
    private Button skip_back_btn;

    @FXML
    private Button stop_btn;

    @FXML
    private Button skip_forward_btn;

    @FXML
    private ToggleButton mute_btn;

    @FXML
    private ToggleButton fullScreen_btn;

    @FXML
    void play_btn_action(ActionEvent event) {
	mediaPlayer.controls().play();
    }

    @FXML
    void skip_back_btn_action(ActionEvent event) {

    }

    @FXML
    void skip_forward_btn_action(ActionEvent event) {

    }

    @FXML
    void stop_btn_action(ActionEvent event) {
	mediaPlayer.controls().pause();
    }

    @SuppressWarnings("restriction")
    public void init(Path file, Stage stage) {
	this.file = file;
	this.vlc_stage = stage;

	pixelWriter = vlc_canvas.getGraphicsContext2D().getPixelWriter();
	pixelFormat = PixelFormat.getByteBgraInstance();

	mediaPlayerFactory = new MediaPlayerFactory();
	mediaPlayer = mediaPlayerFactory.mediaPlayers().newEmbeddedMediaPlayer();

	mediaPlayer.events().addMediaPlayerEventListener(new MPE(slider));
	mediaPlayer.videoSurface().set(new JavaFxVideoSurface());

	volume_slider.valueProperty().addListener(new ChangeListener<Number>() {

	    @Override
	    public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
		System.out.println("volumeee: " + newValue);
		Platform.runLater(() -> {
		    mediaPlayer.audio().setVolume(newValue.intValue());
		});
	    }
	});
	mediaPlayer.media().prepare(file.toString());
	timer = new AnimationTimer() {
	    @Override
	    public void handle(long now) {
		renderFrame();
	    }
	};
	Pane pane = (Pane) stage.getScene().getRoot();
	// FadeTransition ft = new FadeTransition(Duration.seconds(1), bottomBar);
	// ft.setAutoReverse(false);
	// ft.setCycleCount(1);
	// ft.setFromValue(1);
	// ft.setToValue(0);

	pane.setOnMouseMoved(new EventHandler<MouseEvent>() {
	    @Override
	    public void handle(MouseEvent event) {
		FadeTransition ft = new FadeTransition(Duration.millis(2200), bottomBar);
		ft.setFromValue(1.0);
		ft.setToValue(0.0);
		ft.setAutoReverse(true);
		ft.play();
	    }
	});
	vlc_canvas.widthProperty().bind(pane.widthProperty().subtract(10));
	vlc_canvas.heightProperty().bind(pane.heightProperty().subtract(10));

	timer.start();
	mediaPlayer.media().play(file.toString());

    }

    private class JavaFxVideoSurface extends CallbackVideoSurface {

	JavaFxVideoSurface() {
	    super(new JavaFxBufferFormatCallback(), new JavaFxRenderCallback(), true,
		    VideoSurfaceAdapters.getVideoSurfaceAdapter());
	}

    }

    private class JavaFxBufferFormatCallback implements BufferFormatCallback {
	@Override
	public BufferFormat getBufferFormat(int sourceWidth, int sourceHeight) {
	    VLCPlayerController.this.img = new WritableImage(sourceWidth, sourceHeight);
	    VLCPlayerController.this.pixelWriter = img.getPixelWriter();

	    Platform.runLater(new Runnable() {
		@Override
		public void run() {
		    vlc_stage.setWidth(Math.floor(sourceWidth / 2));
		    vlc_stage.setHeight(Math.floor(sourceHeight / 2));
		}
	    });
	    return new RV32BufferFormat(sourceWidth, sourceHeight);
	}
    }

    // Semaphore used to prevent the pixel writer from being updated in one thread
    // while it is being rendered by a
    // different thread
    private final Semaphore semaphore = new Semaphore(1);

    // This is correct as far as it goes, but we need to use one of the timers to
    // get smooth rendering (the timer is
    // handled by the demo sub-classes)
    private class JavaFxRenderCallback implements RenderCallback {
	@Override
	public void display(MediaPlayer mediaPlayer, ByteBuffer[] nativeBuffers, BufferFormat bufferFormat) {
	    try {
		semaphore.acquire();
		pixelWriter.setPixels(0, 0, bufferFormat.getWidth(), bufferFormat.getHeight(), pixelFormat,
			nativeBuffers[0], bufferFormat.getPitches()[0]);
		semaphore.release();
	    } catch (InterruptedException e) {
	    }
	}
    }

    protected final void renderFrame() {
	GraphicsContext g = vlc_canvas.getGraphicsContext2D();

	double width = vlc_canvas.getWidth();
	double height = vlc_canvas.getHeight();

	g.setFill(new Color(0, 0, 0, 1));
	g.fillRect(0, 0, width, height);

	if (img != null) {
	    double imageWidth = img.getWidth();
	    double imageHeight = img.getHeight();

	    double sx = width / imageWidth;
	    double sy = height / imageHeight;

	    double sf = Math.min(sx, sy);

	    double scaledW = imageWidth * sf;
	    double scaledH = imageHeight * sf;

	    Affine ax = g.getTransform();

	    g.translate((width - scaledW) / 2, (height - scaledH) / 2);

	    if (sf != 1.0) {
		g.scale(sf, sf);
	    }

	    try {
		semaphore.acquire();
		g.drawImage(img, 0, 0);
		semaphore.release();
	    } catch (InterruptedException e) {
	    }

	    g.setTransform(ax);
	}
    }

    public EmbeddedMediaPlayer getMediaPlayer() {
	return this.mediaPlayer;
    }

    private class MPE implements MediaPlayerEventListener {

	private Slider slider;

	MPE(Slider aSlider) {
	    this.slider = aSlider;
	}

	@Override
	public void timeChanged(MediaPlayer mp, long l) {
	    // System.out.println("Time now is: " +
	    // hhmmss_dots.format(mediaPlayer.status().time()) + " Long is: " + l);
	}

	@Override
	public void positionChanged(MediaPlayer mp, float f) {
	    slider.setValue(Double.valueOf("" + (f * 100f)));
	}

	@Override
	public void mediaChanged(MediaPlayer mediaPlayer, MediaRef media) {

	}

	@Override
	public void opening(MediaPlayer mediaPlayer) {

	}

	@Override
	public void buffering(MediaPlayer mediaPlayer, float newCache) {

	}

	@Override
	public void playing(MediaPlayer mediaPlayer) {

	}

	@Override
	public void paused(MediaPlayer mediaPlayer) {

	}

	@Override
	public void stopped(MediaPlayer mediaPlayer) {

	}

	@Override
	public void forward(MediaPlayer mediaPlayer) {

	}

	@Override
	public void backward(MediaPlayer mediaPlayer) {

	}

	@Override
	public void finished(MediaPlayer mediaPlayer) {

	}

	@Override
	public void seekableChanged(MediaPlayer mediaPlayer, int newSeekable) {

	}

	@Override
	public void pausableChanged(MediaPlayer mediaPlayer, int newPausable) {

	}

	@Override
	public void titleChanged(MediaPlayer mediaPlayer, int newTitle) {

	}

	@Override
	public void snapshotTaken(MediaPlayer mediaPlayer, String filename) {

	}

	@Override
	public void lengthChanged(MediaPlayer mediaPlayer, long newLength) {

	}

	@Override
	public void videoOutput(MediaPlayer mediaPlayer, int newCount) {

	}

	@Override
	public void scrambledChanged(MediaPlayer mediaPlayer, int newScrambled) {

	}

	@Override
	public void elementaryStreamAdded(MediaPlayer mediaPlayer, TrackType type, int id) {

	}

	@Override
	public void elementaryStreamDeleted(MediaPlayer mediaPlayer, TrackType type, int id) {

	}

	@Override
	public void elementaryStreamSelected(MediaPlayer mediaPlayer, TrackType type, int id) {

	}

	@Override
	public void corked(MediaPlayer mediaPlayer, boolean corked) {

	}

	@Override
	public void muted(MediaPlayer mediaPlayer, boolean muted) {

	}

	@Override
	public void volumeChanged(MediaPlayer mediaPlayer, float volume) {
	    System.out.println("volume: " + volume);
	}

	@Override
	public void audioDeviceChanged(MediaPlayer mediaPlayer, String audioDevice) {

	}

	@Override
	public void chapterChanged(MediaPlayer mediaPlayer, int newChapter) {

	}

	@Override
	public void error(MediaPlayer mediaPlayer) {

	}

	@Override
	public void mediaPlayerReady(MediaPlayer mediaPlayer) {

	}

    }

    public void stop() {
	timer.stop();
	mediaPlayer.release();
    }

}
