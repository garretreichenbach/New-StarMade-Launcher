package smlauncher.news;

import org.apache.commons.io.FileUtils;
import org.json.JSONObject;
import smlauncher.util.SetupUtil;

import javax.swing.*;
import javax.swing.text.html.HTMLDocument;
import java.awt.*;
import java.io.File;
import java.io.IOException;

/**
 * Entry for displaying news. Formats the news data from json into a custom html format.
 *
 * @author TheDerpGamer (TheDerpGamer#0027)
 */
public class NewsEntry extends JPanel {

	public static final String HTML_TEMPLATE_PATH = "./launcher/html/news_template.html";
	private final JSONObject newsJson;
	private HTMLDocument document;

	public NewsEntry(JSONObject newsJson) {
		this.newsJson = newsJson;
	}

	public File loadTemplate() {
		File templateFile = new File(HTML_TEMPLATE_PATH);
		if(!templateFile.exists()) SetupUtil.generateHTMLFiles();
		return templateFile;
	}

	/**
	 * Creates the HTML for this news entry.
	 */
	private String doReplacements(JSONObject newsData) {
		File templateFile = loadTemplate();
		String rawFileText = null;
		try {
			rawFileText = FileUtils.readFileToString(templateFile, "UTF-8");
		} catch(IOException exception) {
			exception.printStackTrace();
		}
		if(rawFileText == null) return null;
		String html = rawFileText.replace("%title%", newsData.getString("title"));
		html = html.replace("%url%", newsData.getString("url"));
		html = html.replace("%contents%", newsData.getString("contents"));
		html = html.replace("%author%", newsData.getString("author"));
		html = html.replace("%date%", newsData.getString("date"));
		return html;
	}

	public void open(JPanel contentPanel) {
		JEditorPane htmlPanel = new JEditorPane();
		htmlPanel.setContentType("text/html");
		htmlPanel.setEditable(false);
		new Thread(() -> {
			String html = doReplacements(newsJson);
			htmlPanel.setText(html);
			SwingUtilities.invokeLater(() -> {
				contentPanel.revalidate(); // notifies the layout manager
				contentPanel.repaint();    // repaints the panel
			});
		}).start();
		JScrollPane newsPanel = new JScrollPane(htmlPanel);
		newsPanel.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		newsPanel.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
		contentPanel.add(newsPanel, BorderLayout.CENTER);
	}
}
