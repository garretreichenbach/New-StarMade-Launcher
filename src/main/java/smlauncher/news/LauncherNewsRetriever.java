package smlauncher.news;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

/**
 * [Description]
 *
 * @author TheDerpGamer (TheDerpGamer#0027)
 */
public class LauncherNewsRetriever {

	private static final String NEWS_URL = "http://api.steampowered.com/ISteamNews/GetNewsForApp/v2/?appid=244770&count=3&maxlength=300&format=json";

	public ArrayList<LauncherNewsPost> getNews(int max) {
		ArrayList<LauncherNewsPost> news = new ArrayList<>();
		try {
			//Retrieve news from Steam
			URL newsURL = new URL(NEWS_URL);
			//Format is Json, get the string data from the URL
			InputStream inputStream = newsURL.openStream();
			JSONObject newsObject = new JSONObject(new String(inputStream.readAllBytes(), StandardCharsets.UTF_8));
			JSONObject appNews = newsObject.getJSONObject("appnews");
			JSONArray newsItems = appNews.getJSONArray("newsitems");
			//Get news posts
			for(int i = 0; i < max; i++) {
				try {
					JSONObject newsItem = newsItems.getJSONObject(i);
					String title = newsItem.getString("title");
					String content = newsItem.getString("contents");
					String url = newsItem.getString("url");
					long date = newsItem.getLong("date");
					news.add(new LauncherNewsPost(title, content, url, date));
				} catch(Exception e) {
					e.printStackTrace();
				}
			}
			inputStream.close();
		} catch(Exception exception) {
			exception.printStackTrace();
		}
		return news;
	}
}
