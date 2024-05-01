package smlauncher.mainui;

import smlauncher.fileio.ImageFileUtil;

import javax.swing.*;
import java.awt.*;

/**
 * A panel containing a scroll pane whose view can be set.
 *
 * @author TheDerpGamer
 * @author SlavSquatSuperstar
 */
public class CenterDisplayPanel extends JPanel {

	private final PanelScrollPane centerScrollPane;
	private final LauncherScrollablePanel[] panels;

	public CenterDisplayPanel(LauncherScrollablePanel[] panels) {
		setDoubleBuffered(true);
		setOpaque(false);
		setLayout(new BorderLayout());

		JLabel background = new JLabel();
		background.setDoubleBuffered(true);
		background.setIcon(ImageFileUtil.getIcon("sprites/left_panel.png", 800, 500));
		add(background, BorderLayout.CENTER);

		centerScrollPane = new PanelScrollPane();
		add(centerScrollPane, BorderLayout.CENTER);
		this.panels = panels;
		setActiveViewPanel(0);
	}

	public void setActiveViewPanel(int index) {
		if (index >= 0 && index < panels.length) {
			centerScrollPane.setActivePanel(panels[index]);
		}
	}

}
