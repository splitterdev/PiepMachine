package main;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import sequencer.ClassicSequencer;
import sequencer.Sequencer;
import soundGen.KickGenerator;
import soundGen.SoundHelper;

public class PiepMachine {

	public static double DEFAULT_TEMPO = 240;
	public static double DEF_MAX_FREQ = 5000;
	public static double DEF_MIN_FREQ = 50;
	public static double DEF_DROP_LENGTH = 0.3;
	public static double DEF_DROP_EXPONENT = 1.0;
	public static double DEF_DIST_LEVEL = 1.0;
	public static double DEF_PITCH_LFO = 0.0;
	public static double DEF_PITCH_SPEED_LFO = 0.5;
	public static double DEF_PHASE_LFO = 0.0;
	public static double DEF_PHASE_SPEED_LFO = 0.5;

	private static String USER_CONFIG_FILE = "userConfig.dat";

	private static double readDouble(BufferedReader reader) throws NumberFormatException, IOException {
		return Double.parseDouble(reader.readLine());
	}

	public static void readConfig() {
		File f = new File(USER_CONFIG_FILE);
		if (!f.exists()) {
			return;
		}
		try (BufferedReader reader = new BufferedReader(new FileReader(f))) {
			String sequencer = reader.readLine();
			DEFAULT_TEMPO = readDouble(reader);
			DEF_MAX_FREQ = readDouble(reader);
			DEF_MIN_FREQ = readDouble(reader);
			DEF_DROP_LENGTH = readDouble(reader);
			DEF_DROP_EXPONENT = readDouble(reader);
			DEF_DIST_LEVEL = readDouble(reader);
			DEF_PITCH_LFO = readDouble(reader);
			DEF_PITCH_SPEED_LFO = readDouble(reader);
			DEF_PHASE_LFO = readDouble(reader);
			DEF_PHASE_SPEED_LFO = readDouble(reader);
			for (int i = 0; i < 5; i++) {
				ClassicSequencer.tempoGenProb[i] = readDouble(reader);
			}
			SoundHelper.getGenerator().setSequencer(sequencer);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void saveConfig() throws IOException {
		try (BufferedWriter writer = new BufferedWriter(new FileWriter(new File(USER_CONFIG_FILE)))) {
			writer.write(SoundHelper.getGenerator().getSequencer());
			writer.newLine();
			writer.write("" + SoundHelper.getGenerator().getTempo());
			writer.newLine();
			writer.write("" + SoundHelper.getGenerator().getMaxFreq());
			writer.newLine();
			writer.write("" + SoundHelper.getGenerator().getMinFreq());
			writer.newLine();
			writer.write("" + SoundHelper.getGenerator().getDropLength());
			writer.newLine();
			writer.write("" + SoundHelper.getGenerator().getDropExp());
			writer.newLine();
			writer.write("" + SoundHelper.getGenerator().getDistortion());
			writer.newLine();
			writer.write("" + SoundHelper.getGenerator().getPitchLFO());
			writer.newLine();
			writer.write("" + SoundHelper.getGenerator().getPitchSpeedLFO());
			writer.newLine();
			writer.write("" + SoundHelper.getGenerator().getPhaseLFO());
			writer.newLine();
			writer.write("" + SoundHelper.getGenerator().getPhaseSpeedLFO());
			writer.newLine();
			for (int i = 0; i < ClassicSequencer.tempoGenProb.length; i++) {
				writer.write("" + ClassicSequencer.tempoGenProb[i]);
				writer.newLine();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		readConfig();
		SoundHelper.init();
		final JFrame frame = new JFrame("Piep Machine b610");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				try {
					saveConfig();
					System.out.println("Config saved.");
					SoundHelper.stopRecording();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
				SoundHelper.stop();
				e.getWindow().dispose();
			}
        });
		frame.setSize(640, 480);
		frame.add(getKickControlPanel(), BorderLayout.PAGE_START);
		frame.add(getSequencerProbabilities(), BorderLayout.CENTER);
		frame.add(getPlayPanel(), BorderLayout.PAGE_END);
		frame.setVisible(true);
	}

	private static Component getKickControlPanel() {
		JPanel panel = new JPanel(new GridLayout(10, 1));
		panel.add(getSequencerSelector());
		panel.add(getTempoSlider());
		panel.add(getMaxFreqSlider());
		panel.add(getMinFreqSlider());
		panel.add(getDropLengthSlider());
		panel.add(getDropExponentSlider());
		panel.add(getDistortionSlider());
		panel.add(getPitchSlider());
		panel.add(getPhaseSlider());
		return panel;
	}

	private static Component getSequencerSelector() {
		JPanel panel = new JPanel(new BorderLayout());
		JLabel label = new JLabel("Sequencer");
		JComboBox<Sequencer> selector = new JComboBox<>();
		for (Sequencer v : KickGenerator.sequencers.values()) {
			selector.addItem(v);
		}
		selector.setSelectedItem(KickGenerator.sequencers.get(SoundHelper.getGenerator().getSequencer()));
		selector.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				String s = e.getItem().toString();
				SoundHelper.getGenerator().setSequencer(s);
			}
		});
		panel.add(label, BorderLayout.LINE_START);
		panel.add(selector, BorderLayout.CENTER);
		return panel;
	}

	private static Component getTempoSlider() {
		JPanel panel = new JPanel(new BorderLayout());
		JLabel label = new JLabel("" + DEFAULT_TEMPO + " BPM ");
		JSlider slider = new JSlider(150, 450, (int) DEFAULT_TEMPO);
		slider.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				int value = slider.getValue();
				label.setText("" + value + " BPM ");
				SoundHelper.getGenerator().setTempo(value);
			}
		});
		panel.add(label, BorderLayout.LINE_START);
		panel.add(slider, BorderLayout.CENTER);
		return panel;
	}

	private static JSlider getQuantizedDoubleSlider(double min, double max, double def, double unitQuantization) {
		return new JSlider((int)(min * unitQuantization), (int)(max * unitQuantization), (int)(def * unitQuantization));
	}

	private static JSlider getLogarithmicSlider(double rMin, double rMax, double rDef, double base, double quant) {
		int min = (int)(Math.log(rMin) / Math.log(base) * quant);
		int max = (int)(Math.log(rMax) / Math.log(base) * quant);
		int def = (int)(Math.log(rDef) / Math.log(base) * quant);
		System.out.println("Min = " + min + " / Max = " + max + " / Def = " + def);
		return new JSlider(min, max, def);
	}

	private static double getLogarithmicValue(JSlider slider, double base, double quant) {
		return Math.pow(base, (double) slider.getValue() / quant);
	}

	private static Component getMaxFreqSlider() {
		JPanel panel = new JPanel(new BorderLayout());
		JLabel label = new JLabel("Max " + (int) DEF_MAX_FREQ + " Hz ");
		double base = 2.0;
		double quantization = 100.0;
		JSlider slider = getLogarithmicSlider(16, 12000, DEF_MAX_FREQ, base, quantization);
		slider.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				int value = (int) getLogarithmicValue(slider, base, quantization);
				label.setText("Max " + value + " Hz ");
				SoundHelper.getGenerator().setMaxFreq(value);
			}
		});
		panel.add(label, BorderLayout.LINE_START);
		panel.add(slider, BorderLayout.CENTER);
		return panel;
	}

	private static Component getMinFreqSlider() {
		JPanel panel = new JPanel(new BorderLayout());
		JLabel label = new JLabel("Min " + (int) DEF_MIN_FREQ + " Hz ");
		double base = 2.0;
		double quantization = 100.0;
		JSlider slider = getLogarithmicSlider(16, 12000, DEF_MIN_FREQ, base, quantization);
		slider.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				int value = (int) getLogarithmicValue(slider, base, quantization);
				label.setText("Min " + value + " Hz ");
				SoundHelper.getGenerator().setMinFreq(value);
			}
		});
		panel.add(label, BorderLayout.LINE_START);
		panel.add(slider, BorderLayout.CENTER);
		return panel;
	}

	private static Component getDropLengthSlider() {
		JPanel panel = new JPanel(new BorderLayout());
		JLabel label = new JLabel("Length x" + DEF_DROP_LENGTH + " ");
		double quantization = 100;
		JSlider slider = getQuantizedDoubleSlider(0.0, 1.0, DEF_DROP_LENGTH, quantization);
		slider.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				double value = (double) slider.getValue() / quantization;
				label.setText("Length x" + value + " ");
				SoundHelper.getGenerator().setDropLength(value);
			}
		});
		panel.add(label, BorderLayout.LINE_START);
		panel.add(slider, BorderLayout.CENTER);
		return panel;
	}

	private static Component getDropExponentSlider() {
		JPanel panel = new JPanel(new BorderLayout());
		JLabel label = new JLabel("Attack x" + DEF_DROP_EXPONENT + " ");
		double quantization = 10;
		JSlider slider = getQuantizedDoubleSlider(0.1, 4.0, DEF_DROP_EXPONENT, quantization);
		slider.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				double value = (double) slider.getValue() / quantization;
				label.setText("Attack x" + value + " ");
				SoundHelper.getGenerator().setKickDropPowerExponent(value);
			}
		});
		panel.add(label, BorderLayout.LINE_START);
		panel.add(slider, BorderLayout.CENTER);
		return panel;
	}

	private static Component getDistortionSlider() {
		JPanel panel = new JPanel(new BorderLayout());
		JLabel label = new JLabel("Dist x" + DEF_DIST_LEVEL + " ");
		double quantization = 20;
		JSlider slider = getQuantizedDoubleSlider(0.0, 3.0, DEF_DIST_LEVEL, quantization);
		slider.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				double value = (double) slider.getValue() / quantization;
				label.setText("Dist x" + value + " ");
				SoundHelper.getGenerator().setDistortionValue(value);
			}
		});
		panel.add(label, BorderLayout.LINE_START);
		panel.add(slider, BorderLayout.CENTER);
		return panel;
	}

	private static Component getPitchSlider() {
		JPanel panel = new JPanel(new GridLayout(1, 2));
		panel.add(getPitchLFOSlider());
		panel.add(getPitchLFOVolumeSlider());
		return panel;
	}

	private static Component getPhaseSlider() {
		JPanel panel = new JPanel(new GridLayout(1, 2));
		panel.add(getPhaseLFOSlider());
		panel.add(getPhaseLFOVolumeSlider());
		return panel;
	}

	private static Component getPitchLFOSlider() {
		JPanel panel = new JPanel(new BorderLayout());
		JLabel label = new JLabel("Pitch LFO " + formatValue(DEF_PITCH_SPEED_LFO) + " Hz ");
		double base = 2.0;
		double quantization = 100.0;
		JSlider slider = getLogarithmicSlider(0.5, 120.0, DEF_PITCH_SPEED_LFO, base, quantization);
		slider.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				double value = getLogarithmicValue(slider, base, quantization);
				label.setText("Pitch LFO " + formatValue(value) + " Hz ");
				SoundHelper.getGenerator().setPitchSpeedLFO(value);
			}
		});
		panel.add(label, BorderLayout.LINE_START);
		panel.add(slider, BorderLayout.CENTER);
		return panel;
	}

	private static Component getPitchLFOVolumeSlider() {
		JPanel panel = new JPanel(new BorderLayout());
		JLabel label = new JLabel("Amp " + DEF_PITCH_LFO + " ");
		double quantization = 100.0;
		JSlider slider = getQuantizedDoubleSlider(0.0, 1.0, DEF_PITCH_LFO, quantization);
		slider.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				double value = (double) slider.getValue() / quantization;
				label.setText("Amp " + value + " ");
				SoundHelper.getGenerator().setPitchLFO(value);
			}
		});
		panel.add(label, BorderLayout.LINE_START);
		panel.add(slider, BorderLayout.CENTER);
		return panel;
	}

	private static Component getPhaseLFOSlider() {
		JPanel panel = new JPanel(new BorderLayout());
		JLabel label = new JLabel("Phase LFO " + formatValue(DEF_PHASE_SPEED_LFO) + " Hz ");
		double base = 2.0;
		double quantization = 100.0;
		JSlider slider = getLogarithmicSlider(0.5, 120.0, DEF_PHASE_SPEED_LFO, base, quantization);
		slider.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				double value = getLogarithmicValue(slider, base, quantization);
				label.setText("Pitch LFO " + formatValue(value) + " Hz ");
				SoundHelper.getGenerator().setPhaseSpeedLFO(value);
			}
		});
		panel.add(label, BorderLayout.LINE_START);
		panel.add(slider, BorderLayout.CENTER);
		return panel;
	}

	private static Component getPhaseLFOVolumeSlider() {
		JPanel panel = new JPanel(new BorderLayout());
		JLabel label = new JLabel("Amp " + DEF_PHASE_LFO + " ");
		double quantization = 100.0;
		JSlider slider = getQuantizedDoubleSlider(0.0, 1.0, DEF_PHASE_LFO, quantization);
		slider.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				double value = (double) slider.getValue() / quantization;
				label.setText("Amp " + value + " ");
				SoundHelper.getGenerator().setPhaseLFO(value);
			}
		});
		panel.add(label, BorderLayout.LINE_START);
		panel.add(slider, BorderLayout.CENTER);
		return panel;
	}

	private static Component getSequencerProbabilities() {
		int limit = ClassicSequencer.tempoGenProb.length;
		JPanel panel = new JPanel(new GridLayout(1, limit));
		for (int i = 0; i < limit; i++) {
			panel.add(getSequencerProbabilitySlider(i));
		}
		return panel;
	}

	private static Component getSequencerProbabilitySlider(int tempoIndex) {
		double probValue = ClassicSequencer.tempoGenProb[tempoIndex];
		JPanel panel = new JPanel(new BorderLayout());
		JLabel label = new JLabel("P(" + ClassicSequencer.tempoMuls[tempoIndex] + ") x" + probValue + " ");
		double quantization = 10;
		JSlider slider = getQuantizedDoubleSlider(0.0, 10.0, probValue, quantization);
		slider.setOrientation(SwingConstants.VERTICAL);
		slider.setPreferredSize(new Dimension(120, 200));
		slider.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				double value = (double) slider.getValue() / quantization;
				label.setText("P(" + ClassicSequencer.tempoMuls[tempoIndex] + ") x" + value + " ");
				ClassicSequencer.tempoGenProb[tempoIndex] = value;
			}
		});
		panel.add(label, BorderLayout.PAGE_START);
		panel.add(slider, BorderLayout.CENTER);
		return panel;
	}

	private static Component getPlayPanel() {
		JPanel panel = new JPanel(new GridLayout(1, 5));
		JButton playButton = new JButton(new ImageIcon("icon/play.png"));
		playButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				SoundHelper.setPlaySound(true);
			}
		});
		panel.add(playButton);
		JButton pauseButton = new JButton(new ImageIcon("icon/pause.png"));
		pauseButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				SoundHelper.setPlaySound(false);
			}
		});
		panel.add(pauseButton);
		JButton recordButton = new JButton(new ImageIcon("icon/record_off.png"));
		recordButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				SoundHelper.toggleRecording();
				if (SoundHelper.isRecording()) {
					recordButton.setIcon(new ImageIcon("icon/record_on.png"));
				} else {
					recordButton.setIcon(new ImageIcon("icon/record_off.png"));
				}
			}
		});
		panel.add(recordButton);
		JPanel subPanel = new JPanel(new BorderLayout());
		JComboBox<Integer> combo = new JComboBox<>(new Integer[]{4, 8, 16, 32, 64, 128, 256});
		combo.setSelectedItem(new Integer(32));
		JButton generateButton = new JButton("Generate sequence:");
		generateButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				SoundHelper.generateSequence((Integer) combo.getSelectedItem());
			}
		});
		subPanel.add(generateButton, BorderLayout.CENTER);
		subPanel.add(combo, BorderLayout.PAGE_END);
		panel.add(subPanel);
		return panel;
	}

	private static String formatValue(double value) {
		return String.format("%.2f", value);
	}

}
