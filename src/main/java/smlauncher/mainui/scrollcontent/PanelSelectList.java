package smlauncher.mainui.scrollcontent;

import smlauncher.util.Palette;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;

/**
 * A list of buttons for displaying different panels.
 *
 * @author TheDerpGamer
 * @author SlavSquatSuperstar
 */
public abstract class PanelSelectList extends JList<JLabel> {

	public PanelSelectList(String[] labelNames) {
		setDoubleBuffered(true);
		setOpaque(false);
		setLayoutOrientation(JList.VERTICAL);
		setVisibleRowCount(-1);
		setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		setCellRenderer((list1, value, index, isSelected, cellHasFocus) -> {
			if (isSelected) {
				value.setForeground(Palette.selectedColor);
				value.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, Palette.selectedColor));
			} else {
				value.setForeground(Palette.deselectedColor);
				value.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, Palette.deselectedColor));
			}
			return value;
		});
		//Highlight on mouse hover
		addMouseMotionListener(new MouseMotionAdapter() {
			@Override
			public void mouseMoved(MouseEvent e) {
				int index = locationToIndex(e.getPoint());
				if (index != -1) setSelectedIndex(index);
				else clearSelection();
			}
		});
		//Do something on mouse click
		addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (e.getClickCount() == 1) {
					int index = locationToIndex(e.getPoint());
					onClickLabel(index);
				}
			}
		});
		//Add list labels
		setFixedCellHeight(48);
		setListModel(labelNames);
	}

	public abstract void onClickLabel(int index);

	//Add labels to list
	private void setListModel(String[] labelNames) {
		DefaultListModel<JLabel> listModel = new DefaultListModel<>();
		for (String name : labelNames) {
			JLabel label = new JLabel(name);
			label.setHorizontalAlignment(SwingConstants.CENTER);
			label.setFont(new Font("Roboto", Font.BOLD, 18));
			label.setForeground(Palette.selectedColor);
			label.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, Palette.selectedColor));
			label.setDoubleBuffered(true);
			label.setOpaque(false);
			listModel.addElement(label);
		}
		setModel(listModel);
	}

}
