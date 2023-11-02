package smlauncher.news;

import org.json.JSONArray;
import org.json.JSONObject;

import javax.swing.*;
import java.awt.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

/**
 * Panel for displaying launcher news.
 *
 * @author TheDerpGamer (TheDerpGamer#0027)
 */
public class LauncherNewsPanel extends JPanel {

	private static final String NEWS_URL = "http://api.steampowered.com/ISteamNews/GetNewsForApp/v0002/?appid=244770&count=3&maxlength=300&format=json";
	private final ArrayList<NewsEntry> newsList = new ArrayList<>();
	private final JPanel contentPanel;

	public LauncherNewsPanel(JPanel contentPanel) {
		super(true);
		this.contentPanel = contentPanel;
		initialize();
	}

	public void initialize() {
		updateNews();
		JScrollPane scrollPane = new JScrollPane(contentPanel);
		scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
		add(scrollPane, BorderLayout.WEST);
	}

	public void updateNews() {
		//Fetch news json
		try {
			URL url = new URL(NEWS_URL);
			InputStream inputStream = url.openStream();
			InputStreamReader isr = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
			BufferedReader reader = new BufferedReader(isr);
			StringBuilder json = new StringBuilder();
			int c;
			while ((c = reader.read()) != -1) json.append((char) c);
			String str = json.toString();

			//Parse json
			JSONObject jsonObject = new JSONObject(str);
			JSONObject newsObject = jsonObject.getJSONObject("appnews");
			JSONArray newsArray = newsObject.getJSONArray("newsitems");

			for(int i = 0; i < newsArray.length(); i ++) {
				NewsEntry newsEntry = new NewsEntry(newsArray.getJSONObject(i));
				newsList.add(newsEntry);
				newsEntry.open(contentPanel);
			}
		} catch(IOException exception) {
			exception.printStackTrace();
		}
	}
}
