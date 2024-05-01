package smlauncher.mainui.scrollcontent;

import smlauncher.fileio.ImageFileUtil;
import smlauncher.starmade.StackLayout;

import javax.swing.*;
import java.awt.*;

/**
 * A list of buttons that controls which panel is displayed in the scroll pane.
 *
 * @author TheDerpGamer
 * @author SlavSquatSuperstar
 */
public class DisplayControlPanel extends JPanel {

	public DisplayControlPanel(CenterDisplayPanel centerPanel) {
		setDoubleBuffered(true);
		setOpaque(false);
		setLayout(new StackLayout());

		JLabel leftLabel = new JLabel();
		leftLabel.setDoubleBuffered(true);
		//Resize the image to the left panel
		leftLabel.setIcon(ImageFileUtil.getIcon("sprites/left_panel.png", 150, 500));
		//Stretch the image to the left panel
		add(leftLabel, StackLayout.BOTTOM);

		JPanel topLeftPanel = new JPanel();
		topLeftPanel.setDoubleBuffered(true);
		topLeftPanel.setOpaque(false);
		topLeftPanel.setLayout(new BorderLayout());
		add(topLeftPanel, StackLayout.TOP);

		//Add list to display links to game website
		JList<JLabel> list = new PanelSelectList(
				new String[]{"NEWS", "FORUMS", "CONTENT", "COMMUNITY"}
		) {
			@Override
			public void onClickLabel(int index) {
				//Select panels on click
				centerPanel.setActiveViewPanel(index);
			}
		};
		topLeftPanel.add(list);

		//Display game logo
		JPanel topLeftLogoPanel = createLogoPanel();
		topLeftPanel.add(topLeftLogoPanel, BorderLayout.NORTH);
	}

	private static JPanel createLogoPanel() {
		JPanel topLeftLogoPanel = new JPanel();
		topLeftLogoPanel.setDoubleBuffered(true);
		topLeftLogoPanel.setOpaque(false);
		topLeftLogoPanel.setLayout(new BorderLayout());

		//Add a left inset
		JPanel leftInset = new JPanel();
		leftInset.setDoubleBuffered(true);
		leftInset.setOpaque(false);
		topLeftLogoPanel.add(leftInset, BorderLayout.CENTER);

		//Add logo at top left
		JLabel logo = new JLabel();
		logo.setDoubleBuffered(true);
		logo.setOpaque(false);
		logo.setIcon(ImageFileUtil.getIcon("sprites/logo.png"));
		leftInset.add(logo);
		return topLeftLogoPanel;
	}

}
