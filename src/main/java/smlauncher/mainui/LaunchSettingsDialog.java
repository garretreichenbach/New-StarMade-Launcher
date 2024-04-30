package smlauncher.mainui;

import smlauncher.LaunchSettings;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;

/**
 * A dialog that lets the user set launch settings for the game.
 *
 * @author TheDerpGamer
 * @author SlavSquatSuperstar
 */
public class LaunchSettingsDialog extends JDialog {

	public LaunchSettingsDialog(String title, int width, int height) {
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		setModal(true);
		setResizable(false);
		setTitle(title);
		setSize(width, height);
		setLocationRelativeTo(null);
		setLayout(new BorderLayout());
		setAlwaysOnTop(true);
//		setBackground(Palette.paneColor);
//		setForeground(Palette.foregroundColor);

		JPanel dialogPanel = new JPanel();
		dialogPanel.setDoubleBuffered(true);
		dialogPanel.setOpaque(true);
//		dialogPanel.setBackground(Palette.paneColor);
//		dialogPanel.setForeground(Palette.foregroundColor);
		dialogPanel.setLayout(new BorderLayout());
		add(dialogPanel);

		// Memory
		MemorySliderPanel northPanel = new MemorySliderPanel();
		dialogPanel.add(northPanel, BorderLayout.NORTH);

		// Launch args
		LaunchArgsPanel centerPanel = new LaunchArgsPanel();
		dialogPanel.add(centerPanel, BorderLayout.CENTER);

		// Buttons
		JPanel buttonPanel = createButtonPanel(
				e -> {
					LaunchSettings.setMemory(northPanel.getSliderValue());
					LaunchSettings.setLaunchArgs(centerPanel.getArgsText());
					LaunchSettings.saveSettings();
					dispose();
				},
				e -> dispose()
		);
		dialogPanel.add(buttonPanel, BorderLayout.SOUTH);
	}

	private static JPanel createButtonPanel(
			ActionListener saveAction, ActionListener cancelAction
	) {
		JPanel buttonPanel = new JPanel();
		buttonPanel.setDoubleBuffered(true);
		buttonPanel.setOpaque(true);
		buttonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));

		JButton saveButton = new JButton("Save");
		saveButton.setFont(new Font("Roboto", Font.BOLD, 12));
		saveButton.setDoubleBuffered(true);
		buttonPanel.add(saveButton);

		JButton cancelButton = new JButton("Cancel");
		cancelButton.setFont(new Font("Roboto", Font.BOLD, 12));
		cancelButton.setDoubleBuffered(true);
		buttonPanel.add(cancelButton);
		saveButton.addActionListener(saveAction);
		cancelButton.addActionListener(cancelAction);
		return buttonPanel;
	}

}
