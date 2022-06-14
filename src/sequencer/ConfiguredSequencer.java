package sequencer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import soundGen.KickGenerator;

public class ConfiguredSequencer implements Sequencer {

	private List<Double> sequence = new ArrayList<>();
	private int seqCursor = 0;
	public static String name = "Predefined section-driven";

	private static class Sequence {
		Sequence(String s, double p) {
			seqConfig = s;
			probability = p;
		}
		String seqConfig;
		double probability;
	}

	public ConfiguredSequencer(String initialFileName) {
		try (BufferedReader reader = new BufferedReader(new FileReader(new File(initialFileName)))) {
			String line;
			while ((line = reader.readLine()) != null) {
				if (line.isEmpty()) {
					continue;
				}
				String[] splits = line.split(",");
				double prob = Double.parseDouble(splits[0]);
				fullProb += prob;
				sectionedSequences.add(new Sequence(splits[1], fullProb));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private List<Sequence> sectionedSequences = new ArrayList<>();
	private double fullProb = 0.0;

	private List<Double> getNextSequence() {
		List<Double> seq = new ArrayList<>();
		double r = Math.random() * fullProb;
		Sequence s = null;
		while (s == null) {
			for (Sequence sequence : sectionedSequences) {
				if (r < sequence.probability) {
					s = sequence;
					break;
				}
			}
		}
		String sequenceConfig = s.seqConfig;
		Map<Character, Double> sectionDef = new HashMap<>();
		for (int i = 0; i < sequenceConfig.length(); i++) {
			Character c = sequenceConfig.charAt(i);
			Double tempoMul = sectionDef.get(c);
			if (tempoMul == null) {
				tempoMul = getRandomTempoMul();
				sectionDef.put(c, tempoMul);
			}
			for (int j = 0; j < tempoMul; j++) {
				seq.add(tempoMul);
			}
		}
		return seq;
	}

	private double getRandomTempoMul() {
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
		return value;
	}

	@Override
	public void setSequences(int sequenceNumber) {
		sequence = new ArrayList<>();
		seqCursor = 0;
		for (int i = 0; i < sequenceNumber; i++) {
			sequence.addAll(getNextSequence());
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
			sequence = getNextSequence();
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
