package smlauncher.mainui.windowcontrols;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;

/**
 * A panel for the main window that contains the minimize close buttons.
 *
 * @author TheDerpGamer
 * @author SlavSquatSuperstar
 */
public class WindowControlsPanel extends JPanel {

	public WindowControlsPanel(
			ImageIcon minimizeIcon, ActionListener minimizeAction,
			ImageIcon closeIcon, ActionListener closeAction
	) {
		setDoubleBuffered(true);
		setLayout(new FlowLayout(FlowLayout.RIGHT));
		setOpaque(false);
		setBounds(0, 0, 800, 30);

		JButton minimizeButton = createControlButton(minimizeIcon, minimizeAction);
		add(minimizeButton);

		JButton closeButton = createControlButton(closeIcon, closeAction);
		add(closeButton);
	}

	private static JButton createControlButton(ImageIcon minimizeIcon, ActionListener minimizeAction) {
		JButton minimizeButton = new JButton(null, minimizeIcon);
		minimizeButton.setDoubleBuffered(true);
		minimizeButton.setOpaque(false);
		minimizeButton.setContentAreaFilled(false);
		minimizeButton.setBorderPainted(false);
		minimizeButton.addActionListener(minimizeAction);
		return minimizeButton;
	}

}
