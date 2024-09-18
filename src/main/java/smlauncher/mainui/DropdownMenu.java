package smlauncher.mainui;

import smlauncher.util.Palette;

import javax.swing.*;
import javax.swing.plaf.basic.BasicComboBoxUI;
import java.awt.*;

/**
 * A dropdown menu that displays many selectable options.
 *
 * @author TheDerpGamer
 * @author SlavSquatSuperstar
 */
public class DropdownMenu extends JComboBox<String> {

	public DropdownMenu(UIDefaults defaults) {
		setDoubleBuffered(true);
		setOpaque(true);
		setBackground(Palette.paneColor);
		setForeground(Palette.textColor);

		setUI(new BasicComboBoxUI() {
			@Override
			protected JButton createArrowButton() {
				JButton button = super.createArrowButton();
				button.setDoubleBuffered(true);
				button.setOpaque(false);
				button.setBackground(Palette.paneColor);
				button.setForeground(Palette.textColor);
				button.setContentAreaFilled(false);
				button.setRolloverEnabled(false);
				button.setBorder(BorderFactory.createEmptyBorder());
				button.setFocusable(false);
				return button;
			}
		});

		setRenderer(new DefaultListCellRenderer() {
			@Override
			public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
				super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
				if(isSelected) setBackground(Palette.selectedColor);
				else setBackground(Palette.deselectedColor);
				return this;
			}
		});

		putClientProperty("Nimbus.Overrides", defaults);
		putClientProperty("Nimbus.Overrides.InheritDefaults", true);
	}

}
