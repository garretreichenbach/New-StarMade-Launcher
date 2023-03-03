package smlauncher.news;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.util.Date;

/**
 * News entry for the launcher news panel.
 *
 * @author TheDerpGamer (TheDerpGamer#0027)
 */
public class NewsEntry extends JPanel {

	private final String title;
	private final Date date;
	private final String link;

	public NewsEntry(String title, Date date, String link) {
		this.title = title;
		this.date = date;
		this.link = link;
	}

	public void open(JPanel contentPanel) {
		JEditorPane htmlPanel = new JEditorPane();
		htmlPanel.setContentType("text/html");
		htmlPanel.setEditable(false);
		new Thread(() -> {
			try {
				htmlPanel.setPage(link);
				contentPanel.repaint();
			} catch(IOException exception) {
				exception.printStackTrace();
				htmlPanel.setText("<html><body><h1>Failed to load news entry</h1></body></html>");
			}
		}).start();
		JScrollPane newsPanel = new JScrollPane(htmlPanel);
		newsPanel.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		newsPanel.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
		contentPanel.add(newsPanel, BorderLayout.CENTER);
	}
}
