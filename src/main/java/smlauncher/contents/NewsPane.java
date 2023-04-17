package smlauncher.contents;

import smlauncher.libt.BBCodeToHTMLConverter;
import smlauncher.libt.SteamNewsAPI;

import javax.swing.*;
import java.awt.*;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;

public class NewsPane extends JEditorPane {
    public static int X = 226;
    public static int Y = 45;
    public static int W = 675;
    public static int H = 295;
    public NewsPane(MainPanel mainPanel) {
        mainPanel.add(this);
        setBounds(X,Y,W,H);
        setBackground(new Color(0, 0, 0, 0));
        setForeground(Color.white);
        setOpaque(false);
        setEditable(false);
        setContentType("text/html");

        StringBuilder sb = new StringBuilder();
        for (SteamNewsAPI.NewsPost post : SteamNewsAPI.getPosts()) {
            ArrayList<String> lines = BBCodeToHTMLConverter.convert(post.getContents());

            LocalDate ldt = Instant.ofEpochMilli(post.getDate()*1000L).atZone(ZoneId.systemDefault()).toLocalDate();
            lines.add("<p><a href=\""+post.getUrl()+"\">Posted on " + ldt.toString() + " by " + post.getAuthor() + "</a></p>");
            lines = BBCodeToHTMLConverter.insertColors(lines, "#eeeeee");
            lines.add("<hr>");

            for (String line : lines) {
                sb.append(line);
            }
        }
        setText(sb.toString());


    }
    public static final Color paneColor = new Color(18, 18, 18);
    @Override
    protected void paintComponent(Graphics g) {


        g.setColor(paneColor);
        g.fillRect(0,0, W, getHeight());

        super.paintComponent(g);
    }
}
