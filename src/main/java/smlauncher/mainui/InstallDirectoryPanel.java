package smlauncher.mainui;

import smlauncher.LaunchSettings;

import javax.swing.*;
import java.awt.*;
import java.io.File;

/**
 * A text field for setting the install directory of the game.
 *
 * @author TheDerpGamer
 * @author SlavSquatSuperstar
 */
public class InstallDirectoryPanel extends JPanel {

	private final JTextField installDirField;

	public InstallDirectoryPanel() {
		setDoubleBuffered(true);
		setOpaque(false);
		setLayout(new FlowLayout(FlowLayout.CENTER));

		JLabel installLabel = new JLabel("Install Directory: ");
		installLabel.setDoubleBuffered(true);
		installLabel.setOpaque(false);
		installLabel.setFont(new Font("Roboto", Font.BOLD, 12));
		add(installLabel);

		installDirField = new JTextField(LaunchSettings.getInstallDir());
		installDirField.setDoubleBuffered(true);
		installDirField.setOpaque(false);
		installDirField.setFont(new Font("Roboto", Font.PLAIN, 12));
		installDirField.setMinimumSize(new Dimension(200, 20));
		installDirField.setPreferredSize(new Dimension(200, 20));
		installDirField.setMaximumSize(new Dimension(200, 20));
		installDirField.addActionListener(e -> {
			//Set the install path by typing
			String path = installDirField.getText();
			if (path == null || path.isEmpty()) return;
			File file = new File(path);
			if (!file.exists()) return;
			if (!file.isDirectory()) file = file.getParentFile();
			setInstallDir(file.getAbsolutePath());
		});
		add(installDirField);

		JButton setInstallButton = new JButton("Change");
		setInstallButton.setIcon(UIManager.getIcon("FileView.directoryIcon"));
		setInstallButton.setDoubleBuffered(true);
		setInstallButton.setOpaque(false);
		setInstallButton.setContentAreaFilled(false);
		setInstallButton.setBorderPainted(false);
		setInstallButton.setFont(new Font("Roboto", Font.BOLD, 12));
		setInstallButton.addActionListener(e -> {
			//Set the install path through the file chooser
			JFileChooser fileChooser = new JFileChooser(getInstallDir());
			fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			int result = fileChooser.showOpenDialog(this);
			if (result == JFileChooser.APPROVE_OPTION) {
				File file = fileChooser.getSelectedFile();
				if (!file.isDirectory()) file = file.getParentFile();
				setInstallDir(file.getAbsolutePath());
			}
		});
		add(setInstallButton);
	}

	public String getInstallDir() {
		return installDirField.getText();
	}

	private void setInstallDir(String installDir) {
		installDirField.setText(installDir);
	}

}
