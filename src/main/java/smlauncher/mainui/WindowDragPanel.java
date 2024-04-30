package smlauncher.mainui;

import smlauncher.starmade.StackLayout;

import javax.swing.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.MouseMotionListener;

/**
 * A panel for the main window that can be used to drag the window.
 *
 * @author TheDerpGamer
 * @author SlavSquatSuperstar
 */
public class WindowDragPanel extends JPanel {

	public WindowDragPanel(
			ImageIcon icon, MouseListener clickAction, MouseMotionListener dragAction
	) {
		setDoubleBuffered(true);
		setOpaque(false);
		setLayout(new StackLayout());

		addMouseListener(clickAction);
		addMouseMotionListener(dragAction);

		//Give the panel a sprite
		JLabel topLabel = new JLabel();
		topLabel.setDoubleBuffered(true);
		topLabel.setIcon(icon);
		add(topLabel);
	}

}
