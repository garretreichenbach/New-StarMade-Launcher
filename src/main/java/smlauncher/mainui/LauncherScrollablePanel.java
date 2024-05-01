package smlauncher.mainui;

import smlauncher.util.Palette;

import javax.swing.*;

/**
 * A generic panel displayed inside a scroll pane.
 *
 * @author SlavSquatSuperstar
 */
public abstract class LauncherScrollablePanel extends JPanel {

	public LauncherScrollablePanel() {
		super(true);
		setBackground(Palette.paneColor);
		setOpaque(true);
	}

	public abstract void updatePanel();

}
