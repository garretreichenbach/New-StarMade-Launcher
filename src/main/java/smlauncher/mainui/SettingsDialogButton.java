package smlauncher.mainui;

import smlauncher.util.Palette;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * A button that opens a settings dialog on click.
 *
 * @author TheDerpGamer
 * @author SlavSquatSuperstar
 */
public class SettingsDialogButton extends JButton {

	public SettingsDialogButton(String text, ImageIcon icon, ActionListener clickAction) {
		super(text);
		setIcon(icon);
		setFont(new Font("Roboto", Font.BOLD, 12));
		setDoubleBuffered(true);
		setOpaque(false);
		setContentAreaFilled(false);
		setForeground(Palette.textColor);
		addMouseListener(new MouseAdapter() {
			@Override
			public void mouseEntered(MouseEvent e) {
				setForeground(Palette.selectedColor);
			}

			@Override
			public void mouseExited(MouseEvent e) {
				setForeground(Palette.textColor);
			}
		});
		addActionListener(clickAction);
	}
}
