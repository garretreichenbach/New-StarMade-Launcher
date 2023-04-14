package smlauncher.contents;

import smlauncher.Images;
import smlauncher.libt.BBCodeToHTMLConverter;
import smlauncher.libt.SteamNewsAPI;

import javax.swing.*;
import javax.swing.text.EditorKit;
import java.awt.*;
import java.io.IOException;
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

            lines.add(0, "<h3>sus!!!!</h3>");
            lines = BBCodeToHTMLConverter.insertColors(lines, "#eeeeee");
            lines.add("<hr>");

            for (String line : lines) {
                sb.append(line);
            }
        }
        setText(sb.toString());


    }

    @Override
    protected void paintComponent(Graphics g) {
        Color inner = new Color(18, 18, 18);

        g.setColor(inner);
        g.fillRect(0,0, W, getHeight());

        super.paintComponent(g);
    }
}
