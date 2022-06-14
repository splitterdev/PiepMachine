package soundGen;

import java.io.FileNotFoundException;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

public class SoundHelper {

	private static volatile boolean done = false;
	private static volatile boolean play = false;
	private static final int SAMPLE_RATE = 44100;
	private static KickGenerator generator = null;
	private static WaveStream recorderOutput = null;

	public static KickGenerator getGenerator() {
		if (generator == null) {
			generator = new KickGenerator(SAMPLE_RATE);
		}
		return generator;
	}

	private static class PlayerThread extends Thread {
		@Override
		public void run() {
			int bufferSize = 512;
			while ( !done ) {
				if ( play ) {
					byte[] buffer = new byte[ bufferSize ];
					generator.fill( buffer );
			        sdl.write( buffer, 0, buffer.length );
					if (recorderOutput != null) {
						recorderOutput.write(buffer);
					}
				    sdl.start();
				} else {
					try {
						Thread.sleep(0);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		}
	}

	private static SourceDataLine sdl = null;
	private static PlayerThread playerThread = new PlayerThread();

	public static void init() {
		AudioFormat af = new AudioFormat( ( float ) SAMPLE_RATE, 16, 2, true, false );
	    try {
			sdl = AudioSystem.getSourceDataLine( af );
		    sdl.open();
		    sdl.start();
		    playerThread.start();
		} catch (LineUnavailableException e) {
			e.printStackTrace();
		}
	}

	public static void setPlaySound(boolean playSound) {
		play = playSound;
		if (!play) {
			sdl.flush();
		}
	}

	public static void stop() {
		done = true;
		try {
			playerThread.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		if ( sdl != null ) {
			sdl.stop();
		}
	}

	private static boolean isRecording = false;
	public static void toggleRecording() {
		isRecording = !isRecording;
		if (isRecording) { // was enabled
			stopRecording();
			try {
				recorderOutput = new WaveStream(generateName("recorderOutput", System.currentTimeMillis()));
			} catch (FileNotFoundException e) {
				isRecording = false;
				e.printStackTrace();
			}
		} else { // was disabled
			stopRecording();
		}
	}

	public static void generateSequence(int seqNumber) {
		boolean beforePlay = play;
		try {
			play = false;
			try {
				Thread.sleep(50);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			WaveStream out = new WaveStream(generateName("generatedSequence", System.currentTimeMillis()));
			generator.rewind();
			generator.generateToFile(out, seqNumber);
			generator.rewind();
			out.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} finally {
			play = beforePlay;
		}
	}

	private static String generateName(String initial, long currentTimeMillis) {
		return initial + "-" + currentTimeMillis + ".wav";
	}

	public static void stopRecording() {
		if (recorderOutput != null) {
			recorderOutput.close();
			recorderOutput = null;
		}
	}

	public static boolean isRecording() {
		return isRecording;
	}
	
}
