package smlauncher.mainui;

import smlauncher.starmade.StackLayout;

import javax.swing.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseMotionAdapter;

/**
 * A panel for the main window that can be used to drag the window.
 *
 * @author TheDerpGamer
 * @author SlavSquatSuperstar
 */
public class WindowDragPanel extends JPanel {

	public WindowDragPanel(MouseAdapter clickAction, MouseMotionAdapter dragAction) {
		setDoubleBuffered(true);
		setOpaque(false);
		setLayout(new StackLayout());

		addMouseListener(clickAction);
		addMouseMotionListener(dragAction);
	}
}
