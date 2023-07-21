package com.girbola.imagehandling;

import com.girbola.Main;
import com.girbola.imagehandling.jcodec.AWTUtil;
import com.girbola.messages.Messages;
import lombok.extern.slf4j.Slf4j;
import net.coobird.thumbnailator.Thumbnails;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.jcodec.api.FrameGrab;
import org.jcodec.common.*;
import org.jcodec.common.io.NIOUtils;
import org.jcodec.common.model.Picture;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Slf4j	
public class JCodecVideoThumb {

	public static List<BufferedImage> getList(File file) throws FFmpegFrameGrabber.Exception {
		List<BufferedImage> list = new ArrayList<>();
		double duration = getVideoLenght(file);
		if (duration < 0) {
			return null;
		}
		List<Double> listOfTimeLine = grabListOfTimeLine(duration);
		list = grabBufferedImageList(file, listOfTimeLine);
		if(list == null) {
			return null;
		}
		System.out.println("Total duration: " + duration + " GetList done: " + list.size());
		return list;
	}

	private static List<BufferedImage> grabBufferedImageList(File file, List<Double> list) throws FFmpegFrameGrabber.Exception {
		List<BufferedImage> buff_list = new ArrayList<>();
		FrameGrab grab = null;
		try {
			grab = FrameGrab.createFrameGrab(NIOUtils.readableChannel(file));
			grab.seekToSecondSloppy(list.get(0));
			if (list.size() > 1) {
				for (int i = 1; i < list.size(); i++) {
					if (Main.getProcessCancelled()) {
						return null;
					}
					double sec = list.get(i);
					Messages.sprintf("createFrameGrab sec: " + i);
					Picture picture = grab.getNativeFrame();
					BufferedImage buff = AWTUtil.toBufferedImage(picture);
					buff = Thumbnails.of(buff).height(150).keepAspectRatio(true).asBufferedImage();
					if (buff != null) {
						buff_list.add(buff);
						Messages.sprintf("sec before seek: " + sec);
						grab.seekToSecondSloppy(sec);
					}
				}
			}
		} catch (Exception e) {

			FFmpegFrameGrabber ffMpegVideo = createVideoThumb(file.toString());
			int lengthInFrames = ffMpegVideo.getLengthInFrames();
			Messages.sprintf("===lengthInFrames: " + lengthInFrames);

			e.printStackTrace();
		}
		return buff_list;
	}
	private static FFmpegFrameGrabber createVideoThumb(String fileName) throws FFmpegFrameGrabber.Exception {
		FFmpegFrameGrabber frameGrabber = FFmpegFrameGrabber.createDefault(fileName);

		int lengthInFrames = frameGrabber.getLengthInFrames();
		Messages.sprintf("=======lengthInFrames" + lengthInFrames);
		return null;
	}
	private static List<Double> grabListOfTimeLine(double duration) {
		List<Double> list = new ArrayList<>();
		if (duration <= 5) {
			list.add(0.0);
		} else if (duration <= 10 && duration >= 6) {
			double startPos = 0;
			double ratio = (duration / 2); //
			list.add(startPos);
			for (int i = 0; i < 1; i++) {
				startPos += ratio;
				list.add(startPos);
			}
		} else {
			double startPos = (duration / 10); // = 3. 10% from duration. This could avoid black screen as a start image
			double ratio = ((duration - startPos) / 5); //
			list.add(startPos);
			for (int i = 0; i < 4; i++) {
				startPos += ratio;
				list.add(startPos);
			}
		}
		return list;
	}

	public static double getVideoLenght(File file) {
		Messages.sprintf("getVideoLenght: " + file);
		Format f = null;
		try {
			f = JCodecUtil.detectFormat(file);
			Demuxer d = JCodecUtil.createDemuxer(f, file);
			DemuxerTrack vt = d.getVideoTracks().get(0);
			DemuxerTrackMeta dtm = vt.getMeta();
			return dtm.getTotalDuration();
		} catch (IOException e) {
			e.printStackTrace();
			return -1;
		}
	}
}