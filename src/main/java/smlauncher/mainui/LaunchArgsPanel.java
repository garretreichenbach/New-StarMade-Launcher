package smlauncher.mainui;

import smlauncher.LaunchSettings;

import javax.swing.*;
import java.awt.*;

/**
 * A panel for controlling launch arguments with a text area.
 *
 * @author TheDerpGamer
 * @author SlavSquatSuperstar
 */
public class LaunchArgsPanel extends JPanel {

	private final JTextArea launchArgsArea;

	public LaunchArgsPanel() {
		setDoubleBuffered(true);
		setOpaque(false);
		setLayout(new BorderLayout());
//		setBackground(Palette.backgroundColor);
//		setForeground(Palette.foregroundColor);

		launchArgsArea = new JTextArea();
//		launchArgsArea.setBackground(Palette.paneColor);
		launchArgsArea.setDoubleBuffered(true);
		launchArgsArea.setOpaque(true);
		launchArgsArea.setText(LaunchSettings.getLaunchArgs());
		launchArgsArea.setLineWrap(true);
		launchArgsArea.setWrapStyleWord(true);
		launchArgsArea.setBorder(BorderFactory.createLineBorder(Color.BLACK));
		add(launchArgsArea, BorderLayout.CENTER);

		JLabel launchArgsLabel = new JLabel("Launch Arguments");
//		launchArgsLabel.setBackground(Palette.paneColor);
		launchArgsLabel.setDoubleBuffered(true);
		launchArgsLabel.setOpaque(true);
		launchArgsLabel.setFont(new Font("Roboto", Font.BOLD, 12));
		launchArgsLabel.setHorizontalAlignment(SwingConstants.CENTER);
		add(launchArgsLabel, BorderLayout.NORTH);
	}

	public String getArgsText() {
		return launchArgsArea.getText();
	}

}
