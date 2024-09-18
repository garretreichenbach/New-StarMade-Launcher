package smlauncher.mainui.scrolldisplay;

import smlauncher.util.Palette;

import javax.swing.*;

/**
 * A generic panel displayed inside a scroll pane.
 *
 * @author SlavSquatSuperstar
 */
public abstract class ScrollablePanel extends JPanel {

	public ScrollablePanel() {
		super(true);
		setBackground(Palette.paneColor);
		setOpaque(true);
	}

	public abstract void updatePanel();

}
