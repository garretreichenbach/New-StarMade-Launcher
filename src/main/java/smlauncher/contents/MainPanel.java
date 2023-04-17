package smlauncher.contents;

import smlauncher.Images;

import javax.swing.*;
import java.awt.*;

public class MainPanel extends JPanel {

    public static MainPanel inst;
    NewsPane np;
    JScrollPane scrollPane;
    JPanel linksPanel;

    public MainPanel(){
        inst = this;
        setLayout(null);
        // Initialize all the side buttons
        int ix = 45; // Initial Y
        int x = 30; // Static X
        int i = 85; // Spacing
        PanelSwitcherButton news = new PanelSwitcherButton(this, x, ix, 150, 40, "button", "NEWS");
        PanelSwitcherButton options = new PanelSwitcherButton(this, x, ix + i, 150, 40, "button", "OPTIONS");
        PanelSwitcherButton links = new PanelSwitcherButton(this, x, ix + (i *2), 150, 40, "button", "LINKS");
        PanelSwitcherButton info = new PanelSwitcherButton(this, x, ix + (i * 3), 150, 40, "button", "INFO");

        LaunchPanel bgPanel = new LaunchPanel(this, x, 391, "bar");

         np = new NewsPane(this);

         scrollPane = new JScrollPane(np);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.setBounds(NewsPane.X, NewsPane.Y, NewsPane.W, NewsPane.H);
        scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        scrollPane.setVisible(true);
        scrollPane.getVerticalScrollBar().setBackground(NewsPane.paneColor);
        scrollPane.getVerticalScrollBar().setBorder(BorderFactory.createEmptyBorder());
        add(scrollPane);

        linksPanel = new LinksPanel();
        add(linksPanel);


        switchActivePane(0);
    }

    public void switchActivePane(int id){
        np.setVisible(false);
        scrollPane.setVisible(false);
        linksPanel.setVisible(false);
        if(id == 0){
            np.setVisible(true);
            scrollPane.setVisible(true);
        }else if(id == 1){
            linksPanel.setVisible(true);
        }else if(id == 2){
            linksPanel.setVisible(true);
        }else if(id == 3){
            linksPanel.setVisible(true);
        }
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
