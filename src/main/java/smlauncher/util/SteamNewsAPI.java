package smlauncher.util;

import org.json.JSONArray;
import org.json.JSONObject;
import smlauncher.LogManager;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

public class SteamNewsAPI {
	public static final String REQ_URL = "http://api.steampowered.com/ISteamNews/GetNewsForApp/v0002/?appid=244770&count=10&format=json";

	public static ArrayList<NewsPost> getPosts() {
		ArrayList<NewsPost> objs = new ArrayList<>();
		JSONObject obj = new JSONObject(GET());
		JSONArray arr = obj.getJSONObject("appnews").getJSONArray("newsitems");
		for(int i = 0; i < arr.length(); i++) {
			JSONObject post = arr.getJSONObject(i);
			NewsPost np = NewsPost.fromJson(post);
			objs.add(np);
		}
		return objs;
	}

	public static String GET() {
		try {
			StringBuilder result = new StringBuilder();
			URL url = new URL(REQ_URL);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("GET");
			try(BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
				for(String line; (line = reader.readLine()) != null; ) {
					result.append(line);
				}
			}
			return result.toString();
		} catch(Exception e) {
			LogManager.logWarning("Failed to get news from Steam API" + e.getMessage());
		}
		return "";
	}

	public static class NewsPost {
		String author;
		String title;
		String url;
		String contents;
		int date;

		public static NewsPost fromJson(JSONObject obj) {
			NewsPost np = new NewsPost();
			np.author = obj.getString("author");
			np.title = obj.getString("title");
			np.url = obj.getString("url");
			np.contents = obj.getString("contents");
			np.contents = np.contents.replaceAll("(.{1,80})(\\s+|$)", "$1\n");
			np.date = obj.getInt("date");
			return np;
		}

		public String getAuthor() {
			return author;
		}

		public String getTitle() {
			return title;
		}

		public String getUrl() {
			return url;
		}

		public String getContents() {
			return contents;
		}

		public int getDate() {
			return date;
		}

		@Override
		public String toString() {
			return "NewsPost{" + "author='" + author + '\'' + ", title='" + title + '\'' + ", url='" + url + '\'' + ", contents='" + contents + '\'' + ", date=" + date + '}';
		}
	}
}