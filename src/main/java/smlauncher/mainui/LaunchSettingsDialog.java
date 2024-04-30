package smlauncher.mainui;

import smlauncher.LaunchSettings;

import javax.swing.*;
import java.awt.*;

/**
 * A dialog that lets the user set launch settings for the game.
 *
 * @author TheDerpGamer
 * @author SlavSquatSuperstar
 */
public class LaunchSettingsDialog extends SettingsDialog {

	public LaunchSettingsDialog(String title, int width, int height) {
		super(title, width, height);

		// Memory
		MemorySliderPanel northPanel = new MemorySliderPanel();
		addToDialog(northPanel, BorderLayout.NORTH);

		// Launch args
		LaunchArgsPanel centerPanel = new LaunchArgsPanel();
		addToDialog(centerPanel, BorderLayout.CENTER);

		// Save or cancel changes
		JPanel buttonPanel = createSaveSettingsPanel(
				e -> {
					LaunchSettings.setMemory(northPanel.getSliderValue());
					LaunchSettings.setLaunchArgs(centerPanel.getArgsText());
					LaunchSettings.saveSettings();
					dispose();
				},
				e -> dispose()
		);
		addToDialog(buttonPanel, BorderLayout.SOUTH);
	}

}
