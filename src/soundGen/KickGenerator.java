package soundGen;

import java.util.HashMap;
import java.util.Map;

import sequencer.ClassicSequencer;
import sequencer.ConfiguredDiffSequencer;
import sequencer.ConfiguredSequencer;
import sequencer.RandomSequencer;
import sequencer.Sequencer;

public class KickGenerator {

	private long currentMonoSampleIndex = 0L;
	private long nextDropSampleIndex = 0L;
	private long thisAttackSampleIndex = 0L;
	private long thisFlatSampleIndex = 0L;

	private double oscillatorTime = 0.0;
	private double pitchLFOTime = 0.0;
	private double phaseLFOTime = 0.0;

	private double tempo = 240.0;
	private double tempoMul = 1.0;
	private double freqMax = 6000.0;
	private double freqMin = 50.0;
	private double kickDropLengthMultiplier = 0.2;
	private double kickDropPowerExponent = 1.5;
	private double pitchLFO = 0.0;
	private double pitchSpeedLFO = 0.5;
	private double phaseLFO = 0.0;
	private double phaseSpeedLFO = 0.5;

	private double distortionValue = 1.0;

	private int sampleRate;

	public static Map<String, Sequencer> sequencers = new HashMap<>();
	static {
		sequencers.put(ConfiguredDiffSequencer.name, new ConfiguredDiffSequencer("seqConfigAssignment.dat"));
		sequencers.put(ConfiguredSequencer.name, new ConfiguredSequencer("seqConfig.dat"));
		sequencers.put(ClassicSequencer.name, new ClassicSequencer());
		sequencers.put(RandomSequencer.name, new RandomSequencer());
	};
	private String sequencer = ConfiguredSequencer.name;

	public KickGenerator(int sampleRate) {
		tempo = main.PiepMachine.DEFAULT_TEMPO;
		freqMax = main.PiepMachine.DEF_MAX_FREQ;
		freqMin = main.PiepMachine.DEF_MIN_FREQ;
		kickDropLengthMultiplier = main.PiepMachine.DEF_DROP_LENGTH;
		kickDropPowerExponent = main.PiepMachine.DEF_DROP_EXPONENT;
		distortionValue = main.PiepMachine.DEF_DIST_LEVEL;
		pitchLFO = main.PiepMachine.DEF_PITCH_LFO;
		pitchSpeedLFO = main.PiepMachine.DEF_PITCH_SPEED_LFO;
		phaseLFO = main.PiepMachine.DEF_PHASE_LFO;
		phaseSpeedLFO = main.PiepMachine.DEF_PHASE_SPEED_LFO;
		this.sampleRate = sampleRate;
	}

	public void fill(byte[] buffer) {
		double distValue = 1.0 / Math.pow(10.0, distortionValue);
		for( int i = 0; i < buffer.length; i += 4 ) {
			if (currentMonoSampleIndex >= nextDropSampleIndex) {
				Sequencer seq = sequencers.get(sequencer);
				if (seq != null) {
					seq.update(this);
				}
				setBeatNow();
			}
			double pitchValue = Math.sin( pitchLFOTime * 2.0 * Math.PI ) * pitchLFO * 0.5 + 0.75;
			double phaseValue = Math.sin( phaseLFOTime * 2.0 * Math.PI ) * phaseLFO * Math.PI;
			pitchLFOTime += pitchSpeedLFO / (double) sampleRate;
			phaseLFOTime += phaseSpeedLFO / (double) sampleRate;
			double freq = getCurrentFrequency() * pitchValue;
			double angle = oscillatorTime * 2.0 * Math.PI;
			oscillatorTime += freq / (double) sampleRate;
			double sineValueL = Math.sin( angle - phaseValue );
			double sineValueR = Math.sin( angle + phaseValue );
			int valueL = ( int )( Math.pow(Math.abs(sineValueL), distValue) * 32000 * Math.signum(sineValueL) );
			int valueR = ( int )( Math.pow(Math.abs(sineValueR), distValue) * 32000 * Math.signum(sineValueR) );
			buffer[ i ] = ( byte )( valueL );
			buffer[ i + 1 ] = ( byte )( valueL >> 8 );
			buffer[ i + 2 ] = ( byte )( valueR );
			buffer[ i + 3 ] = ( byte )( valueR >> 8 );
			currentMonoSampleIndex++;
		}
	}

	public void generateToFile(WaveStream out, int sequenceNumber) {
		Sequencer seq = sequencers.get(sequencer);
		if (seq != null) {
			seq.setSequences(sequenceNumber);
		}
		double distValue = 1.0 / Math.pow(10.0, distortionValue);
		boolean done = false;
		while (!done) {
			byte[] buffer = new byte[512];
			for( int i = 0; i < buffer.length; i += 4 ) {
				if (currentMonoSampleIndex >= nextDropSampleIndex) {
					if (seq != null) {
						done = !seq.listUpdate(this);
						if (done) {
							break;
						}
					}
					setBeatNow();
				}
				double pitchValue = Math.sin( pitchLFOTime * 2.0 * Math.PI ) * pitchLFO * 0.5 + 0.75;
				double phaseValue = Math.sin( phaseLFOTime * 2.0 * Math.PI ) * phaseLFO * Math.PI;
				pitchLFOTime += pitchSpeedLFO / (double) sampleRate;
				phaseLFOTime += phaseSpeedLFO / (double) sampleRate;
				double freq = getCurrentFrequency() * pitchValue;
				double angle = oscillatorTime * 2.0 * Math.PI;
				oscillatorTime += freq / (double) sampleRate;
				double sineValueL = Math.sin( angle - phaseValue );
				double sineValueR = Math.sin( angle + phaseValue );
				int valueL = ( int )( Math.pow(Math.abs(sineValueL), distValue) * 32000 * Math.signum(sineValueL) );
				int valueR = ( int )( Math.pow(Math.abs(sineValueR), distValue) * 32000 * Math.signum(sineValueR) );
				buffer[ i ] = ( byte )( valueL );
				buffer[ i + 1 ] = ( byte )( valueL >> 8 );
				buffer[ i + 2 ] = ( byte )( valueR );
				buffer[ i + 3 ] = ( byte )( valueR >> 8 );
				currentMonoSampleIndex++;
			}
			out.write(buffer);
		}
	}

	private void setBeatNow() {
		double bps = tempo / 60.0;
		double bpsMul = bps * tempoMul;
		long kickLengthInSamples = ( long )( ( double ) sampleRate / bps );
		long kickLengthInSamplesMul = ( long )( ( double ) sampleRate / bpsMul );
		nextDropSampleIndex += kickLengthInSamplesMul;
		thisAttackSampleIndex = currentMonoSampleIndex;
		thisFlatSampleIndex = thisAttackSampleIndex + (long) (kickLengthInSamples * kickDropLengthMultiplier);
	}

	private double getCurrentFrequency() {
		if (currentMonoSampleIndex >= thisFlatSampleIndex) {
			return freqMin;
		}
		double sampleAttackOffset = currentMonoSampleIndex - thisAttackSampleIndex;
		double attackOffset = sampleAttackOffset / ( double )( thisFlatSampleIndex - thisAttackSampleIndex );
		attackOffset = Math.pow(attackOffset, kickDropPowerExponent);
		return freqMin + ((1.0 - attackOffset) * (freqMax - freqMin));
	}

	public void setTempo(double bpm) {
		tempo = bpm;
	}

	public void setMaxFreq(double value) {
		freqMax = value;
	}

	public void setMinFreq(double value) {
		freqMin = value;
	}

	public void setDropLength(double value) {
		kickDropLengthMultiplier = value;
	}

	public void setKickDropPowerExponent(double value) {
		kickDropPowerExponent = value;
	}

	public void setDistortionValue(double value) {
		distortionValue = value;
	}

	public void setTempoMul(double value) {
		tempoMul = value;
	}

	public void setSequencer(String s) {
		sequencer = s;
	}

	public void setPitchLFO(double value) {
		pitchLFO = value;
	}

	public void setPhaseLFO(double value) {
		phaseLFO = value;
	}

	public void setPitchSpeedLFO(double value) {
		pitchSpeedLFO = value;
	}

	public void setPhaseSpeedLFO(double value) {
		phaseSpeedLFO = value;
	}

	public double getTempo() {
		return tempo;
	}
	public double getMaxFreq() {
		return freqMax;
	}
	public double getMinFreq() {
		return freqMin;
	}
	public double getDropLength() {
		return kickDropLengthMultiplier;
	}
	public double getDropExp() {
		return kickDropPowerExponent;
	}
	public double getDistortion() {
		return distortionValue;
	}
	public String getSequencer() {
		return sequencer;
	}

	public double getPitchLFO() {
		return pitchLFO;
	}

	public double getPhaseLFO() {
		return phaseLFO;
	}

	public double getPitchSpeedLFO() {
		return pitchSpeedLFO;
	}

	public double getPhaseSpeedLFO() {
		return phaseSpeedLFO;
	}

	public void rewind() {
		currentMonoSampleIndex = 0L;
		nextDropSampleIndex = 0L;
		thisAttackSampleIndex = 0L;
		thisFlatSampleIndex = 0L;
		oscillatorTime = 0.0;
		pitchLFOTime = 0.0;
		phaseLFOTime = 0.0;
	}

}
