package smlauncher.contents;

import javax.swing.*;
import java.awt.*;

public class LinksPanel extends JPanel {
    public LinksPanel() {
        setBounds(NewsPane.X, NewsPane.Y, NewsPane.W, NewsPane.H);
        setLayout(new FlowLayout(FlowLayout.CENTER, 10, 10));
        setBackground(new Color(0,0,0,10));
        new UrlOpenButton(this, 150, 30, "https://starmadedock.net/", "Forums");
        new UrlOpenButton(this, 150, 30, "https://discord.com/invite/SXbkYpU", "Discord Server");
        new UrlOpenButton(this, 150, 30, "https://www.twitch.tv/starmade", "Twitch (inactive)");
        new UrlOpenButton(this, 150, 30, "https://starmadedock.net/forums/gamesupport/", "Game Support");
        new UrlOpenButton(this, 150, 30, "https://discord.gg/GHHaNGs", "Modding Discord");
        new UrlOpenButton(this, 150, 30, "http://translate.star-made.org/", "Help translate");
        new UrlOpenButton(this, 150, 30, "https://github.com/Schine/StarMade-Open", "GitHub");
        new UrlOpenButton(this, 150, 30, "https://github.com/garretreichenbach/New-StarMade-Launcher", "Launcher GitHub");
        new UrlOpenButton(this, 150, 30, "https://steamcommunity.com/app/244770/discussions/", "Steam Forums");
        new UrlOpenButton(this, 150, 30, "https://old.reddit.com/r/Starmade/", "Reddit"); // New reddit is the ugliest thing I have seen in my entire life

    }
}
