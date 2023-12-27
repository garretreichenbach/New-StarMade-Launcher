package smlauncher.forums;

import smlauncher.util.Palette;

import javax.swing.*;

/**
 * Panel for displaying forums in the launcher.
 *
 * @author TheDerpGamer
 */
public class LauncherForumsPanel extends JPanel {

	public LauncherForumsPanel() {
		super(true);
		setBackground(Palette.paneColor);
		setOpaque(true);
	}

	public void updatePanel() {
		removeAll();
	}
}
