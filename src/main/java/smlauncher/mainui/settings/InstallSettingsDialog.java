package smlauncher.mainui.settings;

import smlauncher.LaunchSettings;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;

/**
 * A dialog that lets the user set the installation directory for the game.
 *
 * @author TheDerpGamer
 * @author SlavSquatSuperstar
 */
public class InstallSettingsDialog extends SettingsDialog {

	public InstallSettingsDialog(
			String title, int width, int height,
			ActionListener repairAction
	) {
		super(title, width, height);

		// Install directory
		InstallDirectoryPanel installLabelPanel = new InstallDirectoryPanel();
		addToDialog(installLabelPanel, BorderLayout.NORTH);

		// Repair game files
		JPanel repairPanel = new JPanel();
		repairPanel.setDoubleBuffered(true);
		repairPanel.setOpaque(false);
		repairPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
		addToDialog(repairPanel, BorderLayout.CENTER);

		JButton repairButton = new JButton("Repair");
		repairButton.setIcon(UIManager.getIcon("FileView.checkIcon"));
		repairButton.setDoubleBuffered(true);
		repairButton.setOpaque(false);
		repairButton.setFont(new Font("Roboto", Font.BOLD, 12));
		repairButton.addActionListener(repairAction);
		repairPanel.add(repairButton);

		// Save or cancel changes
		JPanel buttonPanel = createSaveSettingsPanel(
				e -> {
					LaunchSettings.setInstallDir(installLabelPanel.getInstallDir());
					LaunchSettings.saveSettings();
					dispose();
				},
				e -> dispose()
		);
		addToDialog(buttonPanel, BorderLayout.SOUTH);
	}

}
