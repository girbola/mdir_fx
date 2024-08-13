package com.girbola.vlcj;


import com.girbola.messages.Messages;
import uk.co.caprica.vlcj.factory.MediaPlayerFactory;
import uk.co.caprica.vlcj.player.base.MediaPlayer;
import uk.co.caprica.vlcj.player.base.MediaPlayerEventAdapter;

import java.io.File;
import java.nio.file.Paths;
import java.util.concurrent.CountDownLatch;

import static com.girbola.configuration.VLCJDiscovery.discovery;
import static com.girbola.configuration.VLCJDiscovery.initVlc;

public class VLCJThumbTest {

    private final String[] VLC_ARGS = {
            "--intf", "dummy",          /* no interface */
            "--vout", "dummy",          /* we don't want video (output) */
            "--no-audio",               /* we don't want audio (decoding) */
            "--no-snapshot-preview",    /* no blending in dummy vout */
    };

    private final float VLC_THUMBNAIL_POSITION = 30.0f / 100.0f;

    public void convertToFile(File file) throws Exception {
        initVlc();
        discovery(Paths.get("/snap/vlc/current/lib/x86_64-linux-gnu/"));
//        /snap/vlc/current/lib/x86_64-linux-gnu

        String mrl = new File("/home/gerbiloi/Pictures/Riston kuvat/Kuvia/Videot 200911 (Gran Kanarialla)/16112009 (Suite Monte Golf parveke esitys).mp4").toString();

        int imageWidth = 500;
        File snapshotFile = new File("/home/gerbiloi/vlcj_fiilu");

        MediaPlayerFactory factory = new MediaPlayerFactory(VLC_ARGS);
        MediaPlayer mediaPlayer = factory.mediaPlayers().newMediaPlayer();

        final CountDownLatch inPositionLatch = new CountDownLatch(1);
        final CountDownLatch snapshotTakenLatch = new CountDownLatch(1);

        mediaPlayer.events().addMediaPlayerEventListener(new MediaPlayerEventAdapter() {

            @Override
            public void positionChanged(MediaPlayer mediaPlayer, float newPosition) {
                if(newPosition >= VLC_THUMBNAIL_POSITION * 0.9f) { /* 90% margin */
                    inPositionLatch.countDown();
                }
            }

            @Override
            public void snapshotTaken(MediaPlayer mediaPlayer, String filename) {
                Messages.sprintf("snapshotTaken(filename=" + filename + ")");
                snapshotTakenLatch.countDown();
            }
        });

        if (mediaPlayer.media().start(mrl)) {
            mediaPlayer.controls().setPosition(VLC_THUMBNAIL_POSITION);
            inPositionLatch.await(); // Might wait forever if error

            mediaPlayer.snapshots().save(snapshotFile, imageWidth, 0);
            snapshotTakenLatch.await(); // Might wait forever if error

            mediaPlayer.controls().stop();
        }

        mediaPlayer.release();
        factory.release();
    }

}
