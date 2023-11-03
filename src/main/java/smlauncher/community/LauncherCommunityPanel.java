package smlauncher.community;

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

	private static final String MAIN_DISCORD_URL = "https://discord.gg/SXbkYpU";
	private static final String STARLOADER_DISCORD_URL = "https://discord.gg/Y2UR7AXfsE";
	private static final String COMMUNITY_SERVER_JSON_URL = "https://raw.githubusercontent.com/garretreichenbach/New-StarMade-Launcher/main/community-servers.json";

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

		JButton mainDiscordButton = new JButton("StarMade Discord");
		mainDiscordButton.addActionListener(e -> {
			if(Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
				try {
					Desktop.getDesktop().browse(new URI(MAIN_DISCORD_URL));
				} catch(IOException | URISyntaxException exception) {
					throw new RuntimeException(exception);
				}
			}
		});
		mainDiscordButton.setIcon(new ImageIcon("resources/icon.png"));
		mainDiscordButton.setAlignmentX(Component.CENTER_ALIGNMENT);
		mainDiscordPanel.add(mainDiscordButton);

		JButton starLoaderDiscordButton = new JButton("StarLoader Discord");
		starLoaderDiscordButton.addActionListener(e -> {
			if(Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
				try {
					Desktop.getDesktop().browse(new URI(STARLOADER_DISCORD_URL));
				} catch(IOException | URISyntaxException exception) {
					throw new RuntimeException(exception);
				}
			}
		});
		starLoaderDiscordButton.setIcon(new ImageIcon("resources/starloader.png"));
		starLoaderDiscordButton.setAlignmentX(Component.CENTER_ALIGNMENT);
		mainDiscordPanel.add(starLoaderDiscordButton);
		add(mainDiscordPanel);

		//Community server list
		JPanel communityServerPanel = new JPanel(true);
		communityServerPanel.setBackground(Palette.paneColor);
		communityServerPanel.setOpaque(true);
		communityServerPanel.setLayout(new BoxLayout(communityServerPanel, BoxLayout.Y_AXIS));
		//Fetch community server list
		CommunityServerList communityServerList = new CommunityServerList(COMMUNITY_SERVER_JSON_URL);
	}
}
