package smlauncher.community;

import smlauncher.StarMadeLauncher;
import smlauncher.util.Palette;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * Panel for displaying community info in the launcher as well as some statistics such as how many players on online.
 * <p>Has buttons for both the StarMade and StarLoader discords, as well as a list of community server discords.</p>
 * <p>Server owners can apply to be listed here through a form linked at the bottom.</p>
 *
 * @author TheDerpGamer
 */
public class LauncherCommunityPanel extends JPanel {

	public static final String MAIN_DISCORD_URL = "https://discord.gg/SXbkYpU";
	public static final String STARLOADER_DISCORD_URL = "https://discord.gg/Y2UR7AXfsE";
	public static final String COMMUNITY_SERVER_JSON_URL = "https://raw.githubusercontent.com/garretreichenbach/New-StarMade-Launcher/main/community-servers.json";

	public LauncherCommunityPanel() {
		super(true);
		setBackground(Palette.paneColor);
		setOpaque(true);
	}

	public void updatePanel() {
		removeAll();
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

		//Main discords panel
		JPanel mainDiscordPanel = new JPanel(true);
		mainDiscordPanel.setBackground(Palette.paneColor);
		mainDiscordPanel.setOpaque(true);
		mainDiscordPanel.setLayout(new BoxLayout(mainDiscordPanel, BoxLayout.X_AXIS));

		JButton mainDiscordButton = new JButton("StarMade Discord") {
			@Override
			protected void paintComponent(Graphics g) {
				if(getModel().isArmed()) g.setColor(Palette.selectedColor);
				else g.setColor(Palette.deselectedColor);
				g.fillRoundRect(0, 0, getSize().width - 1, getSize().height - 1, 40, 40);
				super.paintComponent(g);
			}
		};
		mainDiscordButton.addActionListener(e -> {
			if(Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
				try {
					Desktop.getDesktop().browse(new URI(MAIN_DISCORD_URL));
				} catch(IOException | URISyntaxException exception) {
					throw new RuntimeException(exception);
				}
			}
		});
		mainDiscordButton.setIcon(StarMadeLauncher.getIcon("sprites/icon.png", 32, 32));
		mainDiscordButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, mainDiscordButton.getMinimumSize().height));
		mainDiscordButton.setAlignmentX(CENTER_ALIGNMENT);
		mainDiscordButton.setBackground(Palette.buttonColor);
		mainDiscordPanel.add(mainDiscordButton);
		mainDiscordPanel.add(Box.createRigidArea(new Dimension(4, 0)));

		JButton starLoaderDiscordButton = new JButton("StarLoader Discord") {
			@Override
			protected void paintComponent(Graphics g) {
				if(getModel().isArmed()) g.setColor(Palette.selectedColor);
				else g.setColor(Palette.deselectedColor);
				g.fillRoundRect(0, 0, getSize().width - 1, getSize().height - 1, 20, 20);
				super.paintComponent(g);
			}
		};
		starLoaderDiscordButton.addActionListener(e -> {
			if(Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
				try {
					Desktop.getDesktop().browse(new URI(STARLOADER_DISCORD_URL));
				} catch(IOException | URISyntaxException exception) {
					throw new RuntimeException(exception);
				}
			}
		});
		starLoaderDiscordButton.setIcon(StarMadeLauncher.getIcon("sprites/starloader.png", 32, 32));
		starLoaderDiscordButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, starLoaderDiscordButton.getMinimumSize().height));
		starLoaderDiscordButton.setAlignmentX(CENTER_ALIGNMENT);
		starLoaderDiscordButton.setBackground(Palette.buttonColor);
		mainDiscordPanel.add(starLoaderDiscordButton);
		add(mainDiscordPanel);

		//Community server list
		add(Box.createRigidArea(new Dimension(0, 10)));
		JPanel communityServerPanel = new JPanel(true);
		communityServerPanel.setBackground(Palette.paneColor);
		communityServerPanel.setOpaque(true);
		communityServerPanel.setLayout(new BoxLayout(communityServerPanel, BoxLayout.Y_AXIS));
		CommunityServerList communityServerList = new CommunityServerList(COMMUNITY_SERVER_JSON_URL);
		communityServerList.setBackground(Palette.paneColor);
		communityServerList.setOpaque(true);
		communityServerList.setAlignmentX(CENTER_ALIGNMENT);
		communityServerPanel.add(communityServerList);
		add(communityServerPanel);
	}
}
