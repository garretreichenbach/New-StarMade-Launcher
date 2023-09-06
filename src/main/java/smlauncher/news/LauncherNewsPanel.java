package smlauncher.news;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;

/**
 * Panel for displaying launcher news.
 *
 * @author TheDerpGamer
 */
public class LauncherNewsPanel extends JPanel {

	private static final String NEWS_URL = "https://steamcommunity.com/app/244770/allnews/";

	private final JPanel contentPanel;

	public LauncherNewsPanel(JPanel contentPanel) {
		super(true);
		this.contentPanel = contentPanel;
		this.contentPanel.remove(this);
		this.contentPanel.add(this);
		setLayout(new BorderLayout());

		JEditorPane htmlPanel = new JEditorPane();
		htmlPanel.setContentType("text/html");
		htmlPanel.setEditable(false);
		try {
			htmlPanel.setPage(NEWS_URL);
			contentPanel.repaint();
		} catch(IOException exception) {
			exception.printStackTrace();
			htmlPanel.setText("<html><body><h1>Failed to load news entry</h1></body></html>");
		}
		JScrollPane newsPanel = new JScrollPane(htmlPanel);
		newsPanel.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		newsPanel.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
		contentPanel.add(newsPanel, BorderLayout.CENTER);
	}
}
