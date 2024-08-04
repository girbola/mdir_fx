package com.girbola.imagehandling;

import uk.co.caprica.vlcj.factory.MediaPlayerFactory;
import uk.co.caprica.vlcj.player.base.MediaPlayer;

import java.nio.file.Path;

public class VideoVLCJThumb {
    public static long getVideoLength(Path path) {
        // Initialize MediaPlayerFactory
        MediaPlayerFactory mediaPlayerFactory = new MediaPlayerFactory();

        // Create a media player instance
        MediaPlayer mediaPlayer = mediaPlayerFactory.mediaPlayers().newMediaPlayer();

        // Prepare the media
        mediaPlayer.media().prepare(path.toString());

        // Get video length (in milliseconds)
        long videoLength = mediaPlayer.media().info().duration();

        // Convert milliseconds to seconds
        long videoLengthInSeconds = videoLength / 1000;

        System.out.println("Video length: " + videoLengthInSeconds + " seconds");

        // Release resources
        mediaPlayer.release();
        mediaPlayerFactory.release();

        return videoLengthInSeconds;
    }
}
