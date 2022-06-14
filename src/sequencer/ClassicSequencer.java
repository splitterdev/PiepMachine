package sequencer;

import java.util.ArrayList;
import java.util.List;

import soundGen.KickGenerator;

public class ClassicSequencer implements Sequencer {

	public static double tempoMuls[] = {0.5, 1.0, 2.0, 4.0, 8.0};
	public static double tempoGenProb[] = {0.25, 8.0, 2.0, 1.0, 0.25};
	public static String name = "Section random";

	private List<Double> sequence = new ArrayList<>();
	private int seqCursor = 0;

	private List<Double> getNextSequence(int len) {
		double minTempoMul = tempoMuls[0];
		List<Double> seq = new ArrayList<>();
		for (int i = 0; i < len / minTempoMul; i++) {
			double fullProb = 0.0;
			for (int j = 0; j < tempoGenProb.length; j++) {
				fullProb += tempoGenProb[j];
			}
			double tempoSelect = Math.random() * fullProb;
			double value = 1.0;
			double probCheck = 0.0;
			for (int j = 0; j < tempoGenProb.length; j++) {
				probCheck += tempoGenProb[j];
				if (tempoSelect < probCheck) {
					value = tempoMuls[j];
					break;
				}
			}
			for (int j = 0; j < value / minTempoMul; j++) {
				seq.add(value);
			}
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
