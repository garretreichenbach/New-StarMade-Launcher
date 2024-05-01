package smlauncher.mainui.scrolldisplay;

import smlauncher.fileio.ImageFileUtil;

import javax.swing.*;
import java.awt.*;

/**
 * A scrolling pane that can display a view set by the user
 *
 * @author TheDerpGamer
 * @author SlavSquatSuperstar
 */
public class ScrollDisplayPanel extends JPanel {

	private final JScrollPane centerScrollPane;
	private final ScrollablePanel[] panels;

	public ScrollDisplayPanel(ScrollablePanel[] panels) {
		setDoubleBuffered(true);
		setOpaque(false);
		setLayout(new BorderLayout());

		JLabel background = new JLabel();
		background.setDoubleBuffered(true);
		background.setIcon(ImageFileUtil.getIcon("sprites/left_panel.png", 800, 500));
		add(background, BorderLayout.CENTER);

		centerScrollPane = new JScrollPane();
		centerScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		centerScrollPane.getHorizontalScrollBar().setUnitIncrement(16);
		centerScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
		centerScrollPane.getVerticalScrollBar().setUnitIncrement(16);
		add(centerScrollPane, BorderLayout.CENTER);
		this.panels = panels;
		setActiveViewPanel(0);
	}

	public void setActiveViewPanel(int index) {
		if (index >= 0 && index < panels.length) {
			ScrollablePanel panel = panels[index];
			panel.updatePanel();

			centerScrollPane.setViewportView(panel);
			SwingUtilities.invokeLater(() -> {
				JScrollBar scrollBar = centerScrollPane.getVerticalScrollBar();
				scrollBar.setValue(scrollBar.getMinimum());
			});
		}
	}

}
