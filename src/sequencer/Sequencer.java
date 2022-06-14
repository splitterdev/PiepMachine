package sequencer;

import soundGen.KickGenerator;

public interface Sequencer {
	public String getName();
	public void setSequences(int sequenceNumber);
	public boolean listUpdate(KickGenerator generator);
	public void update(KickGenerator generator);
}
