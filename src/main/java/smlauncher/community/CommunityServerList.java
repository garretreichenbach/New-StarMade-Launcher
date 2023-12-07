package smlauncher.community;

import org.json.JSONArray;
import org.json.JSONObject;
import smlauncher.util.Palette;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;

/**
 * Table for displaying community servers.
 *
 * @author TheDerpGamer
 */
public class CommunityServerList extends JTable {

	private JSONObject serverList;

	public CommunityServerList(String url) {
		try {
			serverList = new JSONObject(new String(new URL(url).openStream().readAllBytes(), StandardCharsets.UTF_8));
		} catch(IOException exception) {
			exception.printStackTrace();
			serverList = new JSONObject();
			serverList.put("servers", new JSONArray());
		}

		DefaultTableModel tableModel = new DefaultTableModel(new Object[] {"Name", "Description"}, 0);
		setModel(tableModel);
		setGridColor(Palette.selectedColor);
		setForeground(Palette.textColor);
		setShowGrid(true);
		setDefaultEditor(Object.class, null);
		setFont(new Font("Arial", Font.BOLD, 12));
		getColumnModel().getColumn(0).setPreferredWidth(30);

		for(int i = 0; i < serverList.getJSONArray("servers").length(); i++) {
			JSONObject server = serverList.getJSONArray("servers").getJSONObject(i);
			CommunityServer communityServer = new CommunityServer();
			communityServer.name = server.getString("name");
			communityServer.description = server.getString("description");
			communityServer.url = server.getString("url");
			tableModel.addRow(new Object[] {communityServer.name, communityServer.description});
		}

		addMouseListener(new MouseAdapter() {
			@Override
			public void mouseEntered(java.awt.event.MouseEvent e) {
				int row = rowAtPoint(e.getPoint());
				if(row >= 0 && row < getRowCount()) {
					setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
//					setForeground(Palette.selectedColor);
				} else {
					setCursor(Cursor.getDefaultCursor());
//					setForeground(Palette.textColor);
				}
			}

			@Override
			public void mouseExited(java.awt.event.MouseEvent e) {
				setCursor(Cursor.getDefaultCursor());
//				setForeground(Palette.textColor);
			}

			@Override
			public void mouseClicked(java.awt.event.MouseEvent e) {
				int row = rowAtPoint(e.getPoint());
				if(row >= 0 && row < getRowCount()) {
					String url = serverList.getJSONArray("servers").getJSONObject(row).getString("url");
					if(Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
						try {
							Desktop.getDesktop().browse(new URI(url));
						} catch(IOException | URISyntaxException exception) {
							throw new RuntimeException(exception);
						}
					}
				}
			}
		});
	}
}
