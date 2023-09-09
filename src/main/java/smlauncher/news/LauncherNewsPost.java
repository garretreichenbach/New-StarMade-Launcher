package smlauncher.news;

/**
 * [Description]
 *
 * @author TheDerpGamer (TheDerpGamer#0027)
 */
public class LauncherNewsPost {

	private final String title;
	private final String content;
	private final String url;
	private final long date;

	public LauncherNewsPost(String title, String content, String url, long date) {
		this.title = title;
		this.content = content;
		this.url = url;
		this.date = date;
	}

	public String getTitle() {
		return title;
	}

	public String getContent() {
		return content;
	}

	public String getUrl() {
		return url;
	}

	public long getDate() {
		return date;
	}
}
