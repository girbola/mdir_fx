package com.girbola.imagehandling;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.jcodec.api.FrameGrab;
import org.jcodec.common.Demuxer;
import org.jcodec.common.DemuxerTrack;
import org.jcodec.common.DemuxerTrackMeta;
import org.jcodec.common.Format;
import org.jcodec.common.JCodecUtil;
import org.jcodec.common.io.NIOUtils;
import org.jcodec.common.model.Picture;

import com.girbola.Main;
import com.girbola.imagehandling.jcodec.AWTUtil;
import com.girbola.messages.Messages;

import net.coobird.thumbnailator.Thumbnails;

public class VideoThumb {

	public static List<BufferedImage> getList(File file) {
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

	private static List<BufferedImage> grabBufferedImageList(File file, List<Double> list) {
		List<BufferedImage> buff_list = new ArrayList<>();
		if (list == null) {
			return null;
		}
		if (list.isEmpty()) {
			return null;
		}
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
			e.printStackTrace();
		}
		return buff_list;
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