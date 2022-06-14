package soundGen;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class WaveStream {

	private RandomAccessFile output;

	private final Lock lock = new ReentrantLock(true);

	public WaveStream(String path) throws FileNotFoundException {
		output = new RandomAccessFile(path, "rw");
		writeHeader();
	}

	public boolean write(byte[] buffer) {
		lock.lock();
		try {
			output.write(buffer);
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		} finally {
			lock.unlock();
		}
		return true;
	}

	public void close() {
		lock.lock();
		try {
			output.seek(0);
			writeHeader();
			output.close();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			lock.unlock();
		}
	}

	private void writeHeader() {
		short bps = 16;
		short numChannels = 2;
		int sampleRate = 44100;
		int byteRate = sampleRate * numChannels * bps / 8;
		short blockAlign = (short) (numChannels * bps / 8);
		try {
			output.write(new byte[]{82, 73, 70, 70}); // "RIFF"
			output.writeInt(Integer.reverseBytes((int)(output.length() - 8))); // ChunkSize
			output.write(new byte[]{87, 65, 86, 69}); // "WAVE"
			output.write(new byte[]{102, 109, 116, 32}); // "fmt "
			output.writeInt(Integer.reverseBytes(16)); // Subchunk1Size
			output.writeShort(Short.reverseBytes((short) 1)); // AudioFormat
			output.writeShort(Short.reverseBytes(numChannels)); // NumChannels
			output.writeInt(Integer.reverseBytes(sampleRate)); // SampleRate
			output.writeInt(Integer.reverseBytes(byteRate)); // ByteRate
			output.writeShort(Short.reverseBytes(blockAlign)); // BlockAlign
			output.writeShort(Short.reverseBytes(bps)); // BitsPerSample
			output.write(new byte[]{100, 97, 116, 97}); // "data"
			output.writeInt(Integer.reverseBytes((int)(output.length() - 44))); // Subchunk2Size
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
