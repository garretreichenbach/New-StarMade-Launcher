package smlauncher.content;

import smlauncher.util.Palette;

import javax.swing.*;

/**
 * [Description]
 *
 * @author TheDerpGamer
 */
public class LauncherContentPanel extends JPanel {

	public LauncherContentPanel() {
		super(true);
		setBackground(Palette.paneColor);
		setOpaque(true);
	}

	public void updatePanel() {
		removeAll();
	}
}
