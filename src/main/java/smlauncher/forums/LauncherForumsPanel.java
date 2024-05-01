package smlauncher.forums;

import smlauncher.mainui.scrollcontent.LauncherScrollablePanel;

import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * Panel for displaying forums in the launcher.
 *
 * @author TheDerpGamer
 */
public class LauncherForumsPanel extends LauncherScrollablePanel {

	@Override
	public void updatePanel() {
		removeAll();
		if(Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
			String ccURL = "https://starmadedock.net/forums/";
			try {
				Desktop.getDesktop().browse(new URI(ccURL));
			} catch(IOException | URISyntaxException exception) {
				exception.printStackTrace();
			}
		}
	}
}
