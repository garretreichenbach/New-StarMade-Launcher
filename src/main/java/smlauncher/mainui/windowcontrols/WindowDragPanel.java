package smlauncher.mainui.windowcontrols;

import smlauncher.fileio.ImageFileUtil;
import smlauncher.starmade.StackLayout;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/**
 * A panel for the main window that can be used to drag the window.
 *
 * @author TheDerpGamer
 * @author SlavSquatSuperstar
 */
public class WindowDragPanel extends JPanel {

	private int mouseX, mouseY;

	public WindowDragPanel(ImageIcon icon, Frame window) {
		super(new StackLayout(), true);
		setOpaque(false);

		//Detect when the panel is clicked
		addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				mouseX = e.getX();
				mouseY = e.getY();
				//If the mouse is on the top panel buttons, don't drag the window
				if (mouseX > 770 || mouseY > 30) {
					mouseX = 0;
					mouseY = 0;
				}
				super.mousePressed(e);
			}
		});

		//Drag the window when the mouse is moved
		addMouseMotionListener(new MouseMotionAdapter() {
			@Override
			public void mouseDragged(MouseEvent e) {
				if (mouseX != 0 && mouseY != 0) {
					int mouseDx = e.getX() - mouseX;
					int mouseDy = e.getY() - mouseY;
					window.setLocation(window.getLocation().x + mouseDx, window.getLocation().y + mouseDy);
				}
				super.mouseDragged(e);
			}
		});

		//Give the panel a sprite
		JLabel topLabel = new JLabel();
		topLabel.setDoubleBuffered(true);
		topLabel.setIcon(icon);
		add(topLabel);

		//Buttons to minimize/close window
		JPanel windowControlsPanel = new WindowControlsPanel(
				ImageFileUtil.getIcon("sprites/minimize_icon.png"),
				ImageFileUtil.getIcon("sprites/close_icon.png"),
				window
		);
		add(windowControlsPanel);

		//Display the Schine logo
		JPanel topRightPanel = createSchineLogoPanel();
		add(topRightPanel, BorderLayout.EAST);
	}

	private static JPanel createSchineLogoPanel() {
		JPanel topRightPanel = new JPanel();
		topRightPanel.setDoubleBuffered(true);
		topRightPanel.setOpaque(false);
		topRightPanel.setLayout(new BorderLayout());

		JLabel logoLabel = new JLabel();
		logoLabel.setDoubleBuffered(true);
		logoLabel.setOpaque(false);
		logoLabel.setIcon(ImageFileUtil.getIcon("sprites/launcher_schine_logo.png"));
		topRightPanel.add(logoLabel, BorderLayout.EAST);
		return topRightPanel;
	}

}
