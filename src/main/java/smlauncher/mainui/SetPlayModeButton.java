package smlauncher.mainui;

import smlauncher.util.Palette;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * A button that sets the play mode for the game.
 *
 * @author TheDerpGamer
 * @author SlavSquatSuperstar
 */
public class SetPlayModeButton extends JButton {

	public SetPlayModeButton(String text, ActionListener clickAction) {
		super(text);
		setFont(new Font("Roboto", Font.BOLD, 12));
		setDoubleBuffered(true);
		setOpaque(false);
		setContentAreaFilled(false);
		setBorderPainted(false);
		setForeground(Palette.textColor);
		//Change color when mouse over
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
		//Switch the play modo
		addActionListener(clickAction);
	}

}
