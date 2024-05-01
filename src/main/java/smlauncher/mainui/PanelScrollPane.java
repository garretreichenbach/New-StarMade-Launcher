package smlauncher.mainui;


import javax.swing.*;
import java.awt.*;

/**
 * A scrolling pane that can be made to display a panel.
 *
 * @author TheDerpGamer
 * @author SlavSquatSuperstar
 */
public class PanelScrollPane extends JScrollPane {

	public PanelScrollPane() {
		setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		getHorizontalScrollBar().setUnitIncrement(16);
		setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
		getVerticalScrollBar().setUnitIncrement(16);
	}

	@Override
	public void setViewportView(Component view) {
		if (view instanceof LauncherScrollablePanel) {
			setActivePanel((LauncherScrollablePanel) view);
		} else {
			super.setViewportView(view);
		}
	}

	private void setActivePanel(LauncherScrollablePanel panel) {
		panel.updatePanel();
		setViewportView(panel);
		SwingUtilities.invokeLater(() -> {
			JScrollBar scrollBar = getVerticalScrollBar();
			scrollBar.setValue(scrollBar.getMinimum());
		});
	}

}
