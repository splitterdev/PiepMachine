package sequencer;

import java.util.ArrayList;
import java.util.List;

import soundGen.KickGenerator;

public class RandomSequencer implements Sequencer {

	public static String name = "Full random";

	private List<Double> sequence = new ArrayList<>();
	private int seqCursor = 0;

	private List<Double> getNextSequence(int len) {
		List<Double> seq = new ArrayList<>();
		for (int i = 0; i < len; i++) {
			double fullProb = 0.0;
			for (int j = 0; j < ClassicSequencer.tempoGenProb.length; j++) {
				fullProb += ClassicSequencer.tempoGenProb[j];
			}
			double tempoSelect = Math.random() * fullProb;
			double value = 1.0;
			double probCheck = 0.0;
			for (int j = 0; j < ClassicSequencer.tempoGenProb.length; j++) {
				probCheck += ClassicSequencer.tempoGenProb[j];
				if (tempoSelect < probCheck) {
					value = ClassicSequencer.tempoMuls[j];
					break;
				}
			}
			seq.add(value);
		}
		return seq;
	}

	@Override
	public void setSequences(int sequenceNumber) {
		sequence = new ArrayList<>();
		seqCursor = 0;
		for (int i = 0; i < sequenceNumber; i++) {
			sequence.addAll(getNextSequence(1));
		}
	}

	@Override
	public boolean listUpdate(KickGenerator generator) {
		if (seqCursor >= sequence.size()) {
			return false;
		}
		generator.setTempoMul(sequence.get(seqCursor));
		seqCursor++;
		return true;
	}

	@Override
	public void update(KickGenerator generator) {
		if (seqCursor >= sequence.size()) {
			sequence = getNextSequence(1);
			seqCursor = 0;
		}
		generator.setTempoMul(sequence.get(seqCursor));
		seqCursor++;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public String toString() {
		return getName();
	}

}
