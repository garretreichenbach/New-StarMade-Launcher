package smlauncher.mainui;

import javax.swing.*;
import java.awt.*;

/**
 * A panel for controlling game memory with a slider.
 *
 * @author TheDerpGamer
 * @author SlavSquatSuperstar
 */
public class MemorySliderPanel extends JPanel {

	private final JSlider memorySlider;

	public MemorySliderPanel() {
		setDoubleBuffered(true);
		setOpaque(true);
		setLayout(new BorderLayout());
//		setBackground(Palette.paneColor);
//		setForeground(Palette.foregroundColor);

		JLabel sliderLabel = new JLabel();
//		sliderLabel.setBackground(Palette.paneColor);
		sliderLabel.setDoubleBuffered(true);
		sliderLabel.setOpaque(true);
		sliderLabel.setFont(new Font("Roboto", Font.BOLD, 12));
		sliderLabel.setHorizontalAlignment(SwingConstants.CENTER);
		add(sliderLabel, BorderLayout.NORTH);

		memorySlider = new MemorySlider();
		memorySlider.addChangeListener(e1 -> sliderLabel.setText("Memory: " + memorySlider.getValue() + " MB"));
		sliderLabel.setText("Memory: " + memorySlider.getValue() + " MB");
		add(memorySlider, BorderLayout.CENTER);
	}

	public int getSliderValue() {
		return memorySlider.getValue();
	}

}
