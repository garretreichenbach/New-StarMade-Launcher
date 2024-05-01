package smlauncher.mainui;

import smlauncher.util.Palette;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * A text field that allows the user to enter a port to run the server.
 *
 * @author TheDerpGamer
 * @author SlavSquatSuperstar
 */
public class PortField extends JTextField {

	public PortField(String text) {
		super(text);
		setDoubleBuffered(true);
		setOpaque(true);
		setBackground(Palette.paneColor);
		setForeground(Palette.textColor);
		setFont(new Font("Roboto", Font.PLAIN, 12));
		setMinimumSize(new Dimension(50, 20));
		setPreferredSize(new Dimension(50, 20));
		setMaximumSize(new Dimension(50, 20));
		//Show tooltip text
		addMouseListener(new MouseAdapter() {
			@Override
			public void mouseEntered(MouseEvent e) {
				setToolTipText("Port");
			}

			@Override
			public void mouseExited(MouseEvent e) {
				setToolTipText("");
			}
		});
		//Only allow numerical ports between 0-65535 to be typed
		addKeyListener(new KeyAdapter() {
			@Override
			public void keyTyped(KeyEvent e) {
				char c = e.getKeyChar();
				try {
					int port = Integer.parseInt(getText() + c);
					if (port < 0 || port > 65535 || !Character.isDigit(c)) e.consume();
				} catch (Exception ignored) {
					e.consume();
				}
			}
		});
	}

}
