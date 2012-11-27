package wb.prod.xuggle;

import java.io.File;

import com.xuggle.mediatool.IMediaReader;
import com.xuggle.mediatool.IMediaViewer;
import com.xuggle.mediatool.IMediaWriter;
import com.xuggle.mediatool.MediaToolAdapter;
import com.xuggle.mediatool.ToolFactory;
import com.xuggle.mediatool.event.AudioSamplesEvent;
import com.xuggle.mediatool.event.IAddStreamEvent;
import com.xuggle.mediatool.event.IAudioSamplesEvent;
import com.xuggle.mediatool.event.ICloseCoderEvent;
import com.xuggle.mediatool.event.ICloseEvent;
import com.xuggle.mediatool.event.IOpenCoderEvent;
import com.xuggle.mediatool.event.IOpenEvent;
import com.xuggle.xuggler.IAudioSamples;

import static java.lang.System.out;
import static java.lang.System.exit;

import static java.util.concurrent.TimeUnit.SECONDS;
import static com.xuggle.xuggler.Global.DEFAULT_TIME_UNIT;

/**
 * A very simple media transcoder which uses {@link IMediaReader},
 * {@link IMediaWriter} and {@link IMediaViewer}.
 */

public class ConcatenateAudio {
	
	/**
	 * Concatenate two files.
	 * 
	 * @param args
	 *            3 strings; an input file 1, input file 2, and an output file.
	 */
	public static void main(String[] args) {
		
		final File root = new File("C:\\Work\\WB\\work\\recordings\\1353561762563-487");

		File source1 = new File(root, "oldest-story.wav");
		File source2 = new File(root, "whole-world-using.wav");
		File target = new File(root, "z1.wav");

		if (!source1.exists()) {
			out.println("Source file does not exist: " + source1);
			exit(0);
		}

		if (!source2.exists()) {
			out.println("Source file does not exist: " + source2);
			exit(0);
		}
		
		concatenate(source1, source2, target);
	}

	/**
	 * Concatenate two source files into one destination file.
	 * 
	 * @param sourceUrl1
	 *            the file which will appear first in the output
	 * @param sourceUrl2
	 *            the file which will appear second in the output
	 * @param destinationUrl
	 *            the file which will be produced
	 */

	public static void concatenate(File sourceFile1, File sourceFile2,
			File destinationFile) {
		out.printf("transcode %s + %s -> %s\n", sourceFile1, sourceFile2,
				destinationFile);

		// ////////////////////////////////////////////////////////////////////
		// //
		// NOTE: be sure that the audio and video parameters match those of //
		// your input media //
		// //
		// ////////////////////////////////////////////////////////////////////

		// audio parameters

		final int audioStreamIndex = 0; // 1
		final int audioStreamId = 0;
		final int channelCount = 1; // 2
		final int sampleRate = 22050; // Hz

		// create the first media reader

		IMediaReader reader1 = ToolFactory.makeReader(sourceFile1.toString());

		// create the second media reader

		IMediaReader reader2 = ToolFactory.makeReader(sourceFile2.toString());

		// create the media concatenator

		MediaConcatenator concatenator = new MediaConcatenator(audioStreamIndex);

		// concatenator listens to both readers

		reader1.addListener(concatenator);
		reader2.addListener(concatenator);

		// create the media writer which listens to the concatenator

		IMediaWriter writer = ToolFactory.makeWriter(destinationFile.toString());
		concatenator.addListener(writer);

		// add the audio stream

		writer.addAudioStream(audioStreamIndex, audioStreamId, channelCount,
				sampleRate);

		// read packets from the first source file until done

		while (reader1.readPacket() == null)
			;
		
//		concatenator.
		
		final long duration = DEFAULT_TIME_UNIT.convert(5, SECONDS);
		System.out.println("duration: " + duration);

		/*
		long blank = IAudioSamples.defaultPtsToSamples(duration, sampleRate);
		IAudioSamples blankSamples = IAudioSamples.make(blank, channelCount);
		blankSamples.setTimeStamp(concatenator.mNextAudio);
		blankSamples.setPts(concatenator.mNextAudio);
		concatenator.mNextAudio = blankSamples.getNextPts();
		writer.encodeAudio(audioStreamIndex, blankSamples);
		concatenator.mOffset = concatenator.mNextAudio;
		 */
		
		long totalSampleCount = 0L;
		long clock = 0L;
		for (; clock < duration; clock = IAudioSamples
				.samplesToDefaultPts(totalSampleCount, sampleRate)) {

			// compute and encode the audio for the balls

			short[] samples = new short[1000]; // balls.getAudioFrame(sampleRate);
			writer.encodeAudio(audioStreamIndex, samples, concatenator.mNextAudio + clock, 
					DEFAULT_TIME_UNIT);
			
			totalSampleCount += samples.length;
		}		
	      
		concatenator.mNextAudio += clock;
		concatenator.mOffset = concatenator.mNextAudio;

		// read packets from the second source file until done

		while (reader2.readPacket() == null)
			;

		// close the writer

		writer.close();
	}

	static class MediaConcatenator extends MediaToolAdapter {
		// the current offset

		private long mOffset = 0;

		// the next audio timestamp

		private long mNextAudio = 0;

		// the index of the audio stream

		private final int mAudoStreamIndex;

		/**
		 * Create a concatenator.
		 * 
		 * @param audioStreamIndex
		 *            index of audio stream
		 */

		public MediaConcatenator(int audioStreamIndex) {
			mAudoStreamIndex = audioStreamIndex;
		}

		public void onAudioSamples(IAudioSamplesEvent event) {
			IAudioSamples samples = event.getAudioSamples();
			
			// set the new time stamp to the original plus the offset
			// established
			// for this media file

			long newTimeStamp = samples.getTimeStamp() + mOffset;

			// keep track of predicted time of the next audio samples, if the
			// end
			// of the media file is encountered, then the offset will be
			// adjusted
			// to this time.

			mNextAudio = samples.getNextPts();

			// set the new timestamp on audio samples

			samples.setTimeStamp(newTimeStamp);

			// create a new audio samples event with the one true audio stream
			// index

			super.onAudioSamples(new AudioSamplesEvent(this, samples,
					mAudoStreamIndex));
		}

		public void onClose(ICloseEvent event) {
			// update the offset by the larger of the next expected audio or
			// frame time

			mOffset = mNextAudio;
		}

		public void onAddStream(IAddStreamEvent event) {
			// overridden to ensure that add stream events are not passed down
			// the tool chain to the writer, which could cause problems
		}

		public void onOpen(IOpenEvent event) {
			// overridden to ensure that open events are not passed down the
			// tool
			// chain to the writer, which could cause problems
		}

		public void onOpenCoder(IOpenCoderEvent event) {
			// overridden to ensure that open coder events are not passed down
			// the
			// tool chain to the writer, which could cause problems
		}

		public void onCloseCoder(ICloseCoderEvent event) {
			// overridden to ensure that close coder events are not passed down
			// the
			// tool chain to the writer, which could cause problems
		}

	}

}
