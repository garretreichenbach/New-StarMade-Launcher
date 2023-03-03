package smlauncher.news;

import org.json.JSONArray;
import org.json.JSONObject;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;

import static smlauncher.StarMadeLauncher.deselectedColor;
import static smlauncher.StarMadeLauncher.selectedColor;

/**
 * Panel for displaying launcher news.
 *
 * @author TheDerpGamer (TheDerpGamer#0027)
 */
public class LauncherNewsPanel extends JPanel {

	private static final String NEWS_URL = "http://api.steampowered.com/ISteamNews/GetNewsForApp/v0002/?appid=244770&count=3&maxlength=300&format=json";
	private final HashMap<JLabel, NewsEntry> newsMap = new HashMap<>();
	private final JList<JLabel> newsList = new JList<>();
	private final DefaultListModel<JLabel> listModel = new DefaultListModel<>();

	private final JPanel contentPanel;

	public LauncherNewsPanel(JPanel contentPanel) {
		super(true);
		contentPanel.remove(this);
		this.contentPanel = contentPanel;
	}

	public void initialize() {
		updateNews();
		for(int i = 0; i < listModel.getSize(); i ++) {
			JLabel entry = listModel.getElementAt(i);
			entry.setFont(new Font("Roboto", Font.BOLD, 12));
			entry.setForeground(selectedColor);
			entry.setDoubleBuffered(true);
			entry.setOpaque(false);
			entry.setBounds(0, 0, 30, 30);
			entry.setHorizontalAlignment(SwingConstants.CENTER);
		}
		setLayout(new BorderLayout());

		JScrollPane scrollPane = new JScrollPane(newsList);
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
				newsList.setDoubleBuffered(true);
				newsList.setOpaque(false);
				newsList.setLayoutOrientation(JList.VERTICAL);
				newsList.setVisibleRowCount(-1);
				newsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
				newsList.setBorder(new EmptyBorder(0, 0, 0, 0));
				newsList.setFixedCellHeight(30);
				newsList.setCellRenderer((list1, value, index, isSelected, cellHasFocus) -> {
					if(isSelected) {
						value.setForeground(selectedColor);
						value.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, selectedColor));
					} else {
						value.setForeground(deselectedColor);
						value.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, deselectedColor));
					}
					return value;
				});
				newsList.addMouseMotionListener(new MouseMotionAdapter() {
					@Override
					public void mouseMoved(MouseEvent e) {
						int index = newsList.locationToIndex(e.getPoint());
						if(index != -1) newsList.setSelectedIndex(index);
						else newsList.clearSelection();
					}
				});
				newsList.addMouseListener(new MouseAdapter() {
					@Override
					public void mouseClicked(MouseEvent e) {
						if(e.getClickCount() == 1) {
							int index = newsList.locationToIndex(e.getPoint());
							if(index != -1) newsMap.get(listModel.getElementAt(index)).open(contentPanel);
						}
					}
				});

				JSONObject newsEntry = newsArray.getJSONObject(i);
				String title = newsEntry.getString("title");
				String link = newsEntry.getString("url");
				long date = newsEntry.getLong("date");
				JLabel label = new JLabel(title);
				listModel.addElement(label);
				newsMap.put(label, new NewsEntry(title, new Date(date), link));
			}
			newsList.setModel(listModel);
		} catch(IOException exception) {
			exception.printStackTrace();
		}
	}
}
