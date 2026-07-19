package com.girbola.imagehandling;

import com.girbola.messages.Messages;
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
        long videoLength = mediaPlayer.status().length();

        // Convert milliseconds to seconds
        long videoLengthInSeconds = videoLength / 1000;

        Messages.sprintf("Video length: " + videoLengthInSeconds + " seconds" + " videoLength: " + videoLength);

        // Release resources
        mediaPlayer.release();
        mediaPlayerFactory.release();

        return videoLengthInSeconds;
    }
}
