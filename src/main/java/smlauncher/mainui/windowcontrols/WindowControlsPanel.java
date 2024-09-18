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

	public WindowControlsPanel(ImageIcon minimizeIcon, ImageIcon closeIcon, Frame window) {
		super(new FlowLayout(FlowLayout.RIGHT), true);
		setOpaque(false);
		setBounds(0, 0, 800, 30);

		JButton minimizeButton = createControlButton(minimizeIcon, e -> window.setState(Frame.ICONIFIED));
		add(minimizeButton);

		JButton closeButton = createControlButton(closeIcon, e -> {
			window.dispose();
			System.exit(0);
		});
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
