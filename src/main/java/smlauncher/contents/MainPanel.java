package smlauncher.contents;

import smlauncher.Images;

import javax.swing.*;
import java.awt.*;

public class MainPanel extends JPanel {
    public MainPanel(){
        setLayout(null);
        // Initialize all the side buttons
        int ix = 45; // Initial Y
        int x = 30; // Static X
        int i = 85; // Spacing
        CoolButton news = new CoolButton(this, x, ix, 150, 40, "button", "NEWS");
        CoolButton options = new CoolButton(this, x, ix + i, 150, 40, "button", "OPTIONS");
        CoolButton links = new CoolButton(this, x, ix + (i *2), 150, 40, "button", "LINKS");
        CoolButton info = new CoolButton(this, x, ix + (i * 3), 150, 40, "button", "INFO");

        LaunchPanel bgPanel = new LaunchPanel(this, x, 391, "bar");

        NewsPane np = new NewsPane(this);

        JScrollPane scrollPane = new JScrollPane(np);
        Image bg = Images.get("npane");
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.setBounds(NewsPane.X, NewsPane.Y, bg.getWidth(null), bg.getHeight(null));
        scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        scrollPane.setBackground(Color.black);
        scrollPane.setForeground(Color.blue);
        add(scrollPane);
    }

    @Override
    protected void paintComponent(Graphics g) {
        g.drawImage(Images.get("background"), 0, 0, null);
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);
        g.drawImage(Images.get("bg2"), 0, 0, null);
    }
}
