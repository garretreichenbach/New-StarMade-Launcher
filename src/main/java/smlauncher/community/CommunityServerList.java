package smlauncher.community;

import org.json.JSONObject;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.io.IOException;
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
			serverList.put("servers", new JSONObject[0]);
		}

		DefaultTableModel tableModel = new DefaultTableModel(new Object[] {"Name", "Description", "URL"}, 0);
		setModel(tableModel);

		for(int i = 0; i < serverList.getJSONArray("servers").length(); i++) {
			JSONObject server = serverList.getJSONArray("servers").getJSONObject(i);
			CommunityServer communityServer = new CommunityServer();
			communityServer.name = server.getString("name");
			communityServer.description = server.getString("description");
			communityServer.url = server.getString("url");
			tableModel.addRow(new Object[] {communityServer.name, communityServer.description, communityServer.url});
		}
	}
}
